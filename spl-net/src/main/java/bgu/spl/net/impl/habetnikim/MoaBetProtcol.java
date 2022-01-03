package bgu.spl.net.impl.habetnikim;

import bgu.spl.net.api.MessagingProtocol;

public class MoaBetProtcol implements MessagingProtocol {
    @Override
    public Object process(Object msg) {
        String opCode=minCutMaxFlow((String)msg,1);
        switch (opCode){
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "12":
        }
        return null;
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
}
