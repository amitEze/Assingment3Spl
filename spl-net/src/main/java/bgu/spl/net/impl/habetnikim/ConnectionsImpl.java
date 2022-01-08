package bgu.spl.net.impl.habetnikim;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.ConnectionHandler;

public class ConnectionsImpl<T> implements Connections<T> {
    ConcurrentHashMap<Integer, ConnectionHandler> clientsHandlers=new ConcurrentHashMap<Integer,ConnectionHandler>();
    ConcurrentLinkedQueue<Betnik> users = new ConcurrentLinkedQueue<Betnik>();


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

    public void addUser(Betnik u)
    {
        users.add(u);
    }

    public ConcurrentLinkedQueue<Betnik> getUsers() {
        return users;
    }

    public ConnectionHandler getCHandler(int id){
        return clientsHandlers.get(id);
    }
}
