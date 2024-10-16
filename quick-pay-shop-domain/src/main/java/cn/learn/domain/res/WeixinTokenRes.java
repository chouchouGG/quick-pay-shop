package cn.learn.domain.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @program: quick-pay-shop
 * @description:
 * @author: chouchouGG
 * @create: 2024-10-15 22:04
 **/
@Data
public class WeixinTokenRes {

    // note：@JsonProperty：用于映射 JSON 中的字段与 Java 对象的属性。

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    private String errcode;

    private String errmsg;
}
