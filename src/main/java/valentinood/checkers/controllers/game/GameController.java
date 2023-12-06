package valentinood.checkers.controllers.game;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import valentinood.checkers.Constants;
import valentinood.checkers.controllers.game.handlers.CurrentMoveChangedEventHandler;
import valentinood.checkers.controllers.game.handlers.PieceEatenEventHandler;
import valentinood.checkers.controllers.game.handlers.PieceMovedEventHandler;
import valentinood.checkers.controllers.game.handlers.WonGameEventHandler;
import valentinood.checkers.controllers.packet.PacketHandlerDisconnected;
import valentinood.checkers.event.WonGameEvent;
import valentinood.checkers.game.GameBoard;
import valentinood.checkers.game.GameBoardSnapshot;
import valentinood.checkers.game.piece.PieceTeam;
import valentinood.checkers.network.Network;
import valentinood.checkers.network.PacketListener;
import valentinood.checkers.network.annotations.OnFXThread;
import valentinood.checkers.network.packet.*;
import valentinood.checkers.util.AlertUtils;
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

        this.gameBoard.getEventRepository().register(new WonGameEventHandler(this));
        this.gameBoard.getEventRepository().register(new CurrentMoveChangedEventHandler(this, txtPlayerName, txtOtherPlayerName));
        this.gameBoard.getEventRepository().register(new PieceMovedEventHandler(this));
        this.gameBoard.getEventRepository().register(new PieceEatenEventHandler(this));

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

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                if (network != null) network.stop();
            }
        });
    }

    public void connect(Network network, PacketGameBegin begin) {
        this.network = network;

        this.gameBoard.setUnplayableSide(begin.getPlayerTeam() == PieceTeam.Blue ? PieceTeam.Red : PieceTeam.Blue);

        txtOtherPlayerName.setText(begin.getTeams().get(PieceTeam.Red));
        txtPlayerName.setText(begin.getTeams().get(PieceTeam.Blue));

        // note: PacketHandlerDisconnected is already registered before
        this.network.register(new PacketReceiveMessage());
        this.network.register(new PacketCurrentMove());
        this.network.register(new PacketPieceEaten());
        this.network.register(new PacketPieceMoved());
        this.network.register(new PacketGameEndListener());
        this.network.register(new PacketHandlerDisconnected());
    }

    public void restart() {
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
            AlertUtils.error("Failed to load game", "The game could not be loaded", ex);
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

            AlertUtils.error("Failed to save game", "The game could not be saved", ex);
        }
    }

    public Network getNetwork() {
        return network;
    }

    public void onClickMenuItemClose(ActionEvent event) {
        Platform.exit();
    }

    @OnFXThread
    private class PacketGameEndListener implements PacketListener<PacketGameEnd> {
        @Override
        public void received(PacketGameEnd packet) throws Exception {
            EventHandler<WonGameEvent> wgeHandler = gameBoard.getEventRepository().getHandler(WonGameEvent.class);

            if (wgeHandler != null) {
                wgeHandler.handle(new WonGameEvent(packet.getWinner(), false));
            }
        }
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
            Platform.runLater(() -> gameBoard.setCurrentMove(packet.getCurrentMove(), false));
        }
    }

    private class PacketReceiveMessage implements PacketListener<PacketGameChatMessage> {
        @Override
        public void received(PacketGameChatMessage packet) throws Exception {
            Platform.runLater(() -> lstChat.getItems().add(new Text(packet.getMessage())));
        }
    }
}
