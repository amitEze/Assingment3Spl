package bgu.spl.net.impl.habetnikim;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.ConnectionHandler;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MoaBetProtcol implements BidiMessagingProtocol {
    ConnectionsImpl betShlita;
    int clientId;
    @Override
    public void start(int connectionId, Connections connections) {
        betShlita=(ConnectionsImpl) connections;
        clientId=connectionId;
    }

    @Override
    public void process(Object message) {
        String opCode=minCutMaxFlow((String)message,1);
        switch (opCode){
            case "1":
                String userName = minCutMaxFlow((String)message,2);
                String passWord = minCutMaxFlow((String)message,3);
                String bDay = minCutMaxFlow((String)message,4);
                Betnik user = new Betnik(userName,passWord,bDay); //might need to calculate age here
                betShlita.addUser(user);
                break;
            case "2":
                String uName = minCutMaxFlow((String)message,2);
                String pWord = minCutMaxFlow((String)message,3);
                String captcha = minCutMaxFlow((String)message,4); //should enter zero after captcha
                if(captcha.getBytes(StandardCharsets.UTF_8)[0]==1){
                    Queue<Betnik> tryLog=betShlita.getUsers();
                    Boolean logged=false;
                    for(Betnik b: tryLog){
                        if(b.getUserName()==uName)
                        {
                            if(b.getPassWord()==pWord){
                                if(b.getcHandler()==null){
                                    b.setHandler(betShlita.getChandler(clientId));
                                    logged=true;
                                }
                            }
                        }
                    }
                    if(logged==false){
                        //need to write error message
                    }
                    else{
                        //need to write ack message
                    }
                }

            case "3":


            case "4":
                String follow = ((String) message).substring(2,4);
                String toFollow = (String)((String) message).substring(4,((String) message).length());
                if(follow.getBytes(StandardCharsets.UTF_8)[0]==0)
                {
                    //case follow
                    Betnik userMe = getUser();
                    Betnik ushiya = getUserByName(toFollow);
                    if(userMe!=null && ushiya!=null)
                    {
                        //should follow
                        Boolean isFollowed = false;
                        ConcurrentLinkedQueue<Betnik> iFollow = userMe.getFollowing();
                        for(Betnik b: iFollow) {
                            if(b==ushiya) {
                                isFollowed=true;
                            }
                        }
                        if(!isFollowed) {
                            //actualy follow
                            iFollow.add(ushiya);
                            ushiya.getFollowers().add(userMe);
                            // send ack message!!!
                        }
                        else {
                            //should send errorr!!!!
                        }
                    }
                    else{
                        // should send errorr!!!!!
                    }
                }
                else
                {
                    //case unfollow
                    Betnik userMe = getUser();
                    Betnik ugly = getUserByName(toFollow);
                    if(userMe!=null && ugly!=null && userMe.getcHandler()!=null){
                        Boolean isFollowed = false;
                        ConcurrentLinkedQueue<Betnik> iFollow = userMe.getFollowing();
                        for(Betnik b: iFollow) {
                            if(b==ugly)
                                isFollowed=true;
                        }
                        if(isFollowed){
                            userMe.getFollowing().remove(ugly);
                            ugly.getFollowers().remove(userMe);
                        }
                        else
                        {
                            //send errorr
                        }
                    }
                    else{
                        //send error
                    }
                }

            case "5":
                Betnik userMe = getUser();
                if(userMe!=null) {
                    String content = minCutMaxFlow((String)message,2);
                    LinkedList<Betnik> usersInContent = new LinkedList<Betnik>();
                    for(int i=0; i<content.length()-1;i++){ //finds all usernames in content
                        if(content.charAt(i)=='@'){
                            int endOfUsername=i+1;
                            while(content.charAt(endOfUsername)!=' ' && content.charAt(endOfUsername)!='\0'){ //not sure about that
                                endOfUsername++;
                            }
                            Betnik u = getUserByName(content.substring(i+1,endOfUsername));
                            if(u!=null){
                                usersInContent.add(u);
                            }
                        }
                    }
                    ConcurrentLinkedQueue<Betnik> followers = userMe.getFollowers();
                    LinkedList<Betnik> usersToSend = new LinkedList<Betnik>(usersInContent);
                    for(Betnik b: followers) {
                        boolean isABuddy;
                        for(Betnik bInContent: usersInContent){

                        }
                    }


                }
            case "6":
            case "7":
            case "8":
            case "12":
        }
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }

    private String minCutMaxFlow(String msg,int zNum){
        int zCount=0;
        int start=0;

        for(int i=0; i<msg.length();i++){
            if(msg.charAt(i)=='\0'){
                if(zCount==zNum-1)
                    return msg.substring(start,i);
                else{
                    zCount++;
                    start=i+1;}
            }
        }
        return "error";
    }

    public Betnik getUser(){
        Queue<Betnik> users=betShlita.getUsers();
        for(Betnik b: users)
        {
            if(b.getcHandler()==betShlita.getChandler(clientId))
                return b;
        }
        return null; //not connected not registered
    }

    public  Betnik getUserByName(String userName){
        Queue<Betnik> users=betShlita.getUsers();
        for(Betnik b: users){
            if(b.getUserName().equals(userName))
                return b;
        }
        return null; // not registered
    }


}
