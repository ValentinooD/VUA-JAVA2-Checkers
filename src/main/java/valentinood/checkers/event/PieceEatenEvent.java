package valentinood.checkers.event;

import javafx.event.Event;
import valentinood.checkers.game.piece.Piece;

public class PieceEatenEvent extends Event {
    private final Piece whoAte;
    private final Piece ateWhat;

    public PieceEatenEvent(Piece whoAte, Piece ateWhat) {
        super(ANY);
        this.whoAte = whoAte;
        this.ateWhat = ateWhat;
    }

    public Piece getWhoAte() {
        return whoAte;
    }

    public Piece getAteWhat() {
        return ateWhat;
    }
}
