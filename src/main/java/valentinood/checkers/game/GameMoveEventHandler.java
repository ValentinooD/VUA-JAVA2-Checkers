package valentinood.checkers.game;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import valentinood.checkers.Constants;
import valentinood.checkers.event.PieceEatenEvent;
import valentinood.checkers.event.PieceMovedEvent;
import valentinood.checkers.event.WonGameEvent;
import valentinood.checkers.game.piece.Piece;
import valentinood.checkers.game.piece.PieceTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameMoveEventHandler implements EventHandler<MouseEvent> {
    private final GameBoard gameBoard;
    private final StackPane stackPane;
    private final int pieceColumn;
    private final int pieceRow;

    private static SelectedPiece selected = null;

    public GameMoveEventHandler(GameBoard gameBoard, StackPane stackPane, int pieceColumn, int pieceRow) {
        this.gameBoard = gameBoard;
        this.stackPane = stackPane;
        this.pieceColumn = pieceColumn;
        this.pieceRow = pieceRow;

        HANDLERS.add(this);
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        clearSelections();

        if (!gameBoard.isPlayable()) return;

        Piece piece = gameBoard.get(pieceColumn, pieceRow);
        if (piece == null && selected != null) {
            doMove(); // method handles legality of move
            selected = null;
            return;
        } else if (piece == null) {
            return;
        }

        if (piece.getTeam() == gameBoard.getUnplayableSide()) return;
        if (piece.getTeam() != gameBoard.getCurrentMove()) return;

        selected = new SelectedPiece(piece, pieceColumn, pieceRow);

        stackPane.setBackground(Constants.BACKGROUND_TILE_HIGHLIGHTED);

        List<StackPane> moves = GameLogic.getAllowedMoves(gameBoard, piece, pieceColumn, pieceRow);
        for (StackPane move : moves) {
            move.setBackground(Constants.BACKGROUND_TILE_HIGHLIGHTED);
            selected.allowedMoves.add(move);
        }
    }

    private void doMove() {
        if (selected == null) throw new IllegalStateException();

        if (!selected.allowedMoves.contains(stackPane)) return;

        gameBoard.move(selected.column, selected.row, pieceColumn, pieceRow);
        EventHandler<PieceMovedEvent> pmeh = gameBoard.getEventRepository().getHandler(PieceMovedEvent.class);
        if (pmeh != null) {
            pmeh.handle(new PieceMovedEvent(selected.column, selected.row, pieceColumn, pieceRow));
        }

        if (distance(selected.column, selected.row, pieceColumn, pieceRow) > 2) {
            int middleRow = (selected.row + pieceRow) / 2;
            int middleColumn = (selected.column + pieceColumn) / 2;

            Piece eaten = gameBoard.remove(middleColumn, middleRow);

            EventHandler<PieceEatenEvent> handler = gameBoard.getEventRepository().getHandler(PieceEatenEvent.class);
            if (handler != null) {
                handler.handle(new PieceEatenEvent(selected.piece, eaten, middleColumn, middleRow));
            }

            // It calls an event and refreshes the UI, LEAVE IT HEREE
            gameBoard.setCurrentMove(selected.piece.getTeam());

            PieceTeam won = null;
            if (gameBoard.getPieces(PieceTeam.Blue) == 0)       won = PieceTeam.Red;
            else if (gameBoard.getPieces(PieceTeam.Red) == 0)   won = PieceTeam.Blue;

            if (won != null) {
                EventHandler<WonGameEvent> wgeHandler = gameBoard.getEventRepository().getHandler(WonGameEvent.class);

                if (wgeHandler != null) {
                    wgeHandler.handle(new WonGameEvent(won));
                }
            }
        } else {
            // Nothing was eaten so we flip the currentMove
            if (gameBoard.getCurrentMove() == PieceTeam.Red)
                gameBoard.setCurrentMove(PieceTeam.Blue);
            else
                gameBoard.setCurrentMove(PieceTeam.Red);
        }

        selected = null;
        clearSelections();
    }

    private Optional<StackPane> safeGetPane(int column, int row) {
        if (column < 0 || column >= gameBoard.getColumns() || row < 0 || row >= gameBoard.getRows()) return Optional.empty();
        if (gameBoard.get(column, row) != null) return Optional.empty();

        return Optional.of(gameBoard.getPaneAt(column, row));
    }

    private void clearHighlight() {
        stackPane.setBackground(Constants.BACKGROUND_TILE_DEFAULT);
    }

    private double distance(int fromColumn, int fromRow, int toColumn, int toRow) {
        return Math.sqrt(Math.pow(fromColumn - toColumn, 2) + Math.pow(fromRow - toRow, 2));
    }

    // Useful for if the user clicks on another tile we can clear all the rest
    // and there aren't too many to slow down the app
    private static final List<GameMoveEventHandler> HANDLERS = new ArrayList<>();
    public static void clearSelections() {
        for (GameMoveEventHandler handler : HANDLERS) {
            handler.clearHighlight();
        }
    }

    private static final class SelectedPiece {
        private final Piece piece;
        private final int column;
        private final int row;
        private final List<StackPane> allowedMoves = new ArrayList<>();

        private SelectedPiece(Piece piece, int column, int row) {
            this.piece = piece;
            this.column = column;
            this.row = row;
        }
    }
}
