package valentinood.checkers.network.packet;

import valentinood.checkers.game.piece.PieceTeam;

public class PacketGameEnd extends PacketGame {
    private PieceTeam winner;

    public PacketGameEnd() {
        super();
    }

    public PacketGameEnd(PieceTeam winner) {
        super();
        this.winner = winner;
    }

    public PieceTeam getWinner() {
        return winner;
    }

    @Override
    public String toString() {
        return "PacketGameEnd{" +
                "winner=" + winner +
                '}';
    }
}
