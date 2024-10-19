package cn.learn.service.impl;

import cn.learn.common.constants.Constants;
import cn.learn.dao.IOrderDao;
import cn.learn.domain.po.PayOrder;
import cn.learn.domain.req.ShopCartReq;
import cn.learn.domain.res.PayOrderRes;
import cn.learn.domain.vo.ProductVO;
import cn.learn.service.IOrderService;
import cn.learn.service.rpc.ProductRPC;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

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

    @Value("${alipay.notify_url}")
    private String notifyUrl;

    @Value("${alipay.return_url}")
    private String returnUrl;


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
        ProductVO productVO = productRPC.queryProductByProductId(shopCartReq.getProductId());
        String orderId = RandomStringUtils.randomNumeric(10); // 公司中会使用雪花算法来生产唯一id
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

    private PayOrder doPrepayOrder(String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException {
        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
        payRequest.setNotifyUrl(notifyUrl);
        payRequest.setReturnUrl(returnUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        bizContent.put("total_amount", totalAmount.toString());
        bizContent.put("subject", productName);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        payRequest.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(payRequest).getBody();

        PayOrder payOrder = new PayOrder();
        payOrder.setOrderId(orderId);
        payOrder.setPayUrl(form);
        payOrder.setStatus(Constants.OrderStatus.PAY_WAIT.getCode());

        orderDao.updateOrderPayInfo(payOrder);

        return payOrder;
    }

}
