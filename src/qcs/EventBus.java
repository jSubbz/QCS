package qcs;
import qcs.EventBus;
import java.util.ArrayList;
import java.util.List;

public class EventBus {
    public interface EventListener {
        void onEvent(String eventType);
    }

    private static EventBus instance;
    private final List<EventListener> listeners = new ArrayList<>();

    private EventBus() {}

    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    public void publish(String eventType) {
        for (EventListener listener : listeners) {
            listener.onEvent(eventType);
        }
    }
}
