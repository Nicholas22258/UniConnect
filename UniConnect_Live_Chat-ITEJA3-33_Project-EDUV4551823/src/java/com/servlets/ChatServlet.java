package com.servlets;

import com.beans.UserSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  Loads the chat
 * @author Nicholas Leong EDUV4551823
 */
@WebServlet("/chatServlet")
public class ChatServlet extends HttpServlet {
    @Inject private UserSession userSession;
    
    /*Database connection data*/
    private static Connection conn;
    private static final String dbURL = "jdbc:mysql://localhost:3306/uniconnectdb?useSSL=false";
    private static final String dbUsername = "root";
    private static final String dbPassword = "DB4eca";
    private static final String dbDriver = "com.mysql.cj.jdbc.Driver";
    
    /*Will store the messages from the selected chat/group chat*/
    private static String chat;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        System.out.println("==============Chat================================");//used to separate and debug web application
        
        /*flag variable for storing if the web app successfully connected to database*/
        boolean successfullyConnected = false;  
        
        /*Attempts connection to database*/
        try{//connects to database
            Class.forName(dbDriver).newInstance();
            conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
            successfullyConnected = true;
        }catch(ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e){ //error catch
            processRequest(request, response, "01) Error in connecting to database. Please try again." + e.getMessage());
            RequestDispatcher rd = request.getRequestDispatcher("/Conversations.html");//refreshes page to let user try again
            rd.forward(request, response);
            closeConnection(conn);
        }finally{//perform SELECT function
            String groupID = request.getParameter("groupID").trim();
            if (successfullyConnected){
                if (getChat(groupID)){
                    closeConnection(conn);
                    System.out.println("SUCCESSFULLY CONNECTED AND RETRIEVED CHAT");
                    processRequest(request, response);
                }else{
                    System.out.println("ERROR IN RETRIEVING CHAT");
                    processRequest(request, response, "Error in retrieving chat. Please try again.");
                    closeConnection(conn);
                }
            }else{
                processRequest(request, response, "02) Error in connecting to database. Please try again.");//refreshes page to let user try again
            }
        }
    }

    //==========================================================================
    
    /*Retrieves messages and message data from database and builds the conversation*/    
    private boolean getChat(String groupID){
        String getID_SQL = "SELECT DISTINCT sender_id FROM uniconnectdb.messages WHERE (messages.conver_id = "+groupID+")";//gets all user id's to get usernames later
        String getUsernames_SQL = "";
        try{
            Statement st = conn.createStatement();
            System.out.println("PREPARED STATEMENT");
            ResultSet rs = st.executeQuery(getID_SQL);//executes SELECT query and retrieves data 
            System.out.println("RETRIEVED DATA");
            
            LinkedList<String> userIDs = new LinkedList<>();
            LinkedList<String> usernames = new LinkedList<>();
            
            int userIDTemp;
            while(rs.next()){//retrieves user_ids that are in this group vhat
                userIDTemp = rs.getInt("sender_id");
                userIDs.add(Integer.toString(userIDTemp));
                System.out.println("USER ID: " + userIDTemp);
            }
            
            String usernameTemp;
            for (int i = 0; i < userIDs.size(); i++){//retrieves usernames with associated user_id's
                System.out.println("AT userIDs.get("+i+"): " + userIDs.get(i));
                getUsernames_SQL = "SELECT username FROM uniconnectdb.users WHERE (user_id = "+userIDs.get(i)+")";
                rs = st.executeQuery(getUsernames_SQL);//executes SELECT query and retrieves data 
                System.out.println("RETRIEVED DATA: i = " + i);
                rs.next();
                usernameTemp = rs.getString("username");
                System.out.println("SET usernameTemp: " + usernameTemp);
                usernames.add(usernameTemp);
                System.out.println("USERNAME: " + usernameTemp);
            }
            
            String getMessages_SQL = "SELECT sender_id, message_text FROM uniconnectdb.messages WHERE (conver_id = "+groupID+")";
            rs = st.executeQuery(getMessages_SQL);//executes SELECT query and retrieves data 
            System.out.println("RETRIEVED DATA: MESSAGES");
            String tempUserID = "";
            String tempMessage = "";
            String tempUsername = "";
            StringBuilder sb = new StringBuilder();//builds html string that will be printed out
            while (rs.next()){
                tempUserID = Integer.toString(rs.getInt("sender_id"));
                tempMessage = rs.getString("message_text");
                System.out.println(tempUserID + ": " + tempMessage);
                for (int j = 0; j < userIDs.size(); j++){//matches user_id with usernames
                    if (tempUserID.equals(userIDs.get(j))){
                        tempUsername = usernames.get(j);
                        sb.append("<div class = 'messages'>\n" +
                                    "<label class = 'messageSender' id = 'messageSender'>"+tempUsername+"</label><br>\n" +
                                    "<label class = 'messageText' id = 'messageText'>"+tempMessage+"</label>\n" +
                                  "</div><br><hr /><br>\n");
                        break;
                    }
                }
            }
            chat = sb.toString();
            
            userSession.setgroupID(Integer.parseInt(groupID));
            
            return true;
        }catch(SQLException e){
            System.out.println("SQL ERROR: " + e);
            return false;
        }
        
    }
    
    //==========================================================================
    
    /*Function to close database connection*/
    private void closeConnection(Connection conn){//closes datbase connection
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(ChatServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //==========================================================================
    
    /*Outputs if there is an error*/
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>\n" +
                        "<!--Nicholas Leong EDUV4551823-->\n" +
                        "<!--This page shows the conversations that the user has currently, as well as will show the selected conversation-->\n" +
                        "<html>\n" +
                        "    <head>\n" +
                        "        <title>Conversations</title>\n" +
                        "        <meta charset='UTF-8'>\n" +
                        "        <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                        "        <link rel = 'stylesheet' href = 'Header_And_Footer.css'>\n" +
                        "        <link rel = 'stylesheet' href = 'Conversations.css'>\n" +
                        "        <link rel='icon' href='data:,'>\n" +
                        "        <script>alert('"+message+"');</script>" +
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
                        "        <main onload = ''>\n" +
                        "        </main>\n" +
                        "    </body>\n" +
                        "</html>\n" +
                        "");
        }
    }
    
    //==========================================================================
    
    /*Outputs to show the messages*/
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>\n" +
                        "<!--Nicholas Leong EDUV4551823-->\n" +
                        "<!--This page shows the conversations that the user has currently, as well as will show the selected conversation-->\n" +
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
                        "           <script type = 'text/javascript' src = 'Chat.js'></script>\n" +
                        "           <div id = 'parentDiv'>\n" +
                        "               <div id = 'chat-window'>" +
                                         chat +
                        "                </div><br>" +
                        "                <div id = 'sendMessages'>\n" +
                        "                    <input type = 'text' id = 'inputMessage' name = 'inputMessage' placeholder = 'Type something'>\n" +
                        "                    <button id = 'sendMessage' onclick = 'onMessage()'>Send</button>\n" +
                        "                </div>\n" +
                        "            </div>" +
                        "        </main>\n" +
                        "    </body>\n" +
                        "</html>\n" +
                        "");
        }
    }

}
