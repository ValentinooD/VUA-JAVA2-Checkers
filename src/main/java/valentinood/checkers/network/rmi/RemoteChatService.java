package valentinood.checkers.network.rmi;

import java.rmi.*;
import java.util.List;

public interface RemoteChatService extends Remote {
    String REMOTE_OBJECT_NAME = "valentiood.checkers.network.rmi.RemoteChatService";
    void send(String message) throws RemoteException;
    List<String> getMessages() throws RemoteException;
}
