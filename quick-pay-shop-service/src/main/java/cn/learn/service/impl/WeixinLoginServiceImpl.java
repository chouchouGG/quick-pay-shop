package cn.learn.service.impl;

import cn.learn.domain.vo.WeixinTemplateMessageVO;
import cn.learn.domain.req.WeixinQrCodeReq;
import cn.learn.domain.res.WeixinQrCodeRes;
import cn.learn.domain.res.WeixinTokenRes;
import cn.learn.service.ILoginService;
import cn.learn.service.weixin.IWeixinApiService;
import com.google.common.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: quick-pay-shop
 * @description:
 * @author: chouchouGG
 * @create: 2024-10-16 13:04
 **/
@Slf4j
@Service
public class WeixinLoginServiceImpl implements ILoginService {

    @Value("${weixin.config.app-id}")
    private String appId;
    @Value("${weixin.config.app-secret}")
    private String appSecret;
    @Value("${weixin.config.template-id}")
    private String templateId;

    @Resource
    private Cache<String, String> weixinAccessTokenCache;
    @Resource
    private Cache<String, String> openidTokenCache;
    @Resource
    private IWeixinApiService weixinApiService;

    @Override
    public String createQrCodeTicket() throws Exception {
        // 1. 获取 Access Token
        String accessToken = fentchAccessToken();

        // 2. 生成 Ticket
        WeixinQrCodeReq weixinQrCodeReq = WeixinQrCodeReq.builder()
                .expire_seconds(2592000) // 2592000是临时二维码最长可以设置的值。
                .action_name(WeixinQrCodeReq.ActionNameTypeVO.QR_SCENE.getCode())
                .action_info(WeixinQrCodeReq.ActionInfo.builder()
                        .scene(WeixinQrCodeReq.ActionInfo.Scene.builder()
                                .scene_id(100601) // 场景值ID，临时二维码时为 32位非 0整型
                                .build())
                        .build())
                .build();

        // 3. 获取 QR Code
        Call<WeixinQrCodeRes> call2 = weixinApiService.createQrCode(accessToken, weixinQrCodeReq);
        WeixinQrCodeRes weixinQrCodeRes = call2.execute().body();
        assert weixinQrCodeRes != null;
        return weixinQrCodeRes.getTicket();
    }

    @Override
    public String checkLogin(String ticket) {
        return openidTokenCache.getIfPresent(ticket);
    }

    @Override
    public void saveLoginState(String ticket, String openid) throws IOException {
        openidTokenCache.put(ticket, openid);

        // 获取 access token
        String accessToken = fentchAccessToken();

        // 发送模板消息（固定结构的消息体）
        log.info("初次登录，发送模板消息");
        Map<String, Map<String, String>> data = new HashMap<>();
        WeixinTemplateMessageVO.put(data, WeixinTemplateMessageVO.TemplateKey.USER, openid);

        WeixinTemplateMessageVO templateMessageDTO = WeixinTemplateMessageVO.builder()
                .toUser(openid)
                .templateId(templateId)
                .url("https://tieba.baidu.com")
                .data(data)
                .build();

        Call<Void> call = weixinApiService.sendMessage(accessToken, templateMessageDTO);
        call.execute();
    }

    private String fentchAccessToken() {
        String accessToken = weixinAccessTokenCache.getIfPresent(appId);
        if (null == accessToken) {
            try {
                Call<WeixinTokenRes> call1 = weixinApiService.getAccessToken("client_credential", appId, appSecret);
                Response<WeixinTokenRes> response = call1.execute();
                // 检查 HTTP 请求是否成功
                if (!response.isSuccessful()) {
                    throw new RuntimeException("未能获取微信的 Access Token, HTTP 错误码: " + response.code());
                }
                WeixinTokenRes weixinTokenRes = response.body();
                // 检查返回的 body 是否为 null
                if (weixinTokenRes == null) {
                    throw new NullPointerException("未能获取微信的 Access Token, HTTP 响应体为空");
                }
                // 获取并缓存 accessToken
                accessToken = weixinTokenRes.getAccessToken();
                weixinAccessTokenCache.put(appId, accessToken);
            } catch (IOException e) {
                throw new RuntimeException("在尝试通过微信API获取Access Token时发生错误。", e);
            }
        }
        return accessToken;
    }

}