package valentinood.checkers.network.packet;

public class PacketGamePieceEaten extends PacketGame {
    private int column;
    private int row;

    public PacketGamePieceEaten() {
        super();
    }

    public PacketGamePieceEaten(int column, int row) {
        this.column = column;
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    @Override
    public String toString() {
        return "PacketGamePieceEaten{" +
                "column=" + column +
                ", row=" + row +
                '}';
    }
}
