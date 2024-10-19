package cn.learn.test.service;

import cn.learn.domain.po.PayOrder;
import cn.learn.domain.req.ShopCartReq;
import cn.learn.domain.res.PayOrderRes;
import cn.learn.service.IOrderService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderServiceTest {

    @Resource
    private IOrderService orderService;

    @Test
    public void test() throws Exception {
        ShopCartReq shopCartReq = ShopCartReq.builder().userId("joyboy").productId("0").build();
        PayOrderRes order = orderService.createOrder(shopCartReq);
        log.info("请求参数：{}", JSON.toJSONString(shopCartReq));
        log.info("测试结果：{}", JSON.toJSONString(order));
    }

}
