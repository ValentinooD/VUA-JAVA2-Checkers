package valentinood.checkers.network.packet;

import java.io.Serial;

public class PacketGamePieceMove extends PacketGame {
    @Serial
    private static final long serialVersionUID = 4431432422L;

    private int fromColumn;
    private int fromRow;
    private int toColumn;
    private int tomRow;

    public PacketGamePieceMove() {
        super();
    }

    public PacketGamePieceMove(int fromColumn, int fromRow, int toColumn, int tomRow) {
        super();
        this.fromColumn = fromColumn;
        this.fromRow = fromRow;
        this.toColumn = toColumn;
        this.tomRow = tomRow;
    }

    public int getFromColumn() {
        return fromColumn;
    }

    public int getFromRow() {
        return fromRow;
    }

    public int getToColumn() {
        return toColumn;
    }

    public int getTomRow() {
        return tomRow;
    }

    @Override
    public String toString() {
        return "PacketGamePieceMove{" +
                "fromColumn=" + fromColumn +
                ", fromRow=" + fromRow +
                ", toColumn=" + toColumn +
                ", tomRow=" + tomRow +
                '}';
    }
}
