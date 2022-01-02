package bgu.spl.net.srv;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.api.bidi.Connections;

public class ConnectionsImpl<T> implements Connections<T> {
    ConcurrentHashMap<Integer,ConnectionHandler> clientsHandlers=new ConcurrentHashMap<Integer,ConnectionHandler>();
    @Override
    public boolean send(int connectionId, T msg) {
        if(clientsHandlers.get(connectionId)!=null){
            clientsHandlers.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(T msg) {
        for(Integer id: clientsHandlers.keySet()){
            clientsHandlers.get(id).send(msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        if(clientsHandlers.get(connectionId)!=null)
            clientsHandlers.remove(connectionId);
    }

}
