package valentinood.checkers.game.piece;

import valentinood.checkers.CheckersApplication;

import java.net.URL;

public enum PieceType {
    Red(PieceTeam.Red, "images/red.png", false),
    RedKing(PieceTeam.Red, "images/red_king.png", true),
    Blue(PieceTeam.Blue, "images/blue.png", false),
    BlueKing(PieceTeam.Blue, "images/blue_king.png", true);

    private final PieceTeam team;
    private final URL resource;
    private final boolean king;

    PieceType(PieceTeam team, String res, boolean king) {
        this.team = team;
        this.resource = CheckersApplication.class.getResource(res);
        this.king = king;
    }


    public PieceTeam getTeam() {
        return team;
    }

    public URL getResource() {
        return resource;
    }

    public boolean isKing() {
        return king;
    }
}
