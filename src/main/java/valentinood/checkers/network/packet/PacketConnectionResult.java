package valentinood.checkers.network.packet;

public class PacketConnectionResult extends PacketConnection {
    private final String username;
    private final int columns;
    private final int rows;
    private final ConnectionResult result;

    public PacketConnectionResult(ConnectionResult result) {
        super();
        this.username = null;
        this.columns = 0;
        this.rows = 0;
        this.result = result;
    }

    public PacketConnectionResult(String username, int columns, int rows, ConnectionResult result) {
        this.username = username;
        this.columns = columns;
        this.rows = rows;
        this.result = result;
    }

    public String getUsername() {
        return username;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public ConnectionResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "PacketConnectionResult{" +
                "username='" + username + '\'' +
                ", result=" + result +
                '}';
    }

    public enum ConnectionResult {
        ACCEPTED(true),
        BAD_PACKET(false),
        REFUSED(false);

        private final boolean allowed;

        ConnectionResult(boolean allowed) {
            this.allowed = allowed;
        }

        public boolean isAllowed() {
            return allowed;
        }
    }
}
