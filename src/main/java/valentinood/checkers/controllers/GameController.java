package valentinood.checkers.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import valentinood.checkers.Constants;
import valentinood.checkers.event.CurrentMoveChangedEvent;
import valentinood.checkers.event.PieceEatenEvent;
import valentinood.checkers.event.PieceMovedEvent;
import valentinood.checkers.event.WonGameEvent;
import valentinood.checkers.game.GameBoard;
import valentinood.checkers.game.GameBoardSnapshot;
import valentinood.checkers.game.piece.PieceTeam;
import valentinood.checkers.network.Network;
import valentinood.checkers.network.PacketListener;
import valentinood.checkers.network.packet.*;
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

    @FXML
    public ListView<Text> lstChat;
    @FXML
    public TextField tfChatInput;

    private Network network;

    private Stage myself;
    public GameBoard gameBoard;

    public void init(Stage stage, String username, int columns, int rows) {
        this.myself = stage;

        txtPlayerName.setText(username);
        txtOtherPlayerName.setText(username + "'s archnemesis");

        this.gameBoard = new GameBoard(gpGameBoard, columns, rows);
        this.gameBoard.initGrid();

        this.gameBoard.getEventRepository().register(new WonGameEventHandler());
        this.gameBoard.getEventRepository().register(new CurrentMoveChanged());
        this.gameBoard.getEventRepository().register(new PieceMovedEventHandler());
        this.gameBoard.getEventRepository().register(new PieceEatenEventHandler());

        this.gameBoard.initGame();

        this.tfChatInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (tfChatInput.getText().isBlank()) return;

                if (event.getCode() == KeyCode.ENTER) {
                    network.sendOnThread(new PacketGameChatMessage(tfChatInput.getText()));
                    lstChat.getItems().add(new Text(tfChatInput.getText()));

                    tfChatInput.setText("");
                }
            }
        });
    }

    public void connect(Network network, PacketGameBegin begin) {
        this.network = network;

        this.gameBoard.setUnplayableSide(begin.getPlayerTeam() == PieceTeam.Blue ? PieceTeam.Red : PieceTeam.Blue);

        txtOtherPlayerName.setText(begin.getTeams().get(PieceTeam.Red));
        txtPlayerName.setText(begin.getTeams().get(PieceTeam.Blue));

        this.network.register(new PacketReceiveMessage());
        this.network.register(new PacketCurrentMove());
        this.network.register(new PacketPieceEaten());
        this.network.register(new PacketPieceMoved());
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

    private class PacketPieceEaten implements PacketListener<PacketGamePieceEaten> {
        @Override
        public void received(PacketGamePieceEaten packet) throws Exception {
            Platform.runLater(() -> gameBoard.remove(packet.getColumn(), packet.getRow()));
        }
    }

    private class PacketPieceMoved implements PacketListener<PacketGamePieceMove> {
        @Override
        public void received(PacketGamePieceMove packet) throws Exception {
            Platform.runLater(() -> gameBoard.move(packet.getFromColumn(), packet.getFromRow(), packet.getToColumn(), packet.getTomRow()));
        }
    }

    private class PacketCurrentMove implements PacketListener<PacketGameCurrentMove> {
        @Override
        public void received(PacketGameCurrentMove packet) throws Exception {
            Platform.runLater(() -> gameBoard.setCurrentMove(packet.getCurrentMove(), true));
        }
    }

    private class PacketReceiveMessage implements PacketListener<PacketGameChatMessage> {
        @Override
        public void received(PacketGameChatMessage packet) throws Exception {
            Platform.runLater(() -> lstChat.getItems().add(new Text(packet.getMessage())));
        }
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

            if (network != null)
                network.sendOnThread(new PacketGameCurrentMove(event.getCurrentMove()));
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

    private class PieceMovedEventHandler implements EventHandler<PieceMovedEvent> {
        @Override
        public void handle(PieceMovedEvent event) {
            if (network != null)
                network.sendOnThread(new PacketGamePieceMove(
                        event.getFromColumn(),
                        event.getFromRow(),
                        event.getToColumn(),
                        event.getTomRow()
                ));
        }
    }

    private class PieceEatenEventHandler implements EventHandler<PieceEatenEvent> {
        @Override
        public void handle(PieceEatenEvent event) {
            if (network != null)
                network.sendOnThread(new PacketGamePieceEaten(event.getColumn(), event.getRow()));
        }
    }
}
