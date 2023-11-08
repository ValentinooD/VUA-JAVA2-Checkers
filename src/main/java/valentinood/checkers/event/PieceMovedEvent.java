package valentinood.checkers.event;

import javafx.event.Event;

public class PieceMovedEvent extends Event {
    private final int fromColumn;
    private final int fromRow;
    private final int toColumn;
    private final int tomRow;

    public PieceMovedEvent(int fromColumn, int fromRow, int toColumn, int tomRow) {
        super(ANY);
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
}
