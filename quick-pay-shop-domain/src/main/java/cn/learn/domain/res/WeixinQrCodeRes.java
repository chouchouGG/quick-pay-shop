package cn.learn.domain.res;

import lombok.Data;

/**
 * @program: quick-pay-shop
 * @description: 获取微信登录二维码时的响应对象
 * @author: chouchouGG
 * @create: 2024-10-15 22:27
 **/
@Data
public class WeixinQrCodeRes {

    /**
     * ticket经过浏览器渲染后就是二维码
     */
    private String ticket;

    private Long expire_seconds;

    private String url;

}

