package valentinood.checkers.replay.actions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import valentinood.checkers.event.PieceMovedEvent;
import valentinood.checkers.game.GameBoard;
import valentinood.checkers.replay.GameAction;

public class MovePieceGameAction extends GameAction<PieceMovedEvent> {
    private int fromColumn;
    private int fromRow;
    private int toColumn;
    private int tomRow;

    public MovePieceGameAction() {
        super("moved", false);
    }

    @Override
    public void _play(GameBoard gameBoard) {
        gameBoard.move(fromColumn, fromRow, toColumn, tomRow);
    }

    @Override
    public void load(PieceMovedEvent event) {
        fromColumn = event.getFromColumn();
        toColumn = event.getToColumn();

        fromRow = event.getFromRow();
        tomRow = event.getTomRow();
    }

    @Override
    public void parse(Element element) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
            Element child = (Element) childNodes.item(i);

            if (child.getTagName().equals("from")) {
                fromColumn = Integer.parseInt(child.getAttribute("column"));
                fromRow = Integer.parseInt(child.getAttribute("row"));
            } else if (child.getTagName().equals("to")) {
                toColumn = Integer.parseInt(child.getAttribute("column"));
                tomRow = Integer.parseInt(child.getAttribute("row"));
            }
        }
    }

    @Override
    public Element createXmlElement(Document document, Element element) {
        Element from = document.createElement("from");
        from.setAttribute("column", String.valueOf(fromColumn));
        from.setAttribute("row", String.valueOf(fromRow));

        Element to = document.createElement("to");
        to.setAttribute("column", String.valueOf(toColumn));
        to.setAttribute("row", String.valueOf(tomRow));

        element.appendChild(from);
        element.appendChild(to);
        return element;
    }

    @Override
    public String toString() {
        return "MovePieceGameAction{" +
                "fromColumn=" + fromColumn +
                ", fromRow=" + fromRow +
                ", toColumn=" + toColumn +
                ", tomRow=" + tomRow +
                '}';
    }
}
