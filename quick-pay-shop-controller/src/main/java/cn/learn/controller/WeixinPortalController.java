package cn.learn.controller;

import cn.learn.common.weixin.MessageTextEntity;
import cn.learn.common.weixin.SignatureUtil;
import cn.learn.common.weixin.XmlUtil;
import cn.learn.service.ILoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 微信服务对接：
 * <p>
 * 资源路径：<a href="http://chouchou1.natapp1.cc/api/v1/weixin/portal/receive">/api/v1/weixin/portal/receive</a>
 * <p>
 * 完整URL：http://chouchou1.natapp1.cc/api/v1/weixin/portal/receive/
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/weixin/portal/")
public class WeixinPortalController {

    @Value("${weixin.config.originalid}")
    private String originalid;
    @Value("${weixin.config.token}")
    private String token;

    @Resource
    private ILoginService loginService;

    /**
     * 接口作用：微信公众号平台服务器进行开发者服务器的 URL 验证时调用的接口。
     * @param signature 微信服务器发送的签名
     * @param timestamp 微信服务器发送的时间戳
     * @param nonce 微信服务器发送的随机数
     * @param echostr 微信服务器发送的随机字符串
     * @return 如果校验通过，则返回 echostr，确认服务器配置成功。
     */
    @GetMapping(value = "receive", produces = "text/plain;charset=utf-8")
    public String validate(@RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr) {
        try {
            log.info("微信公众号验签信息开始 [{}, {}, {}, {}]", signature, timestamp, nonce, echostr);
            if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
                throw new IllegalArgumentException("请求参数非法，请核实!");
            }
            boolean check = SignatureUtil.check(token, signature, timestamp, nonce);
            log.info("微信公众号验签信息完成 check：{}", check);
            if (!check) {
                log.info("签名校验不通过，不继续处理", check);
                return null;
            }
            return echostr;
        } catch (Exception e) {
            log.error("微信公众号验签信息失败 [{}, {}, {}, {}]", signature, timestamp, nonce, echostr, e);
            return null;
        }
    }

    /**
     * 接口作用：处理来自微信服务器的消息，解析出消息内容后进行回复。（在这个例子中，回复的内容是用户发送的文本消息的内容。）
     * @param requestBody 接收请求体的内容（即微信推送的消息）
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param openid <strong>用于唯一标识该用户在该公众号中的身份</strong>
     * @param encType 标识消息是否加密
     * @param msgSignature 如果消息加密，则这个参数包含加密的签名，用于验证消息的完整性
     * @return 调用 buildMessageTextEntity() 方法，生成一个回复消息。
     */
    @PostMapping(value = "receive", produces = "application/xml; charset=UTF-8")
    public String post(@RequestBody String requestBody,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("openid") String openid,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        try {
            log.info("接收微信公众号信息请求，openid:{} requestBody:{}", openid, requestBody);
            // 消息转换
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);
/*            // Jackson序列化对象
            ObjectMapper objectMapper = new ObjectMapper();
            log.info("message: {}", objectMapper.writeValueAsString(message));*/

            if ("event".equals(message.getMsgType()) && "SCAN".equals(message.getEvent())) {
                loginService.saveLoginState(message.getTicket(), openid);
//                return buildMessageTextEntity(openid, "登录成功");
            }

            return buildMessageTextEntity(openid, "你好，" + message.getContent());
        } catch (Exception e) {
            log.error("接收微信公众号信息请求{} 失败 {}", openid, requestBody, e);
            return "";
        }
    }

    // 构建返回给微信公众号的消息，消息格式为xml
    private String buildMessageTextEntity(String openid, String content) {
        MessageTextEntity res = new MessageTextEntity();
        // 公众号分配的ID
        res.setFromUserName(originalid);
        res.setToUserName(openid);
        res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
        res.setMsgType("text");
        res.setContent(content);
        return XmlUtil.beanToXml(res);
    }

}
