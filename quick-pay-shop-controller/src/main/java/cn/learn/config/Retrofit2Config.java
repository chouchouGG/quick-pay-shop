package cn.learn.config;

import cn.learn.service.weixin.IWeixinApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @program: quick-pay-shop
 * @description:
 * @author: chouchouGG
 * @create: 2024-10-15 23:17
 **/
@Slf4j
@Configuration
public class Retrofit2Config {

    /**
     * 定义了一个 url的前缀。
     */
    private static final String BASE_URL = "https://api.weixin.qq.com/";

    /**
     * 创建 Retrofit 对象.
     */
    @Bean
    public Retrofit retrofit() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL) // 指定 baseUrl 的值，与具体接口中的路径形成完整的 url地址。
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }

    /**
     * 使用 Retrofit对象创建「请求对象 weixinApiService」，之后就可以使用 weixinApiService 实例来进行网络请求。
     */
    @Bean
    public IWeixinApiService weixinApiService(Retrofit retrofit) {
        return retrofit.create(IWeixinApiService.class);
    }

}
