package cse308.map.websocket;

import org.springframework.stereotype.Component;

/**
 * 接收前台用户信息类
 * @author liangxifeng 2018-07-07
 */
@Component
public class MessageInfo {
    String msgContent;

    public String getMsgContent() {
        return this.msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;

    }
    public String toString(){
        return msgContent;
    }
}
