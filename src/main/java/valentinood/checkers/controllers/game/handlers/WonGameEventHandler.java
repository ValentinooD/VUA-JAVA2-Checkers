package valentinood.checkers.controllers.game.handlers;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import valentinood.checkers.controllers.game.GameController;
import valentinood.checkers.event.WonGameEvent;
import valentinood.checkers.network.packet.PacketConnectionDisconnect;
import valentinood.checkers.network.packet.PacketGameEnd;
import valentinood.checkers.util.AlertUtils;

public class WonGameEventHandler implements EventHandler<WonGameEvent> {
    private final GameController controller;

    public WonGameEventHandler(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void handle(WonGameEvent wonGameEvent) {
        if (wonGameEvent.isSendPacket() && controller.getNetwork() != null)
            controller.getNetwork().sendOnThread(new PacketGameEnd(wonGameEvent.getWhoWon()));

        if (controller.getNetwork() == null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Game ended");
            alert.setHeaderText(wonGameEvent.getWhoWon().getPrettyText() + " has won the game.");
            alert.setContentText("Do you wish to restart the game with the same opponent?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                controller.restart();
            } else {
                Platform.exit();
            }
        } else {
            AlertUtils.information("Game ended", wonGameEvent.getWhoWon().getPrettyText() + " has won the game.");

            controller.getNetwork().send(new PacketConnectionDisconnect("Game ended"));

            Platform.exit();
        }
    }
}
