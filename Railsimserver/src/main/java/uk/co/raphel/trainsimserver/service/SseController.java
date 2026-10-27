package uk.co.raphel.trainsimserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import static java.util.Arrays.stream;

/*
  Streams train service updates to external clients (e.g. RailsimWebApp) as Server-Sent Events.
  Event name is the MessageType; data is single-line JSON (ServiceBoardEntry or SystemStatus).
 */
@RestController
@Slf4j(topic = "SseController")
@Deprecated public class SseController {

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
}
