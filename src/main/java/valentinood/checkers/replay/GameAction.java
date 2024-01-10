package valentinood.checkers.replay;

import javafx.event.Event;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import valentinood.checkers.game.GameBoard;
import valentinood.checkers.game.GameBoardSnapshot;

public abstract class GameAction<T extends Event> {

    private final String tagName;
    private final boolean autoplay;

    private GameBoardSnapshot previousState;

    /**
     * Should always contain one empty constructor!
     * @param tagName
     * @param autoplay
     */
    public GameAction(String tagName, boolean autoplay) {
        this.tagName = tagName;
        this.autoplay = autoplay;
    }

    public String getTagName() {
        return tagName;
    }

    public boolean isAutoplay() {
        return autoplay;
    }

    public GameBoardSnapshot getPreviousState() {
        return previousState;
    }

    public void play(GameBoard gameBoard) {
        previousState = gameBoard.getSnapshot();

        _play(gameBoard);
    }

    /**
     * Plays next move on the GameBoard
     * @param gameBoard
     */
    public abstract void _play(GameBoard gameBoard);

    /**
     * Loads from event
     * @param event
     */
    public abstract void load(T event);

    /**
     * Loads from element
     * @param element
     */
    public abstract void parse(Element element);

    /**
     * @param document
     * @param element
     * @return Element to be inserted into document
     */
    public abstract Element createXmlElement(Document document, Element element);
}
