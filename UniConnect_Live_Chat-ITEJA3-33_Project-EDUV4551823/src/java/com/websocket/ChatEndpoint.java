package com.websocket;

import com.beans.UserSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
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
    
    private static Connection conn;
    private static final String dbURL = "jdbc:mysql://localhost:3306/uniconnectdb?useSSL=false";
    private static final String dbUsername = "root";
    private static final String dbPassword = "DB4eca";
    private static final String dbDriver = "com.mysql.cj.jdbc.Driver";
    boolean successfullyConnected = false;
    
    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("===========ChatEndpoint=========\nNEW SESSION ADDED");
        try{//connects to database
            Class.forName(dbDriver).newInstance();
            conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
            successfullyConnected = true;
        }catch(ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e){ //error catch
            e.printStackTrace();
        }
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
        
        //Insert into database
        String insertMessage_SQL = "INSERT INTO uniconnectdb.messages (conver_id, sender_id, message_text, date_sent) VALUES ("+userSession.getGroupID()+", "+userSession.getUserID()+", "+message+", now())";
        try{
            PreparedStatement ps = conn.prepareStatement(insertMessage_SQL);
            System.out.println("PREPARED STATEMENT");
            ps.execute();
            System.out.println("RETRIEVED DATA");
            System.out.println("INSERTED MESSAGE: " + message);            
        }catch(SQLException e){
            e.printStackTrace();
        }
        
    }
    
    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
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
