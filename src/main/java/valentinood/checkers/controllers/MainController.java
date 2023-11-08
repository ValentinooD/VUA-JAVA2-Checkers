package valentinood.checkers.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import valentinood.checkers.CheckersApplication;
import valentinood.checkers.docs.ProjectDocumentation;
import valentinood.checkers.game.GameBoardSnapshot;
import valentinood.checkers.network.Network;
import valentinood.checkers.network.PacketListener;
import valentinood.checkers.network.packet.Packet;
import valentinood.checkers.network.packet.PacketConnectionRequest;
import valentinood.checkers.network.packet.PacketConnectionResult;
import valentinood.checkers.network.packet.PacketGameBegin;

import java.io.File;
import java.io.IOException;

public class MainController {
    @FXML
    public TextField tfUsername;
    @FXML
    public TextField tfPort;
    @FXML
    public Button btnPlay;

    @FXML
    public Text txtInfo;

    private Network network = null;
    private String username;
    private Stage myself;

    @FXML
    public void btnPlayGame() {
        username = tfUsername.getText();
        if (username.isBlank()) username = "Player";

        // Playing single-player
        if (tfPort.getText().isBlank()) {
            startGame(null);
            return;
        }

        try {
            tfUsername.setDisable(true);
            tfPort.setDisable(true);
            btnPlay.setDisable(true);

            // TODO: check if the user didn't enter letters
            network = new Network(Integer.parseInt(tfPort.getText()));
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
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(CheckersApplication.class.getResource("views/game-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Checkers");
            stage.setScene(scene);
            stage.setResizable(false);

            int columns = 8;
            int rows = 0;

            if (begin != null) {
                columns = begin.getColumns();
                rows = begin.getRows();
            }

            GameController controller = fxmlLoader.getController();
            controller.init(stage, username, columns, rows);

            if (begin != null) {
                controller.connect(network, begin);
            }

            stage.show();
            myself.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
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
    }
}
