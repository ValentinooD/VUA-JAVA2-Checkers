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
        this.currentMove = currentMove;
        this.piecesCount = new HashMap<>();

        // it has to be done like this
        // because clone() will not clone the second array
        // and this keeps breaking the replay
        this.board = new Piece[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Piece piece = board[row][column];

                if (piece != null)
                    piece = piece.clone();

                this.board[row][column] = piece;
            }
        }

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
