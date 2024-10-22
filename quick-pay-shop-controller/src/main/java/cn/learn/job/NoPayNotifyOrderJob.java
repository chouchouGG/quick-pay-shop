package cn.learn.job;

import cn.learn.service.IOrderService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: quick-pay-shop
 * @description: 处理未接收到或未正确处理的支付回调通知
 * @author: chouchouGG
 * @create: 2024-10-19 19:38
 **/
@Slf4j
@Component
public class NoPayNotifyOrderJob {

    @Resource
    private IOrderService orderService;

    @Resource
    private AlipayClient alipayClient;

    @Scheduled(cron = "0/3 * * * * ?")
    public void execute() {
//        log.info("定时任务: 处理未接收到的支付回调通知");
        List<String> orderIds = orderService.queryNoPayNotifyOrder();
        if (null != orderIds && !orderIds.isEmpty()) {
            for (String orderId : orderIds) {
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                AlipayTradeQueryModel bizModel = new AlipayTradeQueryModel();
                bizModel.setOutTradeNo(orderId);
                request.setBizModel(bizModel);

                try {
                    AlipayTradeQueryResponse alipayTradeQueryResponse = alipayClient.execute(request);
                    String code = alipayTradeQueryResponse.getCode();
                    // 判断状态码
                    if ("10000".equals(code)) {
                        log.info("定时任务: 处理成功 - 未接收到的支付回调通知");
                        orderService.changeOrderPaySuccess(orderId);
                    }
                } catch (AlipayApiException e) {
                    log.error("定时任务: 处理失败 - 未接收到的支付回调通知", e);
                }
            }
        }
    }


}
