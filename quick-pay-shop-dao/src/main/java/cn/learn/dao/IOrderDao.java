package cn.learn.dao;

import cn.learn.domain.po.PayOrder;
import org.apache.ibatis.annotations.Mapper;

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
}
