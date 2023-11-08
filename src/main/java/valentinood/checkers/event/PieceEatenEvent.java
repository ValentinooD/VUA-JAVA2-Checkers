package valentinood.checkers.event;

import javafx.event.Event;
import valentinood.checkers.game.piece.Piece;

public class PieceEatenEvent extends Event {
    private final Piece whoAte;
    private final Piece ateWhat;
    private final int column;
    private final int row;

    public PieceEatenEvent(Piece whoAte, Piece ateWhat, int column, int row) {
        super(ANY);
        this.whoAte = whoAte;
        this.ateWhat = ateWhat;
        this.column = column;
        this.row = row;
    }

    public Piece getWhoAte() {
        return whoAte;
    }

    public Piece getAteWhat() {
        return ateWhat;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }
}
