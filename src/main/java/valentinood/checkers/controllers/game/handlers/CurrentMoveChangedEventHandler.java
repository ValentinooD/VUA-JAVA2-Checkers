package valentinood.checkers.controllers.game.handlers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import valentinood.checkers.controllers.game.GameController;
import valentinood.checkers.event.CurrentMoveChangedEvent;
import valentinood.checkers.game.piece.PieceTeam;
import valentinood.checkers.network.Network;
import valentinood.checkers.network.packet.PacketGameCurrentMove;

public class CurrentMoveChangedEventHandler implements EventHandler<CurrentMoveChangedEvent> {
    private final GameController controller;
    private final Text txtPlayerName;
    private final Text txtOtherPlayerName;

    public CurrentMoveChangedEventHandler(GameController controller, Text txtPlayerName, Text txtOtherPlayerName) {
        this.controller = controller;
        this.txtPlayerName = txtPlayerName;
        this.txtOtherPlayerName = txtOtherPlayerName;
    }

    @Override
    public void handle(CurrentMoveChangedEvent event) {
        if (event.getCurrentMove() == PieceTeam.Red) {
            txtOtherPlayerName.setStyle("-fx-font-weight: bold;");
            txtPlayerName.setStyle("-fx-font-weight: normal;");
        } else {
            txtOtherPlayerName.setStyle("-fx-font-weight: normal;");
            txtPlayerName.setStyle("-fx-font-weight: bold;");
        }

        if (controller.getNetwork() != null && event.isSendPacket())
            controller.getNetwork().sendOnThread(new PacketGameCurrentMove(event.getCurrentMove()));
    }
}
