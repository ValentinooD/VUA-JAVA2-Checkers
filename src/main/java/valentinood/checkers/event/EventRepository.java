package valentinood.checkers.event;

import javafx.event.Event;
import javafx.event.EventHandler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

public class EventRepository {
    private final HashMap<Class<? extends Event>, EventHandler<? extends Event>> map;

    public EventRepository() {
        this.map = new HashMap<>();
    }

    public void register(EventHandler<? extends Event> handler) {
        Class<?> handlerClass = handler.getClass();
        ParameterizedType pt = (ParameterizedType) handlerClass.getGenericInterfaces()[0];
        Type actualType = pt.getActualTypeArguments()[0];
        Class<? extends Event> clazz = (Class<? extends Event>) actualType;

        if (map.containsKey(clazz)) {
            return; // will not register (throw exception?)
        }

        map.put(clazz, handler);
    }

    public <T extends Event> EventHandler<T> getHandler(Class<T> clazz) {
        return (EventHandler<T>) map.get(clazz);
    }
}
