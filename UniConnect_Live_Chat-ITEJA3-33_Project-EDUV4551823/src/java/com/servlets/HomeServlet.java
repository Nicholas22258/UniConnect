package com.servlets;

import com.beans.UserSession;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServlet;

/**
 *  Retrieves the user's username. 
 * The JSF page is supposed to show a customised welcome message, but it does 
 * not want to show the output texts
 * @author Nicholas Leong EDUV4551823
 */
@Named("/homepage")
public class HomeServlet extends HttpServlet {
    @Inject private UserSession userSession;
    
    public String getUsername(){
        System.out.println("==============homepage===========================");//used to separate and debug web application
        return userSession.getUsername();
    }

}
