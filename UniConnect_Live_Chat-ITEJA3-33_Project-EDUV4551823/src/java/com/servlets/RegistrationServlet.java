package com.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import com.beans.UserSession;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.inject.Inject;


/**
 *  Registers new user
 * @author Nicholas Leong EDUV4551823
 */
@WebServlet("/registration")
public class RegistrationServlet extends HttpServlet {
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
        response.setContentType("text/html;charset=UTF-8");//sets content type
        
        System.out.println("==============Register===========================");//used to separate and debug web application
        
        /*flag variable for storing if the web app successfully connected to database*/
        boolean successfullyConnected = false;
        
        /*Attempts connection to database*/
        try{//connects to database
            Class.forName(dbDriver).newInstance();
            conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
            successfullyConnected = true;
        }catch(ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e){//error catch
            System.out.println("01) ERROR IN CONNECTING TO DATABASE" + e);
            processRequest(request, response, "Error in connecting to database. Please try again.");//refreshes page to let user try again
            closeConnection(conn);
        }finally{//perform INSERT statement
            if (successfullyConnected){
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                String email = request.getParameter("email");
                
                if (register(username, password, email)){
//                    processRequest(request, response, "Registration successful! Welcome to UniConnect '"+userSession.getUsername()+"'!'");
                    closeConnection(conn);
                    RequestDispatcher rd = request.getRequestDispatcher("/Homepage.xhtml");//sends user to homepage
                    rd.forward(request, response);
                }else{
                    processRequest(request, response, "Error in registering. Please try again.");//refreshes page to let user try again
                    closeConnection(conn);
                }
            }else{
                System.out.println("02) ERROR IN CONNECTING TO DATABASE");
                processRequest(request, response, "Error in connecting to database. Please try again.");
            }
        }
    }
    
    //==========================================================================
    
    /*Function to register user, returns true if successful*/
    private boolean register(String username, String password, String email){//procedure to add a new user to the users table
        String insertSQL = "INSERT INTO uniconnectdb.users (username, password, email, date_created) VALUES ('"+username+"',MD5('"+password+"'),'"+email+"', now())";
        
        String getUserIDSQL = "SELECT user_id FROM uniconnectdb.users ORDER BY user_id DESC LIMIT 1";//gets last user_id, which is the new user_id
        try{
            PreparedStatement ps = conn.prepareStatement(insertSQL);
            System.out.println("PREPARED INSERT STATEMENT");
            ps.execute();
            System.out.println("EXECUTED STATEMENT");
            
            int userID = 0;
            Statement st = conn.createStatement();
            System.out.println("PREPARED SELECT STATEMENT");
            ResultSet rs = st.executeQuery(getUserIDSQL);//executes SELECT query and retrieves data
            System.out.println("RETRIEVED RS");
            while (rs.next()){
                userID = rs.getInt("user_id");//gets user's ID
                System.out.println("RETRIEVED ID: " + userID);
            }
             
            String userIDResult = Integer.toString(userID);
            setSession(userIDResult, username, email);
            System.out.println("SET SESSION");
            
            ps.close();
            
            return true;//if insert statement was successful
        }catch(SQLException e){
            System.out.println("SQL ERROR: " + e);
            return false;//if insert statement was not successful
        }
    }
    
    //==========================================================================
    
    /*Procedure to close database connection*/
    private void closeConnection(Connection conn){//closes datbase connection
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
                        "<!--This page lets a user register to use the web app-->\n" +
                        "<html>\n" +
                        "    <head>\n" +
                        "        <title>Register</title>\n" +
                        "        <meta charset=\"UTF-8\">\n" +
                        "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "        <link rel = \"stylesheet\" href = \"Header_And_Footer.css\">\n" +
                        "        <link rel = \"stylesheet\" href = \"Register.css\">\n" +
                        "        <link rel=\"icon\" href=\"data:,\">\n" +
                        "        <script type=\"text/javascript\">alert('" + message +"');</script>" +
                        "    </head>\n" +
                        "    <body>\n" +
                        "        <header>\n" +
                        "            <img src = \"Pics/UniConnectLogo.png\" alt = \"UniConnect Logo\" id = \"logo\" onclick = \"window.open('index.html', '_self')\">\n" +
                        "            <div id = \"account\">\n" +
                        "                <label id = \"user\" >Register</label>\n" +
                        "            </div>\n" +
                        "        </header>\n" +
                        "        \n" +
                        "        <main>\n" +
                        "            <form action = \"Registration_Servlet\" method = \"POST\">\n" +
                        "                <div id = \"enterInfo\">\n" +
                        "                    <label for = \"username\" class = \"inputLabels\">Username:</label> \n" +
                        "                    <input type = \"text\" id = \"username\" name = \"username\" required><br><br>\n" +
                        "                    <label for = \"password\" class = \"inputLabels\">Password:</label>\n" +
                        "                    <input type = \"password\" id = \"password\" name = \"password\" required><br><br>\n" +
                        "                    <label for = email class = \"inputLabels\">Email:</label>\n" +
                        "                    <input type = \"email\" id = \"email\" name = \"email\" required><br><br>\n" +
                        "                    <input type = \"submit\" id = \"submit\" value = \"Sign Up\"><br><br>\n" +
                        "                </div>\n" +
                        "                \n" +
                        "            </form>\n" +
                        "        </main>\n" +
                        "    </body>\n" +
                        "</html>\n" +
                        "");
        }
    }
}
