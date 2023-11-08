module valentinood.checkers {
    requires javafx.controls;
    requires javafx.fxml;

    exports valentinood.checkers;
    exports valentinood.checkers.controllers;

    opens valentinood.checkers to javafx.fxml;
    opens valentinood.checkers.controllers to javafx.fxml;
    opens valentinood.checkers.game to javafx.fxml;
    opens valentinood.checkers.game.piece to javafx.fxml;

    exports valentinood.checkers.game;
    exports valentinood.checkers.game.piece;
    exports valentinood.checkers.event;
}