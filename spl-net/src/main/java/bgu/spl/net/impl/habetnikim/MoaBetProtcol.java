package bgu.spl.net.impl.habetnikim;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
//import bgu.spl.net.srv.ConnectionHandler;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MoaBetProtcol implements BidiMessagingProtocol {
    ConnectionsImpl betShlita=null;
    int clientId=-1;
    String[] bannedWords= {"flugelhorn","cusmerots","reshonletsion","fuckshit","cluster","databatch","ronenperets"};

    public MoaBetProtcol(){}
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
                String userName=minCutMaxFlow((String)message,2);
                String passWord=minCutMaxFlow((String)message,3);
                String bDay=minCutMaxFlow((String)message,4);
                Betnik user=new Betnik(userName,passWord,bDay);//might need to calculate age here
                betShlita.addUser(user);
                String ackMsg=shorToString(10)+shorToString(1);
                betShlita.send(clientId,ackMsg);
            case "2":
                String uName=minCutMaxFlow((String)message,2);
                String pWord=minCutMaxFlow((String)message,3);
                String captcha=((String) message).substring(((String) message).length()-2,((String) message).length()-1);
                if(captcha.getBytes(StandardCharsets.UTF_8)[0]==1){
                    Queue<Betnik> tryLog=betShlita.getUsers();
                    boolean logged=false;
                    for(Betnik b : tryLog){
                        if(b.getUserName().equals(uName)) {
                            if (b.getPassWord().equals(pWord)) {
                                if(b.getCHandler()==null) {
                                    b.setHandler(betShlita.getCHandler(clientId));
                                    b.setConnectionId(clientId);
                                    logged=true;
                                }
                            }
                        }
                    }
                    if(logged){
                        //need to write error************
                    }
                    else{
                        String ackRes=shorToString(10)+shorToString(2);
                        betShlita.send(clientId,ackRes);
                    //need to push all posts and PM that the user missed************
                        Queue<String> toPush=getUser().getUnseeNotifications();
                        for(String s: toPush){
                            betShlita.send(clientId,s);
                            toPush.remove(s);
                        }
                        }
                    }
            case "3":
                Queue<Betnik> users = betShlita.getUsers();
                boolean loggedOut=false;
                for(Betnik b : users) {
                    if (betShlita.getCHandler(clientId) == b.getCHandler()){
                        b.setHandler(null);
                        loggedOut=true;
                    }
                }
                if(loggedOut){
                    //need to write error*********
                }
                else{
                    //need to send ACK response**************
                    //should terminate?!
                }
            case "4":
                String follow = minCutMaxFlow((String) message,2).substring(0,1);
                String toFollow = minCutMaxFlow((String)message,2).substring(1);
                if(follow.equals("\0"))
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
                    if(userMe!=null && ugly!=null && userMe.getCHandler()!=null){
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
                String dest=minCutMaxFlow((String) message,2);
                String content=minCutMaxFlow((String) message,3);
                String timing= minCutMaxFlow((String)message,4);
                if(getUser()!=null){
                    if(getUserByName(dest)!=null){
                        if(getUser().getFollowing().contains(getUserByName(dest))){
                            for(String w:bannedWords){
                                if(content.contains(w)){
                                    content.replaceAll(w,"<filtered>");
                                }
                            }
                            getUser().addPM(content);
                            if(getUserByName(dest).getCHandler()!=null){//connected so use send to print on his screed (use send func)
                                betShlita.send(getUserByName(dest).getConnectionId(),content);
                            } else{
                                getUserByName(dest).addNotification(content);
                            }
                        }
                        else{
                            //should send error because the sender not following the receiver
                        }
                    }
                    else{
                        //should send error because destination user isnt registered
                    }
                }
                else{
                    //should send error because the sender is not logged in
                }
            case "7":
            case "8":
            case "12":
                String stolker=minCutMaxFlow((String) message,2);
                if(getUserByName(stolker)!=null){
                    Betnik beniSela= getUserByName(stolker);
                    beniSela.addBlocker(getUser());
                    getUser().getFollowers().remove(beniSela);
                    getUser().getFollowers().remove(beniSela);
                    //send ACK now
                }
                else{
                    //send error because the user you want to block dosent exists
                }
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
            if(b.getCHandler()==betShlita.getCHandler(clientId))
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
