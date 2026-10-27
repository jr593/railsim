package uk.co.raphel.trainsimserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.raphel.broadcast.api.BroadcastBus;
import uk.co.raphel.railsim.common.nats.RailSimMessage;

/*
  Routes simulator messages to registered listeners.
  Client-facing delivery happens through the SSE emitters owned by SseController;
  internal components (TimerService, RailsimRunnerTask) also listen for STATUS updates.
 */

@Service
@Slf4j(topic = "MessageBroadcaster")
@RequiredArgsConstructor
public class MessageBroadcaster {

    private final BroadcastBus broadcastBus;

    /**
     * Send an update to every registered listener.
     */
    public void broadcast(RailSimMessage<?>  msg) {

        if (msg.getMessageBody() == null) {
            log.warn("Message broadcast with no body: " + msg.getClass().getName());
        }
        log.info("broardcast : " + msg.toString());
        broadcastBus.publish(msg);
    }


}
