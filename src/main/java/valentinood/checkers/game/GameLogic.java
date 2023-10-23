package valentinood.checkers.game;

import javafx.scene.layout.StackPane;
import valentinood.checkers.game.piece.Piece;
import valentinood.checkers.game.piece.PieceTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GameLogic {
    public static List<StackPane> getAllowedMoves(GameBoard board, Piece piece, int column, int row) {
        List<StackPane> list = new ArrayList<>();

        int moveRow = 1; // Red
        if (piece.getTeam() == PieceTeam.Blue) moveRow = -1;


        // check left forward side (enemy)
        if (isEnemyAt(board, piece, column - 1, row + moveRow)
                && isPresent(board, column - 1, row + moveRow)) {
            Optional<StackPane> opt = safeGetPane(board, column - 2, row + moveRow * 2);
            opt.ifPresent(list::add);
        }

        // check right forward side (enemy)
        if (isEnemyAt(board, piece, column + 1, row + moveRow)
                && isPresent(board, column + 1, row + moveRow)) {
            Optional<StackPane> opt = safeGetPane(board, column + 2, row + moveRow * 2);
            opt.ifPresent(list::add);
        }

        if (piece.isKing()) {
            // check left backward side (enemy)
            if (isEnemyAt(board, piece, column - 1, row - moveRow)
                    && isPresent(board, column - 1, row - moveRow)) {
                Optional<StackPane> opt = safeGetPane(board, column - 2, row - moveRow * 2);
                opt.ifPresent(list::add);
            }

            // check right backward side (enemy)
            if (isEnemyAt(board, piece, column + 1, row - moveRow)
                    && isPresent(board, column + 1, row - moveRow)) {
                Optional<StackPane> opt = safeGetPane(board, column + 2, row - moveRow * 2);
                opt.ifPresent(list::add);
            }
        }

        // According to most official rules, when capturing is possible it must be done
        if (list.size() == 0 && !isOtherCapturePossible(board, piece, column, row)) {
            // check left and right side (nobody)
            Optional<StackPane> left = safeGetPane(board, column - 1, row + moveRow);
            Optional<StackPane> right = safeGetPane(board, column + 1, row + moveRow);
            left.ifPresent(list::add);
            right.ifPresent(list::add);

            if (piece.isKing()) {
                // check left and right side (nobody)
                left = safeGetPane(board, column - 1, row - moveRow);
                right = safeGetPane(board, column + 1, row - moveRow);
                left.ifPresent(list::add);
                right.ifPresent(list::add);
            }
        }

        return list;
    }

    private static boolean isOtherCapturePossible(GameBoard board, Piece piece, int ignoreColumn, int ignoreRow) {
        int moveRow = 1; // Red
        if (piece.getTeam() == PieceTeam.Blue) moveRow = -1;

        for (int column = 0; column < board.getColumns(); column++) {
            for (int row = 0; row < board.getRows(); row++) {
                if (column == ignoreColumn && row == ignoreRow) continue;

                Piece piece2 = board.get(column, row);
                if (piece2 == null) continue;
                if (piece2.getTeam() != piece.getTeam()) continue;

                /*
                isEnemyAt - checks if the piece right next to it is an enemy
                isPresent - checks if there is an actual piece, since isFriendlyAt will return false if there's no piece (because there's no friendly)
                !isPresent - checks if there is no piece 2 tiles away from it. if it's free it means there's a spot to jump
                isInBoundary - checks that the piece 2 tiles away is in a valid location and not outside the board (since isPresent will return false if it's outside)

                All functions are safe to run with illegal parameters, but they may return unusual results hence why it's necessary to have multiple checks.
                I'd rather have safe functions in this case and cleaner code instead of having to check every single parameter before sending it.
                 */

                // check left forward side (enemy)
                if (isEnemyAt(board, piece, column - 1, row + moveRow)
                        && isPresent(board, column - 1, row + moveRow)
                        && !isPresent(board, column - 2, row + moveRow*2)
                        && isInBoundary(board, column - 2, row + moveRow*2)) {
                    return true;
                }

                // check right forward side (enemy)
                if (isEnemyAt(board, piece, column + 1, row + moveRow)
                        && isPresent(board, column + 1, row + moveRow)
                        && !isPresent(board, column + 2, row + moveRow*2)
                        && isInBoundary(board, column + 2, row + moveRow*2)) {
                    return true;
                }

                if (piece2.isKing()) {
                    // check left backwards side (enemy)
                    if (isEnemyAt(board, piece, column - 1, row - moveRow)
                            && isPresent(board, column - 1, row - moveRow)
                            && !isPresent(board, column - 2, row - moveRow*2)
                            && isInBoundary(board, column - 2, row - moveRow*2)) {
                        return true;
                    }

                    // check right backwards side (enemy)
                    if (isEnemyAt(board, piece, column + 1, row - moveRow)
                            && isPresent(board, column + 1, row - moveRow)
                            && !isPresent(board, column + 2, row - moveRow*2)
                            && isInBoundary(board, column + 2, row - moveRow*2)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static Optional<StackPane> safeGetPane(GameBoard board, int column, int row) {
        if (!isInBoundary(board, column, row)) return Optional.empty();
        if (board.get(column, row) != null) return Optional.empty();

        return Optional.of(board.getPaneAt(column, row));
    }

    private static boolean isEnemyAt(GameBoard board, Piece me, int column, int row) {
        if (!isInBoundary(board, column, row)) return true;
        if (board.get(column, row) == null) return true;
        return board.get(column, row).getTeam() != me.getTeam();
    }

    private static boolean isPresent(GameBoard board, int column, int row) {
        if (!isInBoundary(board, column, row)) return false;
        return board.get(column, row) != null;
    }

    private static boolean isInBoundary(GameBoard board, int column, int row) {
        return !(column < 0 || column >= board.getColumns() || row < 0 || row >= board.getRows());
    }

    private GameLogic() {
    }
}
