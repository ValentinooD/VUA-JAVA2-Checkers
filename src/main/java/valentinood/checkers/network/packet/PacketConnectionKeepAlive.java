package valentinood.checkers.network.packet;

public class PacketConnectionKeepAlive extends PacketConnection {
    private final int number;

    public PacketConnectionKeepAlive(int number) {
        super();
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "PacketConnectionKeepAlive{" +
                "number=" + number +
                '}';
    }
}
