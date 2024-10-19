package cn.learn.domain.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: quick-pay-shop
 * @description: 创建订单的 req对象
 * @author: chouchouGG
 * @create: 2024-10-18 22:27
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopCartReq {

    private String userId;

    private String productId;

}
