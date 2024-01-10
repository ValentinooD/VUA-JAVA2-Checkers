package valentinood.checkers.replay.actions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import valentinood.checkers.event.PieceEatenEvent;
import valentinood.checkers.game.GameBoard;
import valentinood.checkers.replay.GameAction;

public class PieceEatenGameAction extends GameAction<PieceEatenEvent> {

    private int column;
    private int row;

    public PieceEatenGameAction() {
        super("eaten", true);
    }

    @Override
    public void _play(GameBoard gameBoard) {
        gameBoard.remove(column, row);
    }

    @Override
    public void load(PieceEatenEvent event) {
        column = event.getColumn();
        row = event.getRow();
    }

    @Override
    public void parse(Element element) {
        column = Integer.parseInt(element.getAttribute("column"));
        row = Integer.parseInt(element.getAttribute("row"));
    }

    @Override
    public Element createXmlElement(Document document, Element element) {
        element.setAttribute("row", String.valueOf(row));
        element.setAttribute("column", String.valueOf(column));
        return element;
    }

    @Override
    public String toString() {
        return "PieceEatenGameAction{" +
                "column=" + column +
                ", row=" + row +
                '}';
    }
}
