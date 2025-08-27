package com.beans;

import java.io.Serializable;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *  Creates a session to manage the user's data
 * @author Nicholas Leong EDUV4551823
 */
@Named("session") 
@SessionScoped
@Stateful
public class UserSession implements Serializable{
    private String userID;
    private String username;
    private String email;
    
    private int groupID;//Tracks which group is currently open
    
    public UserSession() {
    }
    
    public UserSession(String userID, String username, String email){
        this.userID = userID;
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getUserID() {
        return userID;
    }

    public String getEmail() {
        return email;
    }
    
    public int getGroupID() {
        return groupID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setgroupID(int groupID) {
        this.groupID = groupID;
    }
    
    
}
