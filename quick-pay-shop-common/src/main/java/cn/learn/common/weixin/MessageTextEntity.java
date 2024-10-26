package cn.learn.common.weixin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * @program: quick-pay-shop
 * @description: 与微信公众平台进行通信的消息实体
 * @author: chouchouGG
 * @create: 2024-10-15 12:29
 **/
@Setter
@Getter
@XStreamAlias("xml")
public class MessageTextEntity {

    // Getters and Setters
    @XStreamAlias("ToUserName")
    private String toUserName;

    @XStreamAlias("FromUserName")
    private String fromUserName;

    @XStreamAlias("CreateTime")
    private String createTime;

    @XStreamAlias("MsgType")
    private String msgType;

    @XStreamAlias("Event")
    private String event;

    @XStreamAlias("EventKey")
    private String eventKey;

    @XStreamAlias("MsgID")  // 微信扫码登录使用
    private String msgId;

    @XStreamAlias("MsgId")  // 普通消息使用
    private String msgIdAlias;

    @XStreamAlias("Status")
    private String status;

    @XStreamAlias("Ticket")
    private String ticket;

    @XStreamAlias("Content")
    private String content;

}
