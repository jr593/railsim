package uk.co.raphel.trainsimserver.service;

import com.vaadin.flow.component.UI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.dto.ServiceBoardEntry;
import uk.co.raphel.railsim.common.dto.SystemStatus;
import uk.co.raphel.railsim.common.enums.MessageType;

import java.io.IOException;
import java.util.Optional;

/*
  Streams train service updates to external clients (e.g. RailsimWebApp) as Server-Sent Events.
  Event name is the MessageType; data is single-line JSON (ServiceBoardEntry or SystemStatus).
 */
@RestController
@Slf4j(topic = "SseController")
public class SseController {

    private final MessageBroadcaster messageBroadcaster;
    private final TrainManagerService trainManagerService;
    // Single-line JSON: application.yaml turns on INDENT_OUTPUT globally.
    private final ObjectWriter jsonWriter;

    public SseController(MessageBroadcaster messageBroadcaster, TrainManagerService trainManagerService,
                         JsonMapper jsonMapper) {
        this.messageBroadcaster = messageBroadcaster;
        this.trainManagerService = trainManagerService;
        this.jsonWriter = jsonMapper.writer().without(SerializationFeature.INDENT_OUTPUT);
    }

    @GetMapping(path = "/api/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events() {
        SseEmitter emitter = new SseEmitter(0L);
        SseListener listener = new SseListener(emitter);

        emitter.onCompletion(() -> messageBroadcaster.unregister(listener));
        emitter.onTimeout(() -> messageBroadcaster.unregister(listener));
        emitter.onError(e -> messageBroadcaster.unregister(listener));

        // Bring a newly connected client up to date with services already running.
        for (RunningService service : trainManagerService.getRunningServices()) {
            listener.send(MessageType.SERVICESTART, ServiceBoardEntry.from(service));
        }
        messageBroadcaster.register(listener);
        log.info("SSE client connected");
        return emitter;
    }

    private class SseListener implements BroadcastListener {

        private final SseEmitter emitter;

        SseListener(SseEmitter emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onMessage(RailSimMessage<?> message) {
            switch (message.getMessageType()) {
                case SERVICESTART, MOVEMENT, BLOCKING, COMPLETION -> {
                    if (message.getMessageBody() instanceof RunningService service) {
                        send(message.getMessageType(), ServiceBoardEntry.from(service));
                    }
                }
                case STATUS -> {
                    if (message.getMessageBody() instanceof SystemStatus status) {
                        send(MessageType.STATUS, status);
                    }
                }
                default -> {
                    // Not needed by external clients
                }
            }
        }

        void send(MessageType type, Object body) {
            try {
                emitter.send(SseEmitter.event()
                        .name(type.name())
                        .data(jsonWriter.writeValueAsString(body), MediaType.TEXT_PLAIN));
            } catch (IOException | IllegalStateException e) {
                log.info("SSE client disconnected");
                messageBroadcaster.unregister(this);
                emitter.completeWithError(e);
            }
        }

        @Override
        public Optional<UI> retrieveUI() {
            return Optional.empty();
        }
    }
}
