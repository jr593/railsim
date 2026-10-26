package uk.co.raphel.trainsimserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.dto.RailSimMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/*
  Routes simulator messages to registered listeners.
  Client-facing delivery happens through the SSE emitters owned by SseController;
  internal components (TimerService, RailsimRunnerTask) also listen for STATUS updates.
 */

@Service
@Slf4j(topic = "MessageBroadcaster")
public class MessageBroadcaster {

    private final List<BroadcastListener> listeners =
            new CopyOnWriteArrayList<>();

    /**
     * Register a listener for simulator updates (e.g. an SSE client).
     */
    public void register(BroadcastListener listener) {

        if (listeners.contains(listener)) {
            return;
        }

        listeners.add(listener);
    }

    /**
     * Send an update to every registered listener.
     */
    public void broadcast(RailSimMessage<?> msg) {

        if (msg.getMessageBody() == null) {
            log.warn("Message broadcast with no body: " + msg.getMessageType());
        }
        for (BroadcastListener listener : listeners) {
            listener.onMessage(msg);
        }
    }

    public void unregister(BroadcastListener listener) {
        listeners.remove(listener);
    }
}
