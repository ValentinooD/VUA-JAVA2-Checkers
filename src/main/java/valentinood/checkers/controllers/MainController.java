package valentinood.checkers.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import valentinood.checkers.CheckersApplication;
import valentinood.checkers.docs.ProjectDocumentation;
import valentinood.checkers.game.GameBoardSnapshot;

import java.io.File;
import java.io.IOException;

public class MainController {
    @FXML
    public TextField tfUsername;
    @FXML
    public Text txtInfo;

    private GameBoardSnapshot snapshot = null;
    private Stage myself;

    @FXML
    public void btnPlayGame() {
        String username = tfUsername.getText();
        if (username.isBlank()) username = "Player";

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(CheckersApplication.class.getResource("views/game-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Checkers");
            stage.setScene(scene);
            stage.setResizable(false);

            GameController controller = fxmlLoader.getController();
            controller.init(stage, username);

            stage.show();
            myself.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void onClickMenuItemClose(ActionEvent event) {
        Platform.exit();
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
