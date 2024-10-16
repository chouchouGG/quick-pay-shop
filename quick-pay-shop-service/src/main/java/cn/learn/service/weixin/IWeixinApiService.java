package cn.learn.service.weixin;

import cn.learn.domain.po.WeixinTemplateMessageVO;
import cn.learn.domain.req.WeixinQrCodeReq;
import cn.learn.domain.res.WeixinQrCodeRes;
import cn.learn.domain.res.WeixinTokenRes;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @program: quick-pay-shop
 * @description: 微信API服务接口
 * @author: chouchouGG
 * @create: 2024-10-15 22:01
 **/
public interface IWeixinApiService {

    /**
     *
     * <p>获取 Access token</p>
     * <p>文档：<a href="https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/Get_access_token.html">获取 Access token</a></p>
     *
     * @param grantType 获取access_token填写client_credential
     * @param appId 第三方用户唯一凭证
     * @param appSecret 第三方用户唯一凭证密钥，即appsecret
     * @return 响应结果
     */
    @GET("cgi-bin/token")
    Call<WeixinTokenRes> getAccessToken(@Query("grant_type") String grantType,
                                  @Query("appid") String appId,
                                  @Query("secret") String appSecret);

    /**
     * <p>获取凭据 ticket</p>
     * <p>文档：<a href="https://developers.weixin.qq.com/doc/offiaccount/Account_Management/Generating_a_Parametric_QR_Code.html">生成带参数的二维码</a></p>
     *
     * @param accessToken getToken 获取的 token 信息
     * @param weixinQrCodeReq 入参对象
     * @return 响应结果
     */
    @POST("cgi-bin/qrcode/create")
    Call<WeixinQrCodeRes> createQrCode(@Query("access_token") String accessToken,
                                       @Body WeixinQrCodeReq weixinQrCodeReq);

    /**
     * <p>发送微信公众号模板消息</p>
     * <p>文档：<a href="https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Template_Message_Interface.html#%E5%8F%91%E9%80%81%E6%A8%A1%E6%9D%BF%E6%B6%88%E6%81%AF:~:text=%22ok%22%0A%7D-,%E5%8F%91%E9%80%81%E6%A8%A1%E6%9D%BF%E6%B6%88%E6%81%AF,-%E6%8E%A5%E5%8F%A3%E8%B0%83%E7%94%A8">发送模板消息</a></p>
     *
     * @param accessToken              getToken 获取的 token 信息
     * @param weixinTemplateMessageVO 入参对象
     * @return 应答结果
     */
    @POST("cgi-bin/message/template/send")
    Call<Void> sendMessage(@Query("access_token") String accessToken,
                           @Body WeixinTemplateMessageVO weixinTemplateMessageVO);

}
