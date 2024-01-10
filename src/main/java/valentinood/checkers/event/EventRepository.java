package valentinood.checkers.event;

import javafx.event.Event;
import javafx.event.EventHandler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventRepository {
    private final HashMap<Class<? extends Event>, List<EventHandler<? extends Event>>> map;
    private EventWatcher watcher = null;

    public EventRepository() {
        this.map = new HashMap<>();
    }

    public void register(EventHandler<? extends Event> handler) {
        Class<?> handlerClass = handler.getClass();
        ParameterizedType pt = (ParameterizedType) handlerClass.getGenericInterfaces()[0];
        Type actualType = pt.getActualTypeArguments()[0];
        Class<? extends Event> clazz = (Class<? extends Event>) actualType;

        List<EventHandler<? extends Event>> list = map.getOrDefault(clazz, new ArrayList<>());
        list.add(handler);
        map.put(clazz, list);
    }

    public <T extends Event> void call(Event event) {
        for (EventHandler handler : map.getOrDefault(event.getClass(), new ArrayList<>())) {
            try {
                handler.handle(event);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            if (watcher != null) watcher.received(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Deprecated
    public <T extends Event> EventHandler<T> getHandler(Class<T> clazz) {
        return (EventHandler<T>) map.get(clazz).get(0);
    }

    public EventWatcher getWatcher() {
        return watcher;
    }

    public void setWatcher(EventWatcher watcher) {
        this.watcher = watcher;
    }
}
