package valentinood.checkers.game.piece;

public enum PieceTeam {
    Red("Red"),
    Blue("Blue");

    private final String prettyText;

    PieceTeam(String prettyText) {
        this.prettyText = prettyText;
    }

    public String getPrettyText() {
        return prettyText;
    }
}
