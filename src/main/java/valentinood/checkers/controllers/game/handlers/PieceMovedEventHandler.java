package valentinood.checkers.controllers.game.handlers;

import javafx.event.EventHandler;
import valentinood.checkers.controllers.game.GameController;
import valentinood.checkers.event.PieceMovedEvent;
import valentinood.checkers.network.packet.PacketGamePieceMove;

public class PieceMovedEventHandler implements EventHandler<PieceMovedEvent> {
    private final GameController controller;

    public PieceMovedEventHandler(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void handle(PieceMovedEvent event) {
        if (controller.getNetwork() != null) {
            controller.getNetwork().sendOnThread(new PacketGamePieceMove(
                    event.getFromColumn(),
                    event.getFromRow(),
                    event.getToColumn(),
                    event.getTomRow()
            ));
        }

    }
}
