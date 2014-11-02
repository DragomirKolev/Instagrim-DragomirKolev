/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.dundee.computing.aec.instagrim.stores;


//Class for getters and setters for the login, so it can check if a person is logged in or not.
public class LoggedIn {
    boolean logedin=false;
    String Username=null;
    String Comment=null;
    String email=null;
    String gender=null;
    String firstname=null;
    String lastname=null;
    
    
    public void LogedIn(){
        
    }
    
    public void setUsername(String name){
        this.Username=name;
    }
    public void setComment(String comment){
     this.Comment=comment; 
    }
    public String getComment(){
     return Comment;   
    }
    public String getUsername(){
        return Username;
    }
    public void setLogedin(){
        logedin=true;
    }
    public void setLogedout(){
        logedin=false;
    }
    
    public void setLoginState(boolean logedin){
        this.logedin=logedin;
    }
    public boolean getlogedin(){
        return logedin;
    }
    public String getEmail()
    {
    	return email;
    }
    public void setEmail(String email)
    {
    	this.email = email;
    }
    public String getFirstname()
    {
    	return firstname;
    }
    public void setFirstname(String firstname)
    {
    	this.firstname = firstname;
    }
    public String getLastname()
    {
    	return lastname;
    }
    public void setLastname(String lastname)
    {
    	this.lastname = lastname;
    }
    public String getGender()
    {
    	return gender;
    	
    }
    public void setGender(String gender)
    {
    	this.gender = gender;
    }
}

