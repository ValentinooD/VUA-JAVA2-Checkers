package valentinood.checkers.controllers;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import valentinood.checkers.event.CurrentMoveChangedEvent;
import valentinood.checkers.event.WonGameEvent;
import valentinood.checkers.game.GameBoard;
import valentinood.checkers.game.piece.PieceTeam;

public class GameController {
    @FXML
    public Text txtPlayerName;
    @FXML
    public Text txtOtherPlayerName;
    @FXML
    public GridPane gpGameBoard;

    public GameBoard gameBoard;

    public void init(String username) {
        txtPlayerName.setText(username);
        txtOtherPlayerName.setText(username + "'s archnemesis");

        this.gameBoard = new GameBoard(gpGameBoard, 8, 8);
        this.gameBoard.initGrid();

        this.gameBoard.getEventRepository().register(new WonGameEventHandler());
        this.gameBoard.getEventRepository().register(new CurrentMoveChanged());

        this.gameBoard.initGame();
    }

    private void restart() {
        this.gameBoard.restart();
    }

    private class CurrentMoveChanged implements EventHandler<CurrentMoveChangedEvent> {
        @Override
        public void handle(CurrentMoveChangedEvent event) {
            if (event.getCurrentMove() == PieceTeam.Red) {
                txtOtherPlayerName.setStyle("-fx-font-weight: bold;");
                txtPlayerName.setStyle("-fx-font-weight: normal;");
            } else {
                txtOtherPlayerName.setStyle("-fx-font-weight: normal;");
                txtPlayerName.setStyle("-fx-font-weight: bold;");
            }
        }
    }

    private class WonGameEventHandler implements EventHandler<WonGameEvent> {
        @Override
        public void handle(WonGameEvent wonGameEvent) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Game ended");
            alert.setHeaderText(wonGameEvent.getWhoWon().getPrettyText() + " has won the game.");
            alert.setContentText("Do you wish to restart the game with the same opponent?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                restart();
            } else {
                Platform.exit();
            }
        }
    }

}
