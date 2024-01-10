package valentinood.checkers.controllers.game;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
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
import valentinood.checkers.replay.GameReplay;
import valentinood.checkers.network.Network;
import valentinood.checkers.network.PacketListener;
import valentinood.checkers.network.annotations.OnFXThread;
import valentinood.checkers.network.packet.*;
import valentinood.checkers.util.AlertUtils;
import valentinood.checkers.util.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

    @FXML
    public Label txtUnavailable;
    @FXML
    public Button btnBack;
    @FXML
    public Button btnForward;

    private boolean isReplaying = false;
    private GameReplay replay;

    private Network network;

    private Stage myself;
    public GameBoard gameBoard;
    private Timeline timeline;

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

        this.replay = new GameReplay(gameBoard.getEventRepository());
        setReplaying(false);

        this.tfChatInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (tfChatInput.getText().isBlank()) return;

                if (event.getCode() == KeyCode.ENTER) {
                    network.chat(username + ": " + tfChatInput.getText());
                    tfChatInput.setText("");
                }
            }
        });
        tfChatInput.setDisable(true);
        tfChatInput.setText("You cannot chat in single-player mode.");
        lstChat.getItems().clear();
        lstChat.getItems().add(new Text("Chat is not available in single-player mode."));

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                if (network != null) network.stop();

                try {
                    replay.save(new File("./replay.xml"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void replay(GameReplay replay) {
        this.replay = replay;
        setReplaying(true);

        // since we're at the beginning
        btnBack.setDisable(true);
    }

    public void connect(Network network, PacketGameBegin begin) {
        if (isReplaying) {
            throw new IllegalStateException("Unable to connect while replaying");
        }

        this.network = network;

        this.gameBoard.setUnplayableSide(begin.getPlayerTeam() == PieceTeam.Blue ? PieceTeam.Red : PieceTeam.Blue);

        txtOtherPlayerName.setText(begin.getTeams().get(PieceTeam.Red));
        txtPlayerName.setText(begin.getTeams().get(PieceTeam.Blue));

        lstChat.getItems().clear();
        tfChatInput.setDisable(false);
        tfChatInput.setText("");

        // note: PacketHandlerDisconnected is already registered before
        this.network.register(new PacketCurrentMove());
        this.network.register(new PacketPieceEaten());
        this.network.register(new PacketPieceMoved());
        this.network.register(new PacketGameEndListener());
        this.network.register(new PacketHandlerDisconnected());

        timeline = new Timeline(new KeyFrame(Duration.millis(1000), event -> updateChat()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }


    public void setReplaying(boolean replaying) {
        isReplaying = replaying;

        txtUnavailable.setVisible(!replaying);
        btnBack.setVisible(replaying);
        btnForward.setVisible(replaying);

        gameBoard.setPlayable(!replaying);

        if (replaying) {
            tfChatInput.setDisable(true);
            tfChatInput.setText("You cannot chat in replay mode.");
            lstChat.getItems().clear();
            lstChat.getItems().add(new Text("Chat is not available in replay mode."));

            txtPlayerName.setText("Blue");
            txtOtherPlayerName.setText("Red");
        }
    }

    public void onClickButtonForward(ActionEvent event) {
        replay.playForward(gameBoard);

        btnForward.setDisable(replay.getCurrentActionIndex() >= replay.getActionsLength());
        btnBack.setDisable(replay.getCurrentActionIndex() == 0);
    }

    public void onClickButtonBack(ActionEvent event) {
        replay.playBackward(gameBoard);

        btnForward.setDisable( replay.getCurrentActionIndex() >= replay.getActionsLength());
        btnBack.setDisable(replay.getCurrentActionIndex() == 0);
    }

    public void restart() {
        this.gameBoard.restart();
    }

    private void updateChat() {
        List<String> chatMessages = null;
        try {
            chatMessages = Network.remoteChatService.getMessages();
        } catch (Exception e) {
            tfChatInput.setDisable(true);
            lstChat.getItems().clear();
            lstChat.getItems().add(new Text("Chat is not available right now because the chat service is offline."));
            timeline.stop();

            e.printStackTrace();
            return;
        }

        lstChat.getItems().clear();
        for (String message : chatMessages) {
            lstChat.getItems().add(new Text(message));
        }
    }

    public void onClickMenuItemLoad(ActionEvent event) {
        if (network != null) {
            AlertUtils.information("Unavailable", "Loading saved games is not possible in multiplayer-games.");
            return;
        }

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

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public void onClickMenuItemClose(ActionEvent event) {
        Platform.exit();
    }

    @OnFXThread
    private class PacketGameEndListener implements PacketListener<PacketGameEnd> {
        @Override
        public void received(PacketGameEnd packet) throws Exception {
            gameBoard.getEventRepository().call(new WonGameEvent(packet.getWinner(), false));
        }
    }

    @OnFXThread
    private class PacketPieceEaten implements PacketListener<PacketGamePieceEaten> {
        @Override
        public void received(PacketGamePieceEaten packet) throws Exception {
            gameBoard.remove(packet.getColumn(), packet.getRow());
        }
    }

    @OnFXThread
    private class PacketPieceMoved implements PacketListener<PacketGamePieceMove> {
        @Override
        public void received(PacketGamePieceMove packet) throws Exception {
            gameBoard.move(packet.getFromColumn(), packet.getFromRow(), packet.getToColumn(), packet.getTomRow());
        }
    }

    @OnFXThread
    private class PacketCurrentMove implements PacketListener<PacketGameCurrentMove> {
        @Override
        public void received(PacketGameCurrentMove packet) throws Exception {
            gameBoard.setCurrentMove(packet.getCurrentMove(), false);
        }
    }
}
