package qcs;
import qcs.EventBus;
import java.util.ArrayList;
import java.util.List;

/**
 * EventBus is a simple event handling system that enables communication between different components.
 * It allows components to subscribe for events and publish events to all registered listeners.
 * This facilitates decoupling between classes and supports dynamic updates.
 */
public class EventBus {
    /**
     * Interface for components that want to listen for events.
     * Classes implementing this interface must handle the onEvent method.
     */
    public interface EventListener {
        /**
         * Called when an event is published.
         *
         * @param eventType the type of the event being triggered.
         */
        void onEvent(String eventType);
    }

    private static EventBus instance;
    private final List<EventListener> listeners = new ArrayList<>();

    /**
     * Private constructor to enforce singleton pattern.
     * Use getInstance() to get the singleton instance.
     */
    private EventBus() {}

    /**
     * Retrieves the singleton instance of the EventBus.
     *
     * @return the singleton EventBus instance.
     */
    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    /**
     * Subscribes a listener to receive events.
     *
     * @param listener the listener that wants to receive events.
     */
    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    /**
     * Publishes an event to all registered listeners.
     *
     * @param eventType the type of the event being triggered.
     */
    public void publish(String eventType) {
        for (EventListener listener : listeners) {
            listener.onEvent(eventType);
        }
    }
}
