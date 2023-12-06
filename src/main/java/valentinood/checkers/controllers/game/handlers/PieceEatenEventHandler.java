package valentinood.checkers.controllers.game.handlers;

import javafx.event.EventHandler;
import valentinood.checkers.controllers.game.GameController;
import valentinood.checkers.event.PieceEatenEvent;
import valentinood.checkers.network.packet.PacketGamePieceEaten;

public class PieceEatenEventHandler implements EventHandler<PieceEatenEvent> {
    private final GameController controller;

    public PieceEatenEventHandler(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void handle(PieceEatenEvent event) {
        if (controller.getNetwork() != null)
            controller.getNetwork().sendOnThread(new PacketGamePieceEaten(event.getColumn(), event.getRow()));
    }
}
