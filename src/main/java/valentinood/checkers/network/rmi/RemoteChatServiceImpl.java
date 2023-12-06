package valentinood.checkers.network.rmi;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class RemoteChatServiceImpl implements RemoteChatService {
    private final List<String> chatMessages;

    public RemoteChatServiceImpl() {
        chatMessages = new ArrayList<>();
    }

    @Override
    public void send(String chatMessage) throws RemoteException {
        chatMessages.add(chatMessage);
    }

    @Override
    public List<String> getMessages() throws RemoteException {
        return chatMessages;
    }
}
