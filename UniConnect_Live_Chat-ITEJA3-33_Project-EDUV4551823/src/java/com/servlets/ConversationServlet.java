package com.servlets;

import com.beans.UserSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  This servlet loads the user's messages and group chats. Ideally it would 
 *  have loaded on load, but I could not find a way to do so, so the user must 
 *  click a button to load messages
 * 
 * @author Nicholas Leong EDUV4551823
 */
@WebServlet("/Conversations")
public class ConversationServlet extends HttpServlet {
    @Inject private UserSession userSession;
    
    private static Connection conn;
    private static final String dbURL = "jdbc:mysql://localhost:3306/uniconnectdb?useSSL=false";
    private static final String dbUsername = "root";
    private static final String dbPassword = "DB4eca";
    private static final String dbDriver = "com.mysql.cj.jdbc.Driver";
    
    LinkedList<String> conversationToDisplay = new LinkedList<>();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        System.out.println("================Conversation=======================");//used to separate and debug web application
        
        boolean successfullyConnected = false;  //flag variable for storing if the web app successfully connected to database
        
        try{//connects to database
            Class.forName(dbDriver).newInstance();
            conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
            successfullyConnected = true;
        }catch(ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e){ //error catch
            processRequest(request, response, "01) Error in retrieving messages. Please try again." + e.getMessage());//refreshes page to let user try again
            System.out.println("ERROR: " + e);
            closeConnection(conn);
        }finally{//perform SELECT function
            if (successfullyConnected){
                if (retrieveChats()){
                    closeConnection(conn);
                    System.out.println("SUCCESSFULLY CONNECTED AND RETRIEVED CHATS");
                    processRequest(request, response);
                }else{
                    System.out.println("ERROR IN RETRIEVING MESSAGES");
                    processRequest(request, response, "Error in retrieving chats. Please try again.");//refreshes page to let user try again
                    closeConnection(conn);
                }
            }else{
                processRequest(request, response, "02) Error in retrieving messages. Please try again.");//refreshes page to let user try again
            }
        }
    }
    
    //==========================================================================
    
    /*
    1) Retrieves conversation_ids and stores them in "conversationIDs". 
        These will be used to locate the various chats that the user is a part of.
    2) Retrieves is_group_chat to determine if the chat is a group chat
        2.1) If it is not, then it will retrieve the username of the other user that will be displayed
        2.2) If it is , then it will store the group name as what will be displayed.
    3) Retrieves the last message in the chat
    4) Builds and stores all details in an html string that will display the available chats to the user.
    */
    
    private boolean retrieveChats(){
        try{
            //1)
            System.out.println("userSession.UserID: " + userSession.getUserID());
            String getChatsSQL = "SELECT conversation_id FROM uniconnectdb.conversation_users WHERE (user_id =  "+userSession.getUserID()+")";
            
            PreparedStatement ps = conn.prepareStatement(getChatsSQL);
            System.out.println("PREPARED STATEMENT");
            ResultSet rs = ps.executeQuery();//executes SELECT query and retrieves data 
            System.out.println("RETRIEVED DATA");
            
            /*Retrieves conversation ids to identify chat names later*/
            LinkedList<Integer> conversationIDs = new LinkedList<Integer>();
            int tempConversationID = 0;
            int count = 0;
            while (rs.next()){
                tempConversationID = rs.getInt("conversation_id");
                conversationIDs.add(tempConversationID);
                System.out.println("ADDED CHAT ID: " + tempConversationID);
                count++;
            }
            System.out.println("NUMBER OF CHATS: " + count);
            
            /*2) Retrieves conversation names*/
            LinkedList<String> conversationNames = new LinkedList<String>();
            String getIsGroupChat_SQL = "";
            String tempConversationName = "";
            int tempIsGroupChat = 0;
            for (int i = 0; i < count; i++){//loops through retrieved chats - 5 chats exist, will loop 5 times
                getIsGroupChat_SQL = "SELECT is_group_chat FROM uniconnectdb.conversations WHERE (conversation_id = "+conversationIDs.get(i)+")";
                ps = conn.prepareStatement(getIsGroupChat_SQL);
                System.out.println("PREPARED STATEMENT: getIsGroupChat_SQL: " + i);
                rs = ps.executeQuery();
                System.out.println("RETRIEVED DATA: " + i);
                
                while (rs.next()){//retrieves is_group_chat
                    //2.1)
                    tempIsGroupChat = rs.getInt("is_group_chat");
                    
                    if (tempIsGroupChat == 0){//if chat is NOT a group chat
                        String getOtherUsername_SQL = "SELECT user_id FROM uniconnectdb.conversation_users WHERE (conversation_id = "+conversationIDs.get(i)+" AND NOT user_id = "+userSession.getUserID()+")";
                        PreparedStatement ps2 = conn.prepareStatement(getOtherUsername_SQL);
                        System.out.println("PREPARED STATEMENT: getOtherUsername_SQL 1");
                        ResultSet rs2 = ps2.executeQuery();//executes SELECT query and retrieves data 
                        System.out.println("RETRIEVED DATA: getOtherUsername_SQL 1");
                        rs2.next();
                        int otherUser_id = rs2.getInt("user_id");
                        
                        getOtherUsername_SQL = "SELECT username FROM uniconnectdb.users WHERE user_id = "+otherUser_id+"";
                        ps2 = conn.prepareStatement(getOtherUsername_SQL);
                        System.out.println("PREPARED STATEMENT: getOtherUsername_SQL 2");
                        rs2 = ps2.executeQuery();//executes SELECT query and retrieves data 
                        System.out.println("RETRIEVED DATA: getOtherUsername_SQL 2");
                        rs2.next();
                        tempConversationName = rs2.getString("username");//Conversation Name is now the other user's username
                        System.out.println("RETRIEVED OTHER USERNAME: " + tempConversationName);
                        conversationNames.add(tempConversationName);
                        System.out.println("conversationNames SIZE: " + conversationNames.size());
                        
                    }else if (tempIsGroupChat == 1){//if chat is a group chat
                        //2.2)                        
                        String getGroupName = "SELECT group_name FROM uniconnectdb.conversations WHERE conversation_id = "+conversationIDs.get(i)+"";
                        PreparedStatement ps2 = conn.prepareStatement(getGroupName);
                        System.out.println("PREPARED STATEMENT: getGroupName");
                        ResultSet rs2 = ps2.executeQuery();//executes SELECT query and retrieves data 
                        System.out.println("RETRIEVED DATA: getGroupName");
                        rs2.next();
                        tempConversationName = rs2.getString("group_name");
                        System.out.println("RETRIEVED GROUP NAME: " + tempConversationName);
                        conversationNames.add(tempConversationName);
                        System.out.println("conversationNames SIZE: " + conversationNames.size());
                    }
                }
                
                //3)
                ArrayList<String> conversationLastMessages = new ArrayList<String>();//retrieves last conversation message
                String getLastMessage_SQL = "SELECT message_id, message_text FROM uniconnectdb.messages WHERE (conver_id = "+conversationIDs.get(i)+") ORDER BY message_id DESC LIMIT 1";
                String tempLastMessage = "";
                ps = conn.prepareStatement(getLastMessage_SQL);
                rs = ps.executeQuery(getLastMessage_SQL);
                
                /*
                The following code is supposed to add the last message to the end of the ArrayList "conversationLastMessages"
                however, it keeps replacing the first object in the Array List. It also does not work with a LinkedList
                
                rs.next();
                tempLastMessage = rs.getString("message_text");
                System.out.println("RETRIEVED LAST MESSAGE: " + tempLastMessage);
                conversationLastMessages.add(tempLastMessage);
                System.out.println("conversationLastMessages ELEMENTS: " + conversationLastMessages);
                System.out.println("conversationLastMessages SIZE: " + conversationLastMessages.size() + "\n================================\n");
                
                The while loop underneath also does not work - it presents the same error.
                */
                
                while (rs.next()){
                    tempLastMessage = rs.getString("message_text");
                    System.out.println("RETRIEVED LAST MESSAGE: " + tempLastMessage);
                    if (conversationLastMessages.size() == 1 || conversationLastMessages.isEmpty()){
                        conversationLastMessages.add(tempLastMessage);
                    }else if (conversationLastMessages.size() > 1){
                        conversationLastMessages.add(conversationLastMessages.size() + 1, tempLastMessage);
                    }
                    System.out.println("conversationLastMessages ELEMENTS: " + conversationLastMessages);
                    System.out.println("conversationLastMessages SIZE: " + conversationLastMessages.size() + "\n================================\n");
                }
                //i.e. at conversationIDs[0], conversationNames[0], and conversationLastMessages[0] all refer to a single chat's details
                
                //4)
                String tempDisplayConversation = 
                        "<div class = 'conversation'>\n" +
                            "<form action = 'ChatServlet' method='GET'>\n" +
                                "<label class = 'heading'>"+conversationNames.get(i)+"</label><br />\n" +
                                "<input type = 'text' id = 'groupID' name = 'groupID' value = '"+conversationIDs.get(i)+"' hidden/>\n" +
                                "<label class = 'lastMessage'>"+conversationLastMessages.get(i)+"</label><br />\n" +
                                "<input class = 'goButton' type = 'submit' value = 'Go to'>\n" +
                            "</form>\n" +
                        "</div>\n";
                conversationToDisplay.add(tempDisplayConversation);
            }
            
            return true;
        }catch(SQLException e){
            System.out.println("SQL ERROR: " + e);
            return false;
        }
    }
    
    //==========================================================================
    
    private void closeConnection(Connection conn){//closes database connection
        try {
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    //==========================================================================
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, String message)
        throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>\n" +
                        "<!--Nicholas Leong EDUV4551823-->\n" +
                        "<!--This page shows the conversations that the user has currently-->\n" +
                        "<html>\n" +
                        "    <head>\n" +
                        "        <title>Conversations</title>\n" +
                        "        <meta charset='UTF-8'>\n" +
                        "        <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                        "        <link rel = 'stylesheet' href = 'Header_And_Footer.css'>\n" +
                        "        <link rel = 'stylesheet' href = 'Conversations.css'>\n" +
                        "        <link rel='icon' href='data:,'>\n" +
                        "        <script>alert('"+message+"');</script>\n" +
                        "    </head>\n" +
                        "    <body>\n" +
                        "        <header>\n" +
                        "            <img src = 'Pics/UniConnectLogo.png' alt = 'UniConnect Logo' id = 'logo' onclick = 'window.open('Homepage.html', '_self')'>\n" +
                        "            <div id = 'message_call'>\n" +
                        "                <label id = 'message' onclick = 'window.open('Conversations.html', '_self')'>Messages</label>\n" +
                        "                <label id = 'videocall' onclick = 'window.open('VideoCall.html', '_self')'>Video Call</label>\n" +
                        "            </div>\n" +
                        "            <div id = 'account'>\n" +
                        "                <label id = 'user' onclick = 'window.open('index.html', '_self')'>Log Out</label>\n" +
                        "            </div>\n" +
                        "        </header>\n" +
                        "        \n" +
                        "        <main>\n" +
                        "            <script type = \"text/javascript\" src = \"Chat.js\"></script>\n" +
                        "            <form action = \"ConversationsServlet\" method = \"GET\" id = \"getCoversationsForm\">\n" +
                        "                <input type = \"submit\" id = \"getCoversations\" value = \"Get Conversations\"></input>\n" +
                        "            </form>\n" +
                        "        </main>\n" +
                        "    </body>\n" +
                        "</html>\n" +
                        "");
        }
    }
    
    //==========================================================================
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        StringBuilder sb = new StringBuilder();
        for (String s : conversationToDisplay) {
            sb.append(s);
        }
        String display = sb.toString();
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>\n" +
                        "<!--Nicholas Leong EDUV4551823-->\n" +
                        "<!--This page shows the conversations that the user has currently-->\n" +
                        "<html>\n" +
                        "    <head>\n" +
                        "        <title>Conversations</title>\n" +
                        "        <meta charset='UTF-8'>\n" +
                        "        <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                        "        <link rel = 'stylesheet' href = 'Header_And_Footer.css'>\n" +
                        "        <link rel = 'stylesheet' href = 'Conversations.css'>\n" +
                        "        <link rel='icon' href='data:,'>\n" +
                        "    </head>\n" +
                        "    <body>\n" +
                        "        <header>\n" +
                        "            <img src = 'Pics/UniConnectLogo.png' alt = 'UniConnect Logo' id = 'logo' onclick = 'window.open('Homepage.html', '_self')'>\n" +
                        "            <div id = 'message_call'>\n" +
                        "                <label id = 'message' onclick = 'window.open('Conversations.html', '_self')'>Messages</label>\n" +
                        "                <label id = 'videocall' onclick = 'window.open('VideoCall.html', '_self')'>Video Call</label>\n" +
                        "            </div>\n" +
                        "            <div id = 'account'>\n" +
                        "                <label id = 'user' onclick = 'window.open('index.html', '_self')'>Log Out</label>\n" +
                        "            </div>\n" +
                        "        </header>\n" +
                        "        \n" +
                        "        <main>\n" +
                                    display +
                        "        </main>\n" +
                        "    </body>\n" +
                        "</html>\n" +
                        "");
        }
    }
}
