package valentinood.checkers.game.events;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import valentinood.checkers.Constants;

public class HoverEffectEvent implements EventHandler<MouseEvent> {
    private final StackPane stackPane;
    private final boolean enter;

    public HoverEffectEvent(StackPane stackPane, boolean enter) {
        this.stackPane = stackPane;
        this.enter = enter;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        // if the color is highlighted we ignore the event, we don't want to remove the highlight color
        if (stackPane.getBackground() == Constants.BACKGROUND_TILE_HIGHLIGHTED) return;

        if (enter) {
            stackPane.setBackground(Constants.BACKGROUND_TILE_HOVER);
        } else {
            stackPane.setBackground(Constants.BACKGROUND_TILE_DEFAULT);
        }
    }
}