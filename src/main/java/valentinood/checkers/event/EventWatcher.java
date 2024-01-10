package valentinood.checkers.event;

import javafx.event.Event;

public interface EventWatcher {
    void received(Event event) throws Exception;
}
