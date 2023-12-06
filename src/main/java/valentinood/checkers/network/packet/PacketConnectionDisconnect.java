package valentinood.checkers.network.packet;

public class PacketConnectionDisconnect extends PacketConnection {
    private String message;

    public PacketConnectionDisconnect() {
        super();
        this.message = "Disconnected";
    }

    public PacketConnectionDisconnect(String message) {
        super();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "PacketConnectionDisconnect{" +
                "message='" + message + '\'' +
                '}';
    }
}
