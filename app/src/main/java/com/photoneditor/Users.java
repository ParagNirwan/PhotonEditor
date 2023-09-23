package com.photoneditor;


public class Users {
    String id,profilepic, mail, fullname, password;
    public Users(){}

    public Users(String id, String imageuri, String emailId, String name, String passwort) {
        this.id = id;
        this.profilepic =imageuri;
        this.mail = emailId;
        this.fullname = name;
        this.password = passwort;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Users(String profilepic, String mail, String fullname, String password) {
        this.profilepic = profilepic;
        this.mail = mail;
        this.fullname = fullname;
        this.password = password;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}