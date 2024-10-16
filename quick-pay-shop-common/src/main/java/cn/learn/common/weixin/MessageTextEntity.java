package cn.learn.common.weixin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * @program: quick-pay-shop
 * @description: 表示微信公众号消息实体的 Java 类
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

    @XStreamAlias("MsgID")
    private String msgId;

    @XStreamAlias("Status")
    private String status;

    @XStreamAlias("Ticket")
    private String ticket;

    @XStreamAlias("Content")
    private String content;

}
