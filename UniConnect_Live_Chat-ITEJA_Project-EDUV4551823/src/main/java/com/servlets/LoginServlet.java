package com.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.beans.UserSession;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.inject.Inject;

/**
 *  authenticates user
 * @author Nicholas Leong EDUV4551823
 */
@WebServlet("/logIn")
public class LoginServlet extends HttpServlet {
    
    /*Injection of session bean*/
    @Inject private UserSession userSession;
    
    /*Database connection data*/
    private static Connection conn;
    private static final String dbURL = "jdbc:mysql://localhost:3306/uniconnectdb?useSSL=false";
    private static final String dbUsername = "root";
    private static final String dbPassword = "DB4eca";
    private static final String dbDriver = "com.mysql.cj.jdbc.Driver";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");   //sets content type
        
        System.out.println("==============Log in=============================");//used to separate and debug web application
        
        /*flag variable for storing if the web app successfully connected to database*/
        boolean successfullyConnected = false;
        
        /*Attempts connection to database*/
        try{//connects to database
            Class.forName(dbDriver).newInstance();
            conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
            successfullyConnected = true;
        }catch(ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e){ //error catch
            System.out.println("ERROR: " + e.getMessage());
            processRequest(request, response, "01) Error in connecting to database. Please try again.");//refreshes page to let user try again
            closeConnection(conn);
        }finally{//perform SELECT function
            String email = request.getParameter("email").trim();
            String password = request.getParameter("password").trim();
            System.out.println("RETRIEVED EMAIL AND PASSWORD: " + email + " " + password);
            if (successfullyConnected){
                if (logIn(email, password)){
                    closeConnection(conn);
                    System.out.println("SUCCESSFULLY CONNECTED AND LOGGED IN");
                    RequestDispatcher rd = request.getRequestDispatcher("/Homepage.xhtml");//sends user to homepage
                    rd.forward(request, response);
                }else{
                    System.out.println("ERROR IN LOGGING IN");
                    processRequest(request, response, "Error in loggin in. Please try again.");
                    closeConnection(conn);
                }
            }else{
                processRequest(request, response, "02) Error in connecting to database. Please try again.");
            }
        }
        
    }
    
    //==========================================================================
    
    /*Function to authenticate user, returns true if successful*/
    private boolean logIn(String email, String password){
//        String encryptedPassword = encrypt(password);
        System.out.println("PASSWORD: " + password);
        String selectSQL = "SELECT * FROM uniconnectdb.users WHERE (users.email = '"+email+"' AND users.password = MD5('"+password+"'))";
//        String selectSQL = "SELECT * FROM uniconnectdb.users";
        try{
            PreparedStatement ps = conn.prepareStatement(selectSQL);
//            Statement st = conn.createStatement();
            System.out.println("PREPARED STATEMENT");
            ResultSet rs = ps.executeQuery();//executes SELECT query and retrieves data 
            System.out.println("RETRIEVED DATA");
            
            String userIDResult = "";//Instantiates && !rs.getString("email").equals(email) && !rs.getString("password").equals(encryptedPassword)
            String usernameResult = "";
            String emailResult = "";

            while(rs.next()){
                userIDResult = Integer.toString(rs.getInt("user_id"));
                usernameResult = rs.getString("username");//gets username + "\n count: " + count 
                emailResult = rs.getString("email");//gets email
            }
            System.out.println("DATA 2: " + userIDResult + "  " + usernameResult + "  " + emailResult);
            
            setSession(userIDResult, usernameResult, emailResult);//sets session
            System.out.println("SET SESSION\n" + userSession.getUserID() + " " + userSession.getUsername() + " " + userSession.getEmail());
            
            rs.close();
            ps.close();
            
            return true;
        }catch(SQLException e){
            System.out.println("SQL ERROR: " + e);
            return false;
        }
    }
    
    //==========================================================================
    
    /*Procedure to close database connection*/
    private void closeConnection(Connection conn){//closes database connection
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(RegistrationServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //==========================================================================
    
    /*Procedure to set the session*/
    private void setSession(String userID, String username, String email){        
        userSession.setUserID(userID);
        userSession.setUsername(username);
        userSession.setEmail(email);
    }
    
    //==========================================================================
    
    /*Outputs if there is an error*/
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>\n" +
                        "<!--Nicholas Leong EDUV4551823-->\n" +
                        "<!--This page lets a user log in-->\n" +
                        "<html>\n" +
                        "    <head>\n" +
                        "        <title>Log In</title>\n" +
                        "        <meta charset=\"UTF-8\">\n" +
                        "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "        <link rel = \"stylesheet\" href = \"Header_And_Footer.css\">\n" +
                        "        <link rel = \"stylesheet\" href = \"LogIn.css\">\n" +
                        "        <link rel=\"icon\" href=\"data:,\">\n" +
                        "        <script type=\"text/javascript\">alert('" + message +"');</script>" +
                        "    </head>\n" +
                        "    <body>\n" +
                        "        <header>\n" +
                        "            <img src = \"Pics/UniConnectLogo.png\" alt = \"UniConnect Logo\" id = \"logo\" onclick = \"window.open('index.html', '_self')\">\n" +
                        "<!--            <div id = \"message_call\">\n" +
                        "                <label id = \"message\" onclick = \"\">Message</label>\n" +
                        "                <label id = \"videocall\" onclick = \"\">Video Call</label>\n" +
                        "            </div>-->\n" +
                        "            <div id = \"account\">\n" +
                        "                <label id = \"user\" onclick = \"whichScreen()\" onload = \"LogIn.js\">Log In</label>\n" +
                        "            </div>\n" +
                        "        </header>\n" +
                        "        <main>\n" +
                        "            <form action = \"LoginServlet\" method = \"POST\">\n" +
                        "                <div id = \"enterInfo\">\n" +
                        "                    <label for = \"email\">Email: </label> \n" +
                        "                    <input type = \"text\" id = \"email\" name = \"email\" required><br><br>\n" +
                        "                    <label for = \"password\">Password: </label>\n" +
                        "                    <input type = \"password\" id = \"password\" name = \"password\" required><br><br>\n" +
                        "                    <input type = \"submit\" id = \"submit\" value = \"Log In\"><br><br>\n" +
                        "                </div>\n" +
                        "            </form>\n" +
                        "            <br>\n" +
                        "                <hr />\n" +
                        "                <br>\n" +
                        "                <div id = \"dontHaveAccount\">\n" +
                        "                    <label>Don't have an account?</label><br>\n" +
                        "                    <label>Register here: </label> <a href = \"Register.html\" target = \"_SELF\">Register</a><br><br>\n" +
                        "                </div>\n" +
                        "        </main>\n" +
                        "    </body>\n" +
                        "</html>\n" +
                        "");
        }
    }
}
