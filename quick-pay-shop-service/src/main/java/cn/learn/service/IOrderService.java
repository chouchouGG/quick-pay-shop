package cn.learn.service;

import cn.learn.domain.req.ShopCartReq;
import cn.learn.domain.res.PayOrderRes;
import com.alipay.api.domain.Shop;

/**
 * @program: quick-pay-shop
 * @description: 订单服务接口
 * @author: chouchouGG
 * @create: 2024-10-18 22:23
 **/
public interface IOrderService {

    /**
     * 创建订单
     */
    PayOrderRes createOrder(ShopCartReq shopCartReq) throws Exception;


}
