module valentinood.checkers {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    requires java.naming;
    requires java.xml;

    exports valentinood.checkers;
    exports valentinood.checkers.controllers;


    opens valentinood.checkers to javafx.fxml;
    opens valentinood.checkers.controllers to javafx.fxml;
    opens valentinood.checkers.game to javafx.fxml;
    opens valentinood.checkers.game.piece to javafx.fxml;

    exports valentinood.checkers.game;
    exports valentinood.checkers.game.piece;
    exports valentinood.checkers.event;

    exports valentinood.checkers.network;
    exports valentinood.checkers.network.packet;
    exports valentinood.checkers.network.server;
    exports valentinood.checkers.controllers.game;
    opens valentinood.checkers.controllers.game to javafx.fxml;
    exports valentinood.checkers.controllers.game.handlers;
    opens valentinood.checkers.controllers.game.handlers to javafx.fxml;
    exports valentinood.checkers.network.annotations;
    exports valentinood.checkers.network.rmi;
}