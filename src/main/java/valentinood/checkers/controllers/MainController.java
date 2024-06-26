package valentinood.checkers.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import valentinood.checkers.CheckersApplication;
import valentinood.checkers.Constants;
import valentinood.checkers.controllers.game.GameController;
import valentinood.checkers.controllers.packet.PacketHandlerDisconnected;
import valentinood.checkers.docs.ProjectDocumentation;
import valentinood.checkers.game.GameBoardSnapshot;
import valentinood.checkers.network.Network;
import valentinood.checkers.network.PacketListener;
import valentinood.checkers.network.jndi.ConfigurationReader;
import valentinood.checkers.network.packet.PacketConnectionRequest;
import valentinood.checkers.network.packet.PacketConnectionResult;
import valentinood.checkers.network.packet.PacketGameBegin;
import valentinood.checkers.replay.GameReplay;
import valentinood.checkers.util.AlertUtils;
import valentinood.checkers.util.SerializationUtils;

import java.io.File;
import java.io.IOException;

public class MainController {
    @FXML
    public TextField tfUsername;
    @FXML
    public Button btnPlay;
    @FXML
    public CheckBox cbLocalGame;
    @FXML
    public Text txtInfo;

    private GameReplay replay = null;

    private Network network = null;
    private String username;
    private Stage myself;

    @FXML
    public void btnPlayGame() {
        username = tfUsername.getText();
        if (username.isBlank()) username = "Player";

        if (cbLocalGame.isSelected()) {
            startGame(null);
            return;
        }

        try {
            tfUsername.setDisable(true);
            cbLocalGame.setDisable(true);
            btnPlay.setDisable(true);

            network = new Network(ConfigurationReader.getInt(ConfigurationReader.Key.SERVER_PORT));
            network.register(new PacketListener<PacketConnectionResult>() {
                @Override
                public void received(PacketConnectionResult packet) throws Exception {
                    if (packet.getResult().isAllowed()) {
                        txtInfo.setText("Connected! Waiting for other player...");
                    }
                }
            });

            network.register(new PacketListener<PacketGameBegin>() {
                @Override
                public void received(PacketGameBegin packet) throws Exception {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            startGame(packet);
                        }
                    });
                }
            });

            network.register(new PacketHandlerDisconnected());

            txtInfo.setText("Starting server...");
            network.startOnThread();

            txtInfo.setText("Connecting to server...");
            network.sendOnThread(new PacketConnectionRequest(username, 8, 8));
        } catch (Exception ex) {
            txtInfo.setText("Failed to start server.");
            ex.printStackTrace();
        }
    }

    private void startGame(PacketGameBegin begin) {
        if (replay != null && begin != null) throw new IllegalStateException("Can't replay and play in multiplayer at the same time.");

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(CheckersApplication.class.getResource("views/game-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Checkers");
            stage.setScene(scene);
            stage.setResizable(false);

            int columns = 8;
            int rows = 8;

            if (begin != null) {
                columns = begin.getColumns();
                rows = begin.getRows();
            }

            GameController controller = fxmlLoader.getController();
            controller.init(stage, username, columns, rows);

            if (replay != null) {
                controller.replay(replay);
            }

            if (begin != null) {
                controller.connect(network, begin);
            }

            stage.show();
            myself.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void onClickLoadReplayMenuItem(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load replay");
        chooser.setInitialDirectory(new File("."));
        chooser.setSelectedExtensionFilter(Constants.FILTER_GAME_REPLAY);
        File file = chooser.showOpenDialog(myself);

        if (file == null) return;

        try {
            replay = new GameReplay(file);
            txtInfo.setText("Game replay loaded. Starting game...");
            startGame(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtils.error("Failed to load replay", "The replay could not be loaded", ex);
        }
    }

    public void onClickMenuItemClose(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    public void onClickMenuItemGenerateDocs(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Generate documentation");
        File file = chooser.showSaveDialog(myself);

        if (file == null) return;

        ProjectDocumentation documentation = new ProjectDocumentation(file.getAbsolutePath());
        documentation.save();
    }

    public void setStage(Stage myself) {
        this.myself = myself;

        initStageEvents();
    }

    private void initStageEvents() {
        myself.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                if (network != null) {
                    network.stop();
                }
            }
        });
    }
}
