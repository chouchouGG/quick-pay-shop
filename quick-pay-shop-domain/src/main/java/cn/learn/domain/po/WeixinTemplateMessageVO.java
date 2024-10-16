package cn.learn.domain.po;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: quick-pay-shop
 * @description: 封装微信模板消息的相关信息
 * @author: chouchouGG
 * @create: 2024-10-15 22:51
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeixinTemplateMessageVO {

    @JsonProperty("touser")
    private String toUser;
    @JsonProperty("template_id")
    private String templateId;
    /**
     * 微信服务器会将是否送达成功作为通知，发送到开发者在开发模式中填写的 URL 中。
     * URL置空，则在发送后，点击模板消息会进入一个空白页面（ios），或无法点击（android）。
     */
    @JsonProperty("url")
    private String url;
    /**
     * 嵌套的 Map，用于存储模板消息的内容
     */
    @JsonProperty("data")
    private Map<String, Map<String, String>> data;

    public static void put(Map<String, Map<String, String>> data, TemplateKey key, String value) {
        data.put(key.getCode(), new HashMap<String, String>() {
            private static final long serialVersionUID = 7092338402387318563L;

            {
                put("value", value);
            }
        });
    }

    // 后续需要给模板消息增添属性，为了规范性可以在 TemplateKey 中给出。
    @Getter
    @AllArgsConstructor
    public enum TemplateKey {
        USER("user","用户ID")
        ;

        private String code;
        private String desc;
    }

}

