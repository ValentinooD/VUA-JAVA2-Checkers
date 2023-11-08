package valentinood.checkers.game;

import valentinood.checkers.game.piece.Piece;
import valentinood.checkers.game.piece.PieceTeam;

import java.io.Serializable;
import java.util.HashMap;

public class GameBoardSnapshot implements Serializable {
    private final int columns;
    private final int rows;
    private final Piece[][] board;
    private final PieceTeam currentMove;

    private final HashMap<PieceTeam, Integer> piecesCount; // calculated

    public GameBoardSnapshot(int columns, int rows, Piece[][] board, PieceTeam currentMove) {
        this.columns = columns;
        this.rows = rows;
        this.board = board;
        this.currentMove = currentMove;
        this.piecesCount = new HashMap<>();

        calculatePieces();
    }

    private void calculatePieces() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Piece piece = board[i][j];
                if (piece == null) continue;

                piecesCount.put(piece.getTeam(), piecesCount.getOrDefault(piece.getTeam(), 0) + 1);
            }
        }
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public Piece[][] getBoard() {
        return board;
    }

    public PieceTeam getCurrentMove() {
        return currentMove;
    }

    public int getPiecesCount(PieceTeam team) {
        return piecesCount.getOrDefault(team, 0);
    }

    public HashMap<PieceTeam, Integer> getPiecesCount() {
        return piecesCount;
    }
}
