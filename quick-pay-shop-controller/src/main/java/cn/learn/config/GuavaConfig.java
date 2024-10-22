package cn.learn.config;

import cn.learn.listener.OrderPaySuccessListener;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @program: quick-pay-shop
 * @description: 配置类用于创建两个缓存 Bean
 * @author: chouchouGG
 * @create: 2024-10-15 23:03
 **/
@Configuration
public class GuavaConfig {

    // note：Guava框架。Guava 提供了一个轻量级的内存缓存库，属于本地缓存。
    //  通过 CacheBuilder 可以方便地构建本地缓存。后续可以考虑使用 redis 进行缓存。

    /**
     * 用于存储微信的 AccessToken，缓存中存储的值在写入后的 2 小时内有效。
     */
    @Bean("weixinAccessTokenCache")
    public Cache<String, String> weixinAccessTokenCache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.HOURS)
                .build();
    }

    /**
     * 用于存储 OpenID Token，缓存中的值在写入后的 1 小时内有效。
     */
    @Bean("openidTokenCache")
    public Cache<String, String> openidTokenCache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    @Bean("eventBusListener")
    public EventBus eventBusListener(OrderPaySuccessListener listener) {
        EventBus eventBus = new EventBus();
        eventBus.register(listener);
        return eventBus;
    }

}
