package cn.learn.job;

import cn.learn.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: quick-pay-shop
 * @description: 超时订单关闭
 * @author: chouchouGG
 * @create: 2024-10-19 19:51
 **/
@Slf4j
@Component
public class TimeoutCloseOrderJob {

    @Resource
    private IOrderService orderService;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void execute() {
        try {
//            log.info("定时任务: 处理超时 15 分钟订单的关闭");
            List<String> orderIds = orderService.queryTimeoutCloseOrderList();
            if (null == orderIds || orderIds.isEmpty()) {
                return;
            }
            for (String orderId : orderIds) {
                /**
                 * todo 这里只是做了商户系统层面的订单关闭，还没有支付宝层面进行交易关闭。
                 *  后续交易关闭可以参考链接： https://opendocs.alipay.com/open/8dc9ebb3_alipay.trade.close?scene=common&pathHash=0c042d2b
                 */
                boolean status = orderService.changeOrderClose(orderId);
                log.info("定时任务: 处理成功 - 超时 15 分钟订单关闭 orderId: {} status：{}", orderId, status);
            }
        } catch (Exception e) {
            log.error("定时任务: 处理失败 - 超时 15 分钟订单关闭失败", e);
        }
    }

}
