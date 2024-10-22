package cn.learn.dao;

import cn.learn.domain.po.PayOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @program: quick-pay-shop
 * @description:
 * @author: chouchouGG
 * @create: 2024-10-18 22:12
 **/
@Mapper
public interface IOrderDao {

    void insertOrder(PayOrder payOrder);

    PayOrder queryUnPayOrder(PayOrder payOrder);

    void updateOrderPayInfo(PayOrder payOrder);

    void changeOrderPaySuccess(PayOrder order);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);

}
