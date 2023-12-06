package valentinood.checkers.event;

import javafx.event.Event;
import valentinood.checkers.game.piece.PieceTeam;

public class WonGameEvent extends Event {
    private final PieceTeam whoWon;
    private final boolean sendPacket;

    public WonGameEvent(PieceTeam whoWon, boolean sendPacket) {
        super(ANY);
        this.whoWon = whoWon;
        this.sendPacket = sendPacket;
    }

    public boolean isSendPacket() {
        return sendPacket;
    }

    public PieceTeam getWhoWon() {
        return whoWon;
    }
}
