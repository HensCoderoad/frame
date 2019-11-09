package com.spring.socket.util.config;

import org.jboss.logging.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


@Component
@ServerEndpoint(value = "/webSocket")
public class WebSocket {

    private static Logger logger = Logger.getLogger(WebSocket.class);

    private Session sesssion;

    private static CopyOnWriteArraySet<WebSocket> webSockets = new CopyOnWriteArraySet<WebSocket>();

    private Map<String, String> map = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session){
        String sessionUrl = session.getRequestURI().toString();
        String username = sessionUrl.substring(sessionUrl.indexOf("="), sessionUrl.length() ).substring(1);
        map.put(username, session.getId());
        System.out.println("以下是最新的遍历结果");
        map.keySet().forEach((a) -> {
            System.out.println("用户为" + a + "，用户id为" + map.get(a));
        });
        this.sesssion = session;
        webSockets.add(this);
        logger.info("webSocket有新的连接，总数：" + webSockets.size());
    }

    @OnClose
    public void onClose(){
        webSockets.remove(this);
        logger.info("webSocket断开连接，总数：" + webSockets.size());
    }

    @OnMessage
    public void onMessage(String message){
        logger.info("websocket发来新的消息" + message);
        sesssion.getAsyncRemote().sendText(message);
    }
    @OnError
    public void onError(Session sesssion, Throwable e){
        logger.error("发生错误：" + e.getMessage(), e);
        e.printStackTrace();
    }

    public void sendMessage(String message){
        for (WebSocket websocket : webSockets){
            logger.info("websocket广播消息："+ message);
            try {
                websocket.sesssion.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String sessionId, String message) throws IOException {
        Session session = null;
        WebSocket tempWebSocket = null;
        for (WebSocket webSocket:webSockets) {
            if (webSocket.sesssion.getId().equals(sessionId)) {
                tempWebSocket = webSocket;
                session = webSocket.sesssion;
                break;
            }
        }
        if(session != null){
        tempWebSocket.sesssion.getBasicRemote().sendText(message);
        }else {
        logger.warn("没有找到指定的会话id:" + sessionId);
        }
    }
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }
}
