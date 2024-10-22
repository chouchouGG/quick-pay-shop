package cn.learn.controller;

import cn.learn.common.constants.Constants;
import cn.learn.common.response.Response;
import cn.learn.controller.dto.CreatePayRequestDTO;
import cn.learn.domain.req.ShopCartReq;
import cn.learn.domain.res.PayOrderRes;
import cn.learn.service.IOrderService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.POST;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/alipay/")
public class AliPayController {

    @Value("${alipay.alipay_public_key}")
    private String alipayPublicKey;

    @Resource
    private IOrderService orderService;

    /**
     * 内部测试：http://localhost:8080/api/v1/alipay/create_pay_order
     * 外部测试：http://chouchou1.natapp1.cc/api/v1/alipay/create_pay_order
     *
     * {
     *     "userId": "10001",
     *     "productId": "100001"
     * }
     */
    @PostMapping(value = "create_pay_order")
    public Response<String> createPayOrder(@RequestBody CreatePayRequestDTO createPayRequestDTO){
        try {
            log.info("商品下单，根据商品ID创建支付单开始 userId:{} productId:{}", createPayRequestDTO.getUserId(), createPayRequestDTO.getUserId());
            String userId = createPayRequestDTO.getUserId();
            String productId = createPayRequestDTO.getProductId();

            // 下单
            PayOrderRes payOrderRes = orderService.createOrder(ShopCartReq.builder()
                    .userId(userId)
                    .productId(productId)
                    .build());
            log.info("商品下单，根据商品ID创建支付单完成 userId:{} productId:{} orderId:{}", userId, productId, payOrderRes.getOrderId());

            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(payOrderRes.getPayUrl())
                    .build();
        } catch (Exception e) {
            log.error("商品下单，根据商品ID创建支付单失败 userId:{} productId:{}", createPayRequestDTO.getUserId(), createPayRequestDTO.getUserId(), e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * <p>支付宝回调接口，通知业务方</p>
     * http://chouchou1.natapp1.cc/api/v1/alipay/alipay_notify_url
     */
    @PostMapping(value = "alipay_notify_url")
    public String payNotify(HttpServletRequest request) throws AlipayApiException {
        log.info("支付回调，消息接收 {}", request.getParameter("trade_status"));

        if (!request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            return "false";
        }

        // fixme 当前的代码确实忽略了参数可能是数组的情况，只取了第一个值。
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }

        String tradeNo = params.get("out_trade_no");
        String gmtPayment = params.get("gmt_payment");
        String alipayTradeNo = params.get("trade_no");
        String sign = params.get("sign");

        String content = AlipaySignature.getSignCheckContentV1(params);
        // 验证签名
        boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, alipayPublicKey, "UTF-8");
        // 支付宝验签
        if (!checkSignature) {
            return "false";
        } else {
            // 验签通过
            log.info("支付回调，交易名称: {}", params.get("subject"));
            log.info("支付回调，交易状态: {}", params.get("trade_status"));
            log.info("支付回调，支付宝交易凭证号: {}", params.get("trade_no"));
            log.info("支付回调，商户订单号: {}", params.get("out_trade_no"));
            log.info("支付回调，交易金额: {}", params.get("total_amount"));
            log.info("支付回调，买家在支付宝唯一id: {}", params.get("buyer_id"));
            log.info("支付回调，买家付款时间: {}", params.get("gmt_payment"));
            log.info("支付回调，买家付款金额: {}", params.get("buyer_pay_amount"));
            log.info("支付回调，支付回调，更新订单 {}", tradeNo);

            // 修改订单状态
            orderService.changeOrderPaySuccess(tradeNo);

            return "success";
        }
    }

}
