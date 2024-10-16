package cn.learn.controller;

import cn.learn.common.constants.Constants;
import cn.learn.common.response.Response;
import cn.learn.service.ILoginService;
import com.alipay.service.schema.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @program: quick-pay-shop
 * @description:
 * @author: chouchouGG
 * @create: 2024-10-16 15:02
 **/
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/login/")
public class LoginController {

    @Resource
    private ILoginService loginService;

    /**
     * http://chouchou1.natapp1.cc/api/v1/login/weixin_qrcode_ticket
     */
    @GetMapping(value = "weixin_qrcode_ticket")
    public Response<String> weixinQrCodeTicket() {
        try {
            String qrCodeTicket = loginService.createQrCodeTicket();
            log.info("生成微信扫码登录 ticket:{}", qrCodeTicket);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(qrCodeTicket)
                    .build();
        } catch (Exception e) {
            log.error("生成微信扫码登录 ticket 失败", e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * http://chouchou1.natapp1.cc/api/v1/login/check_login
     */
    @GetMapping("check_login")
    public Response<String> checkLogin(@RequestParam("ticket") String ticket) {
        try {
            String openid = loginService.checkLogin(ticket);
            log.info("扫码检测登录结果 ticket:{} openid:{}", ticket, openid);
            if (StringUtils.isNotBlank(openid)) {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.SUCCESS.getCode())
                        .info(Constants.ResponseCode.SUCCESS.getInfo())
                        .data(openid)
                        .build();
            } else {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.NO_LOGIN.getCode())
                        .info(Constants.ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }
        } catch (Exception e) {
            log.error("扫码检测登录结果失败 ticket:{}", ticket, e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }




}
