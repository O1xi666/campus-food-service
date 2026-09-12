package com.sky.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 站内通知 WebSocket 端点。
 *
 * 连接地址：ws://host:8080/ws/notify/{userId}
 * 服务端在异步任务完成 / 订单异步落库成功时主动推送消息，前端不用轮询。
 *
 * 注意：Tomcat 会为每个连接新建端点实例，所以状态必须放在 static 的 SESSIONS 里，
 * 不能用普通的实例字段（也用不到 Spring 注入，因此不存在 @Autowired 失效的问题）。
 */
@ServerEndpoint("/ws/notify/{userId}")
@Component
@Slf4j
public class NotificationWebSocketServer {

    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        SESSIONS.put(userId, session);
        log.info("站内通知连接建立 - userId: {}, 当前在线: {}", userId, SESSIONS.size());
    }

    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        SESSIONS.remove(userId);
        log.info("站内通知连接关闭 - userId: {}, 当前在线: {}", userId, SESSIONS.size());
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") Long userId) {
        SESSIONS.remove(userId);
        log.warn("站内通知连接异常 - userId: {}", userId, error);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") Long userId) {
        log.debug("收到客户端消息 - userId: {}, message: {}", userId, message);
        sendToUser(userId, "PONG", "心跳响应", message);
    }

    /** 给指定用户推送一条站内通知；用户不在线时返回 false，消息只落日志 */
    public static boolean sendToUser(Long userId, String type, String title, String content) {
        Session session = userId == null ? null : SESSIONS.get(userId);
        if (session == null || !session.isOpen()) {
            log.info("用户 {} 当前不在线，站内通知未推送 - [{}] {}", userId, type, title);
            return false;
        }
        try {
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("type", type);
            payload.put("title", title);
            payload.put("content", content);
            payload.put("time", LocalDateTime.now().format(TIME_FORMAT));
            session.getBasicRemote().sendText(MAPPER.writeValueAsString(payload));
            log.info("站内通知已推送 - userId: {}, title: {}", userId, title);
            return true;
        } catch (IOException e) {
            log.warn("站内通知推送失败 - userId: {}", userId, e);
            return false;
        }
    }

    public static int onlineCount() {
        return SESSIONS.size();
    }
}