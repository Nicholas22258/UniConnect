package com.websocket;

import com.beans.UserSession;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.inject.Inject;
import javax.websocket.OnMessage;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;

/**
 *  Serves as the web socket server endpoint
 * @author Nicholas Leong EDUV4551823
 */
@ServerEndpoint("/chatEndpoint")
public class ChatEndpoint {
    @Inject private UserSession userSession;
    
    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("NEW SESSION ADDED");
    }
    
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        String username = userSession.getUsername();
        String fullMessage = "<div class = 'messages'>\n" +
                            "   <label class = 'messageSender' id = 'messageSender'>"+username+"</label><br>\n" +
                            "   <label class = 'messageText' id = 'messageText'>"+message+"</label>\n" +
                            " </div><br><hr /><br>";
        broadcast(fullMessage);
        System.out.println(fullMessage);
    }
    
    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WEB SOCKET ERROR: " + throwable.toString());
    }
    
    private void broadcast(String message) throws IOException{
        for (Session s : sessions){
            if (s.isOpen()){
                s.getBasicRemote().sendText(message);
            }
        }
    }
}
