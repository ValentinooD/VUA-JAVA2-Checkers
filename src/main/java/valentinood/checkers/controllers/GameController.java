package valentinood.checkers.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import valentinood.checkers.Constants;
import valentinood.checkers.event.CurrentMoveChangedEvent;
import valentinood.checkers.event.WonGameEvent;
import valentinood.checkers.game.GameBoard;
import valentinood.checkers.game.GameBoardSnapshot;
import valentinood.checkers.game.piece.PieceTeam;
import valentinood.checkers.util.SerializationUtils;

import java.io.File;
import java.io.IOException;

public class GameController {
    @FXML
    public Text txtPlayerName;
    @FXML
    public Text txtOtherPlayerName;
    @FXML
    public GridPane gpGameBoard;

    private Stage myself;
    public GameBoard gameBoard;

    public void init(Stage stage, String username) {
        this.myself = stage;

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

    public void onClickMenuItemLoad(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load game snapshot");
        chooser.setSelectedExtensionFilter(Constants.FILTER_GAME_SNAPSHOT);
        File file = chooser.showOpenDialog(myself);

        if (file == null) return;

        try {
            GameBoardSnapshot snapshot = SerializationUtils.read(file);
            gameBoard.setSnapshot(snapshot);

        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Failed to load game");
            alert.setHeaderText("The game could not be loaded.");
            alert.setContentText(ex.toString());

            alert.show();
        }
    }

    public void onClickMenuItemSave(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save game snapshot");
        File file = chooser.showSaveDialog(myself);

        if (file == null) return;

        try {
            SerializationUtils.write(gameBoard.getSnapshot(), file);
        } catch (IOException ex) {
            ex.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Failed to save game");
            alert.setHeaderText("The game could not be saved.");
            alert.setContentText(ex.toString());

            alert.show();
        }
    }

    public void onClickMenuItemClose(ActionEvent event) {
        Platform.exit();
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
