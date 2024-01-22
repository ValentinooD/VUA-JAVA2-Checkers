package valentinood.checkers.replay;

import javafx.event.Event;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import valentinood.checkers.event.*;
import valentinood.checkers.game.GameBoard;
import valentinood.checkers.replay.actions.ChangeCurrentMoveGameAction;
import valentinood.checkers.replay.actions.MovePieceGameAction;
import valentinood.checkers.replay.actions.PieceEatenGameAction;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameReplay implements EventWatcher {
    private final List<GameAction<?>> actionList = new ArrayList<>();
    private final HashMap<Class<? extends Event>, Class<? extends GameAction>> map = new HashMap<>();

    private int currentActionIndex = 0;
    private String actionsPackage;

    private GameReplay() {
        map.put(PieceMovedEvent.class, MovePieceGameAction.class);
        map.put(PieceEatenEvent.class, PieceEatenGameAction.class);
        map.put(CurrentMoveChangedEvent.class, ChangeCurrentMoveGameAction.class);
    }

    public GameReplay(File file) throws Exception {
        this();
        loadFromFile(file);
    }

    public GameReplay(EventRepository repository) {
        this();
        attachEvents(repository);
    }

    public GameAction<?> getAction() {
        return actionList.get(currentActionIndex);
    }

    public int getActionsLength() {
        return actionList.size();
    }

    public void playForward(GameBoard gameBoard) {
        // syncing just in case
        if (getAction().getPreviousState() != null)
            gameBoard.setBoard(getAction().getPreviousState().getBoard());

        getAction().play(gameBoard);
        nextAction();

        while (currentActionIndex < actionList.size() && getAction().isAutoplay()) {
            getAction().play(gameBoard);
            nextAction();
        }
    }

    public void playBackward(GameBoard gameBoard) {
        while (actionList.get(currentActionIndex - 1).isAutoplay()) {
            previousAction();
            gameBoard.setBoard(getAction().getPreviousState().getBoard());
        }

        previousAction();
        gameBoard.setBoard(getAction().getPreviousState().getBoard());
    }

    @Override
    public void received(Event event) throws Exception {
        if (!map.containsKey(event.getClass())) {
            return;
        }

        GameAction obj = (GameAction) map.get(event.getClass()).getConstructor().newInstance();
        obj.load(event);

        actionList.add(obj);
    }

    public void save(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement("game");

        Element actions = document.createElement("actions");

        if (!actionList.isEmpty()) {
            actions.setAttribute("package", actionList.get(0).getClass().getPackageName());
        }

        for (GameAction<?> action : actionList) {
            Element element = document.createElement(action.getTagName());
            element = action.createXmlElement(document, element);
            element.setAttribute("action", action.getClass().getSimpleName());

            actions.appendChild(element);
        }
        root.appendChild(actions);

        document.appendChild(root);

        saveFile(file, document);
    }

    private void loadFromFile(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
            }
        });

        Document document = builder.parse(file);
        processNode(document.getDocumentElement());
    }

    private void processNode(Node node) throws Exception {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;

            if (element.getTagName().equals("actions")) {
                actionsPackage = element.getAttribute("package") + ".";
            }

            if (element.hasAttribute("action")) {
                String className = actionsPackage + element.getAttribute("action");
                GameAction action = (GameAction) Class.forName(className).getConstructor().newInstance();
                action.parse(element);
                actionList.add(action);
            }
        }

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            processNode(childNodes.item(i));
        }
    }

    private void attachEvents(EventRepository repository) {
        repository.setWatcher(this);
    }

    private void saveFile(File file, Document document) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        transformer.transform(new DOMSource(document), new StreamResult(file));
    }

    public int getCurrentActionIndex() {
        return currentActionIndex;
    }

    public void setCurrentActionIndex(int currentActionIndex) {
        this.currentActionIndex = currentActionIndex;
    }

    public void nextAction() {
        currentActionIndex++;

        if (currentActionIndex >= actionList.size()) currentActionIndex = actionList.size();
    }

    public void previousAction() {
        currentActionIndex--;

        if (currentActionIndex < 0) currentActionIndex = 0;
    }

}
