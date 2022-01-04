package bgu.spl.net.impl.habetnikim;

import bgu.spl.net.srv.ConnectionHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Betnik {
    private String userName;
    private String passWord;
    private String bDay;
    private ConnectionHandler cHandler=null;

    ConcurrentLinkedQueue<Betnik> following = new ConcurrentLinkedQueue<Betnik>();
    ConcurrentLinkedQueue<Betnik> followers = new ConcurrentLinkedQueue<Betnik>();
    ConcurrentLinkedQueue<String> posts = new ConcurrentLinkedQueue<String>();
    ConcurrentLinkedQueue<String> unseeNotifications = new ConcurrentLinkedQueue<String>();
    ConcurrentLinkedQueue<String> pms = new ConcurrentLinkedQueue<String>();

    public Betnik(String userName, String passWord, String bDay){
        this.userName=userName;
        this.passWord=passWord;
        this.bDay=bDay;
    }

    public void login(ConnectionHandler ch){
        cHandler=ch;
    }

    public String getUserName(){return userName;}

    public String getPassWord(){return passWord;}

    public ConnectionHandler getcHandler(){return cHandler;}

    public void setHandler(ConnectionHandler c){this.cHandler=c;}

    public ConcurrentLinkedQueue<Betnik> getFollowing(){return following;}

    public  ConcurrentLinkedQueue<Betnik> getFollowers(){return followers;}

    public ConcurrentLinkedQueue<String> getUnseeNotifications(){return unseeNotifications;}

    public ConcurrentLinkedQueue<String> getPosts(){return posts;}

    public ConcurrentLinkedQueue<String> getPms(){return pms;}
}
