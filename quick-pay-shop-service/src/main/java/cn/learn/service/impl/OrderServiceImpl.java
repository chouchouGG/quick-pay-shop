package cn.learn.service.impl;

import cn.learn.common.constants.Constants;
import cn.learn.dao.IOrderDao;
import cn.learn.domain.po.PayOrder;
import cn.learn.domain.req.ShopCartReq;
import cn.learn.domain.res.PayOrderRes;
import cn.learn.domain.vo.ProductVO;
import cn.learn.service.IOrderService;
import cn.learn.service.rpc.ProductRPC;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @program: quick-pay-shop
 * @description: 订单服务接口的实现类
 * @author: chouchouGG
 * @create: 2024-10-18 22:37
 **/
@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {

    @Resource
    private IOrderDao orderDao;

    @Resource
    private ProductRPC productRPC;

    @Resource
    private AlipayClient alipayClient;

    /**
     * 支付宝服务器主动通知商户服务器的url。
     */
    @Value("${alipay.notify_url}")
    private String notifyUrl;

    /**
     * 用户在支付宝支付页面成功支付后，进行自动跳转的 url。
     */
    @Value("${alipay.return_url}")
    private String returnUrl;

    @Resource
    private EventBus eventBus;


    @Override
    public PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception {
        PayOrder payOrder = PayOrder.builder()
                .userId(shopCartReq.getUserId())
                .productId(shopCartReq.getProductId())
                .build();

        PayOrder unPayOrder = orderDao.queryUnPayOrder(payOrder);

        if (null != unPayOrder && Constants.OrderStatus.PAY_WAIT.getCode().equals(unPayOrder.getStatus())) {
            log.info("创建订单-存在，已存在未支付订单。userId:{} productId:{} orderId:{}", shopCartReq.getUserId(), shopCartReq.getProductId(), unPayOrder.getOrderId());
            return PayOrderRes.builder()
                    .orderId(unPayOrder.getOrderId())
                    .payUrl(unPayOrder.getPayUrl())
                    .build();
        } else if (null != unPayOrder && Constants.OrderStatus.CREATE.getCode().equals(unPayOrder.getStatus())) {
            // 订单存在但是 url字段未填写有可能是调用第三方支付接口时网络原因造成失败。
            log.info("创建订单-存在，存在未创建支付单订单，创建支付单开始 userId:{} productId:{} orderId:{}",
                    shopCartReq.getUserId(), shopCartReq.getProductId(), unPayOrder.getOrderId());
            payOrder = doPrepayOrder(unPayOrder.getProductId(), unPayOrder.getProductName(), unPayOrder.getOrderId(), unPayOrder.getTotalAmount());
            return PayOrderRes.builder()
                    .orderId(payOrder.getOrderId())
                    .payUrl(payOrder.getPayUrl())
                    .build();
        }

        // 2. 查询商品 & 创建订单
        ProductVO productVO = productRPC.queryProductByProductId(shopCartReq.getProductId()); // note：模拟调用商品服务的接口
        String orderId = RandomStringUtils.randomNumeric(10); // note：公司中会使用雪花算法来生产唯一id

        orderDao.insertOrder(PayOrder.builder()
                .userId(shopCartReq.getUserId())
                .productId(shopCartReq.getProductId())
                .productName(productVO.getProductName())
                .orderId(orderId)
                .totalAmount(productVO.getPrice())
                .orderTime(new Date())
                .status(Constants.OrderStatus.CREATE.getCode()) // 订单状态：CREATE
                .build()
        );

        // 3. 创建支付单，调用支付宝进行支付订单创建
        payOrder = doPrepayOrder(productVO.getProductId(), productVO.getProductName(), orderId, productVO.getPrice());
        return PayOrderRes.builder()
                .orderId(orderId)
                .payUrl(payOrder.getPayUrl())
                .build();
    }

    @Override
    public void changeOrderPaySuccess(String orderId) {
        PayOrder payOrder = PayOrder.builder()
                .orderId(orderId)
                .status(Constants.OrderStatus.PAY_SUCCESS.getCode())
                .build();
        orderDao.changeOrderPaySuccess(payOrder);

        // note：发送事件消息（是基于观察者模式实现的事件通知机制），
        //  事件消息有可能丢失，故有专门的定时任务 NoPayNotifyOrderJob 对丢失消息进行补偿发送。
        eventBus.post(JSON.toJSONString(payOrder));
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return orderDao.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return orderDao.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return orderDao.changeOrderClose(orderId);
    }


    private PayOrder doPrepayOrder(String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException {
        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
        payRequest.setNotifyUrl(notifyUrl);
        payRequest.setReturnUrl(returnUrl);

        // bizContent: 除公共参数外[所有请求参数]都必须放在这个参数中传递。
        JSONObject bizContent = new JSONObject();

        // 设置【业务请求参数】中的 4个必选参数。
        /**
         * out_trade_no
         * 【描述】商户订单号。由商家自定义，64个字符以内，仅支持字母、数字、下划线且需保证在商户端不重复。
         * 【示例值】20150320010101001
         */
        bizContent.put("out_trade_no", orderId); // note 将外部的交易订单id透传给支付宝
        /**
         * total_amount
         * 【描述】订单总金额，单位为元，精确到小数点后两位，取值范围为 [0.01,100000000]。金额不能为0。
         * 【示例值】88.88
         */
        bizContent.put("total_amount", totalAmount.toString());
        /**
         * subject
         * 【描述】订单标题。注意：不可使用特殊字符，如 /，=，& 等。
         * 【示例值】Iphone6 16G
         */
        bizContent.put("subject", productName);
        /**
         * product_code
         * 【描述】销售产品码，与支付宝签约的产品码名称。注：目前电脑支付场景下仅支持FAST_INSTANT_TRADE_PAY
         * 【示例值】FAST_INSTANT_TRADE_PAY
         */
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        payRequest.setBizContent(bizContent.toString());

        // note：pageExecute方法是由支付宝 SDK提供，该方法会调用支付宝平台接口，返回的响应体中的数据为【用户访问支付宝的跳转链接】。
        String form = alipayClient.pageExecute(payRequest, "POST").getBody();

        PayOrder payOrder = PayOrder.builder()
                .orderId(orderId)
                .payUrl(form) // 支付宝付款界面的 html form表单
                .status(Constants.OrderStatus.PAY_WAIT.getCode()).build();
        orderDao.updateOrderPayInfo(payOrder);

        return payOrder;
    }

}
