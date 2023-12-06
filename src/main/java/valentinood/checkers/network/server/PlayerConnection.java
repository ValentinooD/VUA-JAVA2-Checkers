package valentinood.checkers.network.server;

import valentinood.checkers.network.packet.PacketConnectionKeepAlive;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Calendar;

public class PlayerConnection {
    private final Socket socket;
    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;

    private final String name;

    private final int columns;
    private final int rows;

    private long lastKeepAlive = -1;
    private PacketConnectionKeepAlive keepAlive;

    private int portForward = -1;

    public PlayerConnection(Socket socket, ObjectInputStream ois, ObjectOutputStream oos, String name, int columns, int rows) {
        this.socket = socket;
        this.ois = ois;
        this.oos = oos;
        this.name = name;
        this.columns = columns;
        this.rows = rows;
        this.lastKeepAlive = Calendar.getInstance().getTimeInMillis();
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectInputStream getObjectInputStream() {
        return ois;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return oos;
    }

    public String getName() {
        return name;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public int getPortForward() {
        return portForward;
    }

    public long getLastKeepAlive() {
        return lastKeepAlive;
    }

    public void setPortForward(int portForward) {
        this.portForward = portForward;
    }

    public void setLastKeepAlive(long lastKeepAlive) {
        this.lastKeepAlive = lastKeepAlive;
    }

    public PacketConnectionKeepAlive getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlivePacket(PacketConnectionKeepAlive keepAlive) {
        this.keepAlive = keepAlive;
    }

    @Override
    public String toString() {
        return name + " (" + socket.getPort() + ")";
    }
}
