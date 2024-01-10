package valentinood.checkers.replay.actions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import valentinood.checkers.event.CurrentMoveChangedEvent;
import valentinood.checkers.game.GameBoard;
import valentinood.checkers.game.piece.PieceTeam;
import valentinood.checkers.replay.GameAction;

public class ChangeCurrentMoveGameAction extends GameAction<CurrentMoveChangedEvent> {

    private PieceTeam currentMove;

    public ChangeCurrentMoveGameAction() {
        super("current", true);
    }

    @Override
    public void _play(GameBoard gameBoard) {
        gameBoard.setCurrentMove(currentMove);
    }

    @Override
    public void load(CurrentMoveChangedEvent event) {
        currentMove = event.getCurrentMove();
    }

    @Override
    public void parse(Element element) {
        currentMove = PieceTeam.valueOf(element.getAttribute("team"));
    }

    @Override
    public Element createXmlElement(Document document, Element element) {
        element.setAttribute("team", currentMove.name());
        return element;
    }
}
