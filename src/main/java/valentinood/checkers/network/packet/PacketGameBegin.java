package valentinood.checkers.network.packet;

import valentinood.checkers.game.piece.PieceTeam;

import java.util.HashMap;
import java.util.Map;

public class PacketGameBegin extends PacketGame {
    private int columns;
    private int rows;

    private PieceTeam playerTeam;
    private Map<PieceTeam, String> teams;

    public PacketGameBegin() {
        super();
    }

    public PacketGameBegin(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.teams = new HashMap<>();
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public Map<PieceTeam, String> getTeams() {
        return teams;
    }

    public PieceTeam getPlayerTeam() {
        return playerTeam;
    }

    public void setPlayerTeam(PieceTeam team) {
        this.playerTeam = team;
    }

    public void setTeam(PieceTeam team, String username) {
        teams.put(team, username);
    }

    @Override
    public String toString() {
        return "PacketGameBegin{" +
                "columns=" + columns +
                ", rows=" + rows +
                ", playerTeam=" + playerTeam +
                ", teams=" + teams +
                '}';
    }
}
