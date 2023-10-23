package valentinood.checkers.event;

import javafx.event.Event;
import valentinood.checkers.game.piece.PieceTeam;

public class WonGameEvent extends Event {
    private final PieceTeam whoWon;

    public WonGameEvent(PieceTeam whoWon) {
        super(ANY);
        this.whoWon = whoWon;
    }

    public PieceTeam getWhoWon() {
        return whoWon;
    }
}
