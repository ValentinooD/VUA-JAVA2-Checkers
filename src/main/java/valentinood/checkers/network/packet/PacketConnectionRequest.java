package valentinood.checkers.network.packet;

public class PacketConnectionRequest extends PacketConnection {
    private final String name;
    private final int gridWidth;
    private final int gridHeight;

    public PacketConnectionRequest(String name, int gridWidth, int gridHeight) {
        this.name = name;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }

    public String getName() {
        return name;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    @Override
    public String toString() {
        return "PacketConnectionRequest{" +
                "name='" + name + '\'' +
                ", gridWidth=" + gridWidth +
                ", gridHeight=" + gridHeight +
                '}';
    }
}
