package valentinood.checkers.network.packet;

import valentinood.checkers.game.piece.PieceTeam;

import java.io.Serial;

public class PacketGameCurrentMove extends PacketGame {
    @Serial
    private static final long serialVersionUID = 432443254316L;

    public PieceTeam currentMove;

    public PacketGameCurrentMove() {
        super();
    }

    public PacketGameCurrentMove(PieceTeam currentMove) {
        super();
        this.currentMove = currentMove;
    }

    public PieceTeam getCurrentMove() {
        return currentMove;
    }

    @Override
    public String toString() {
        return "PacketGameCurrentMove{" +
                "currentMove=" + currentMove +
                '}';
    }
}
