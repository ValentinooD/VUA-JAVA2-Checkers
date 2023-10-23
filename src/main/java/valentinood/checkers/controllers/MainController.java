package valentinood.checkers.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import valentinood.checkers.CheckersApplication;

import java.io.IOException;

public class MainController {
    @FXML
    public TextField tfUsername;

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
            controller.init(username);

            stage.show();
            myself.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setStage(Stage myself) {
        this.myself = myself;
    }
}
