package valentinood.checkers.game;

import javafx.event.EventHandler;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import valentinood.checkers.Constants;
import valentinood.checkers.event.CurrentMoveChangedEvent;
import valentinood.checkers.event.EventRepository;
import valentinood.checkers.game.events.HoverEffectEvent;
import valentinood.checkers.game.piece.Piece;
import valentinood.checkers.game.piece.PieceTeam;
import valentinood.checkers.game.piece.PieceType;

import java.util.HashMap;

public class GameBoard {
    private final GridPane gridPane;
    private final int columns;
    private final int rows;

    private final HashMap<PieceTeam, Integer> piecesCount;
    private final Piece[][] board;
    private final EventRepository eventRepository;

    private PieceTeam currentMove;  // NOTE: Should not be modified directly! Use setCurrentMove(...)

    public GameBoard(GridPane gridPane, int columns, int rows) {
        this.gridPane = gridPane;
        this.columns = columns;
        this.rows = rows;
        this.board = new Piece[rows][columns];
        this.piecesCount = new HashMap<>();
        this.eventRepository = new EventRepository();
    }

    public void initGrid() {
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(100);
        RowConstraints rc = new RowConstraints();
        rc.setPercentHeight(100);

        for (int i = 0; i < columns; i++) {
            gridPane.getColumnConstraints().add(cc);
        }
        for (int i = 0; i < rows; i++) {
            gridPane.getRowConstraints().add(rc);
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                StackPane pane = new StackPane();

                if ((i + j) % 2 == 0) {
                    pane.setBackground(Constants.BACKGROUND_TILE_LIGHT);
                } else {
                    pane.setBackground(Constants.BACKGROUND_TILE_DEFAULT);

                    pane.setOnMouseEntered(new HoverEffectEvent(pane, true));
                    pane.setOnMouseExited(new HoverEffectEvent(pane, false));
                    pane.setOnMouseClicked(new GameMoveEventHandler(this, pane, j, i));
                }

                gridPane.add(pane, j, i);
            }
        }
    }

    public void initGame() {
        // Init top
        for (int i = 0; i < 3; i++) {
            for (int j = 1; j < columns; j += 2) {
                Piece piece = new Piece(PieceType.Red);
                put(piece, (i % 2 == 0) ? j : j - 1, i);

                piecesCount.put(piece.getTeam(), piecesCount.getOrDefault(piece.getTeam(), 0) + 1);
            }
        }

        // Init bottom
        for (int i = rows - 3; i < rows; i++) {
            for (int j = 0; j < columns; j += 2) {
                Piece piece = new Piece(PieceType.Blue);
                put(piece, (i % 2 == 0) ? j + 1 : j, i);

                piecesCount.put(piece.getTeam(), piecesCount.getOrDefault(piece.getTeam(), 0) + 1);
            }
        }

        setCurrentMove(PieceTeam.Blue);
    }

    public void restart() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (get(j, i) != null) {
                    remove(j, i);
                }
            }
        }

        initGame();
    }

    public void move(int fromColumn, int fromRow, int toColumn, int toRow) {
        Piece piece = board[fromRow][fromColumn];

        if (piece == null) throw new IllegalStateException("There is no piece at " + fromColumn + ", " + fromRow);

        // Remove from
        board[fromRow][fromColumn] = null;
        getPaneAt(fromColumn, fromRow).getChildren().clear();

        // Promote to king
        if ((toRow == 0 && piece.getTeam() == PieceTeam.Blue) || (toRow == getRows() - 1 && piece.getTeam() == PieceTeam.Red)) {
            piece.setKing(true);
        }

        // Place to
        board[toRow][toColumn] = piece;
        getPaneAt(toColumn, toRow).getChildren().clear();
        getPaneAt(toColumn, toRow).getChildren().add(piece.getImageView());
    }

    public StackPane put(Piece piece, int column, int row) {
        if (board[row][column] != null)
            throw new IllegalStateException("Board already contains a piece at that location. (" + board[row][column].getType() + ")");

        board[row][column] = piece;

        getPaneAt(column, row).getChildren().clear();
        getPaneAt(column, row).getChildren().add(piece.getImageView());
        return getPaneAt(column, row);
    }

    public Piece remove(int column, int row) {
        Piece piece = board[row][column];
        piecesCount.put(piece.getTeam(), piecesCount.getOrDefault(piece.getTeam(), 1) - 1);
        board[row][column] = null;
        getPaneAt(column, row).getChildren().clear();

        return piece;
    }

    public Piece get(int column, int row) {
        return board[row][column];
    }

    public StackPane getPaneAt(int column, int row) {
        return (StackPane) gridPane.getChildren().get(row * rows + column);
    }

    public int getPieces(PieceTeam team) {
        return piecesCount.getOrDefault(team, 0);
    }

    public PieceTeam getCurrentMove() {
        return currentMove;
    }

    public EventRepository getEventRepository() {
        return eventRepository;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public void setCurrentMove(PieceTeam currentMove) {
        this.currentMove = currentMove;

        EventHandler<CurrentMoveChangedEvent> handler = getEventRepository().getHandler(CurrentMoveChangedEvent.class);
        if (handler != null) {
            handler.handle(new CurrentMoveChangedEvent(this.currentMove));
        }
    }
}
