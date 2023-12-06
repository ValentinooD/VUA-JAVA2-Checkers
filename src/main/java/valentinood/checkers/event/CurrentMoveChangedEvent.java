package valentinood.checkers.event;

import javafx.event.Event;
import valentinood.checkers.game.piece.PieceTeam;

public class CurrentMoveChangedEvent extends Event {
    public final PieceTeam currentMove;
    private final boolean sendPacket;

    public CurrentMoveChangedEvent(PieceTeam currentMove, boolean sendPacket) {
        super(ANY);
        this.currentMove = currentMove;
        this.sendPacket = sendPacket;
    }

    public boolean isSendPacket() {
        return sendPacket;
    }

    public PieceTeam getCurrentMove() {
        return currentMove;
    }
}
