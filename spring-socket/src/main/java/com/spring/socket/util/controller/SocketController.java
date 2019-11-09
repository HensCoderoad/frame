package com.spring.socket.util.controller;

import com.spring.socket.util.ResponseData;
import com.spring.socket.util.config.WebSocket;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
public class SocketController {
    @Resource
    WebSocket webSocket;

    @RequestMapping("sendToAll")
    public ResponseData sendToAll(){
        webSocket.sendMessage("服务端向客户群发消息！");
        return new ResponseData(200, "群发成功");
    }

    @RequestMapping("sendToOne")
    public ResponseData sendToOne(String sessionId) throws IOException {
     webSocket.sendMessage(sessionId, "你好");
     return new ResponseData(200, "发送成功", sessionId);
    }
}
