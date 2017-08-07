package com.gal.firechat;

import android.util.Log;

import com.google.firebase.database.Exclude;

import java.util.Comparator;

/**
 * Created by Gal on 13/07/2017.
 */



public class User{

    private String email,nickname;
    private String connection;

    public User(){}

    public User(String email) {
        this.email = email.replace('.','_');
        nickname=null;
        this.connection="ONLINE";
    }

    public String getNickname() {
        if (nickname != null)
            return nickname;
        else return getEmail();
    }


    public String getEmail(){
        return email;
    }

    public String getConnection(){
        return connection;
    }

    public static User getPublicRoom(){
        return new User("Lobby");
    }

    public static User getLockedRoom(){
        return new User("Locked Room");
    }

    public static Comparator<User> UserComparator = new Comparator<User>() {

        public int compare(User o1, User o2) {
            int connectiondiff = o1.getConnection().compareTo(o2.getConnection());
            if (connectiondiff==0)
                return (o1.getNickname().compareTo(o2.getNickname()));
            else
                return -connectiondiff;
        }

    };
}