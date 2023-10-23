package valentinood.checkers.event;

import javafx.event.Event;
import valentinood.checkers.game.piece.PieceTeam;

public class CurrentMoveChangedEvent extends Event {
    public final PieceTeam currentMove;

    public CurrentMoveChangedEvent(PieceTeam currentMove) {
        super(ANY);
        this.currentMove = currentMove;
    }

    public PieceTeam getCurrentMove() {
        return currentMove;
    }
}
