package valentinood.checkers.network.packet;

public class PacketGameChatMessage extends PacketGame {
    private String message;

    public PacketGameChatMessage() {
        super();
    }

    public PacketGameChatMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "PacketGameChatMessage{" +
                "message='" + message + '\'' +
                '}';
    }
}
