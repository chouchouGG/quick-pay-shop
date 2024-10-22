package cn.learn.listener;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @program: quick-pay-shop
 * @description:
 * @author: chouchouGG
 * @create: 2024-10-19 14:41
 **/
@Slf4j
@Component
public class OrderPaySuccessListener {

    @Subscribe
    public void handleEvent(String paySuccessMessage) {
        log.info("收到支付成功消息（后续扩展可以进行：发货、充值、开会员...）");
    }
}
