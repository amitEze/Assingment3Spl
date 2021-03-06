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
    boolean shouldTerminate=false;

    public MoaBetProtcol(){}
    @Override
    public void start(int connectionId, Connections connections) {
        betShlita=(ConnectionsImpl) connections;
        clientId=connectionId;
        betShlita = ConnectionsImpl.getInstance();
    }

    @Override
    public void process(Object message) {
        String opCode=minCutMaxFlow((String)message,1);
        Betnik userMe = getUser();
        short actuallyOpCode = Short.valueOf(opCode);
        String notification = shorToString((short)9)+';'+shorToString(actuallyOpCode); //should i enter here a ;?
        String ack = shorToString((short) 10)+';'+shorToString(actuallyOpCode);
        String error = shorToString((short)11)+';'+shorToString(actuallyOpCode);

        switch (opCode){
            case "1":
                //case register
                String userName=minCutMaxFlow((String)message,2);
                String passWord=minCutMaxFlow((String)message,3);
                String bDay=minCutMaxFlow((String)message,4);
                Betnik user=new Betnik(userName,passWord,bDay);//might need to calculate age here
                betShlita.addUser(user);
                betShlita.send(clientId,ack);
            case "2":
                //case login
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
                        betShlita.send(clientId,error); //case he already online
                    }
                    else{
                        betShlita.send(clientId,ack);
                    //need to push all posts and PM that the user missed************
                        Queue<String> toPush=getUser().getUnseeNotifications();
                        for(String s: toPush){
                            betShlita.send(clientId,s);
                            toPush.remove(s);
                        }
                        }
                    }
            case "3":
                //case logout
                Queue<Betnik> users = betShlita.getUsers();
                boolean loggedOut=false;
                for(Betnik b : users) {
                    if (betShlita.getCHandler(clientId) == b.getCHandler()){
                        b.setHandler(null);
                        b.setConnectionId(-1);
                        loggedOut=true;
                    }
                }
                if(!loggedOut){
                    betShlita.send(clientId,error);
                }
                else{
                    betShlita.send(clientId,ack);
                    //terminate
                    shouldTerminate=true;
                    betShlita.disconnect(clientId);
                }
            case "4":
                //follow/unfollow
                String follow = ((String) message).substring(2,4);
                String toFollow = (String)((String) message).substring(4,((String) message).length());
                if(follow.getBytes(StandardCharsets.UTF_8)[0]==0)
                {
                    //case follow
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
                        if(!isFollowed && !userMe.getBlockedBy().contains(ushiya) && !ushiya.getBlockedBy().contains(userMe)) { //not following yet and not blocked by
                            //actualy follow
                            userMe.getFollowing().add(ushiya);
                            ushiya.getFollowers().add(userMe);
                            ack=ack+toFollow+'\0';
                            betShlita.send(clientId,ack);
                        } else { //userme already following this user or blocked by him or blocked him
                            betShlita.send(clientId,error);
                        }
                    }//following user isn't online or there's no ushiya user registered
                    else{
                        betShlita.send(clientId,error);
                    }
                }
                else
                {
                    //case unfollow
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
                            ack = ack+toFollow+'\0';
                            betShlita.send(clientId,ack);
                        }
                        else
                        {
                            betShlita.send(clientId,error);
                        }
                    }
                    else{
                        betShlita.send(clientId,error);
                    }
                }

            case "5":
                //case post
                if(userMe!=null) {
                    String content = minCutMaxFlow((String)message,2);
                    LinkedList<Betnik> usersInContent = new LinkedList<Betnik>();
                    notification = notification+"\1"+userMe.getUserName()+'\0'+content+'\0';
                    for(int i=0; i<content.length()-1;i++){ //finds all usernames in content
                        if(content.charAt(i)=='@'){
                            int endOfUsername=i+1;
                            while(content.charAt(endOfUsername)!=' ' && content.charAt(endOfUsername)!='\0'){
                                endOfUsername++;
                            }
                            Betnik u = getUserByName(content.substring(i+1,endOfUsername));
                            if(u!=null && !(userMe.getBlockedBy().contains(u))){
                                usersInContent.add(u);
                            }
                            i=endOfUsername-1;
                        }
                    }
                    userMe.getPosts().add(content); //adding message to the send user posts
                    ConcurrentLinkedQueue<Betnik> followers = userMe.getFollowers();
                    LinkedList<Betnik> usersToSend = new LinkedList<Betnik>(usersInContent);
                    for(Betnik b: followers) {
                        boolean shouldAddhim=true;
                        for(Betnik bInContent: usersInContent){
                            if(bInContent==b){
                                shouldAddhim=false; // he is already in the list
                            }
                        }
                        if(shouldAddhim)
                            usersToSend.add(b);
                    }
                    for(Betnik b: usersToSend){
                        //send them messages
                        ack=ack;
                        if(b.getCHandler()!=null){
                            betShlita.send(b.getConnectionId(),notification); //for sure?
                        }
                        else{
                            b.getUnseeNotifications().add(notification);
                        }
                    }
                    betShlita.send(clientId,ack);


                }else{
                    betShlita.send(clientId,error);
                }
            case "6":
                //case pm
                String dest=minCutMaxFlow((String) message,2);
                String content=minCutMaxFlow((String) message,3);
                String timing= minCutMaxFlow((String)message,4);
                if(getUser()!=null){ //logged in
                    notification = notification+"\0"+userMe.getUserName()+'\0'+content+'\0';
                    if(getUserByName(dest)!=null){
                        if(getUser().getFollowing().contains(getUserByName(dest))){
                            for(String w:bannedWords){ //filtering message
                                if(content.contains(w)){
                                    content.replaceAll(w,"<filtered>");
                                }
                            }
                            getUser().addPM(content); //add pm to sending username pms list
                            if(getUserByName(dest).getCHandler()!=null){//connected so use send to print on his screen (use send func)
                                betShlita.send(getUserByName(dest).getConnectionId(),notification);
                            } else{
                                getUserByName(dest).addNotification(notification);
                            }
                            betShlita.send(getUser().getConnectionId(),ack); // send ack message to sending user
                        }//sending user isn't following dest user
                        else{
                            betShlita.send(clientId,error);
                        }
                    }//there's no username named dest
                    else{
                        betShlita.send(clientId,error);
                    }
                }//sending user isn't online
                else{
                    betShlita.send(clientId,error);
                }
            case "7":
                //logstat
                if(userMe!=null){
                    ConcurrentLinkedQueue<Betnik> loggedInUsers = betShlita.getUsers();
                    LinkedList<String> loggedStats = new LinkedList<String>();
                    ConcurrentLinkedQueue<Betnik> blockedBy = userMe.getBlockedBy();
                    for(Betnik b: loggedInUsers){
                        if(!blockedBy.contains(b) && !b.getBlockedBy().contains(userMe)){
                            String bStat = "";
                            bStat = bStat+b.getAge();
                            bStat = " " + bStat + shorToString((short) b.getPosts().size());
                            bStat = " " + bStat + shorToString((short) b.getFollowers().size());
                            bStat = " " + bStat + shortToBytes((short) b.getFollowing().size());
                            loggedStats.add(bStat);
                        }
                    }
                    while(!loggedStats.isEmpty()){
                        betShlita.send(clientId,ack+loggedStats.remove());
                    }
                }else{
                    betShlita.send(clientId,error);
                }
            case "8":
                if(userMe!=null){
                    String onlyUsers = minCutMaxFlow((String) message,2);
                    LinkedList<String> statsToSend = new LinkedList<String>();
                    int j=0;
                    for(int i=0; i<onlyUsers.length();i++){
                        if(onlyUsers.charAt(i)=='|' || onlyUsers.charAt(i)=='\0'){
                            Betnik userToStatus = getUserByName(onlyUsers.substring(i,j));
                            if(userToStatus!=null && !(userMe.getBlockedBy().contains(userToStatus)) && !userToStatus.getBlockedBy().contains(userMe)){  //***** and not blocked
                                String s = "";
                                j=i;
                                s = s+ userToStatus.getAge();
                                s=" "+s+shorToString((short)userToStatus.getPosts().size());
                                s=" "+s+shorToString((short) userToStatus.getFollowers().size());
                                s=" "+s+shorToString((short) userToStatus.getFollowing().size());
                                statsToSend.add(s);
                            }else{
                                betShlita.send(clientId,error);
                                break;
                            }
                        }
                    }
                    while(!statsToSend.isEmpty())
                        betShlita.send(clientId,ack+statsToSend.remove());
                }else{
                    betShlita.send(clientId,error);
                }
            case "12":
                String stolker=minCutMaxFlow((String) message,2);
                if(getUserByName(stolker)!=null){
                    Betnik beniSela= getUserByName(stolker);
                    beniSela.addBlocker(getUser());
                    getUser().getFollowers().remove(beniSela);
                    getUser().getFollowing().remove(beniSela);
                    beniSela.getFollowing().remove(getUser());
                    beniSela.getFollowers().remove(getUser());
                    betShlita.send(clientId,ack);
                }
                else{
                    betShlita.send(clientId,error);
                }
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
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

    public String shorToString(short num){
        byte[] byt = shortToBytes(num);
        String a = new String(byt,0,2);
        return a;
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }


}
