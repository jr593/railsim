package uk.co.raphel.railsim.webapp.feed;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import uk.co.raphel.railsim.common.dto.ServiceBoardEntry;
import uk.co.raphel.railsim.common.dto.SystemStatus;
import uk.co.raphel.railsim.common.enums.MessageType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;
import java.util.stream.Stream;

/*
  Reads the Server-Sent Event stream from Railsimserver (/api/events) and feeds ServiceBoardStore.
  Reconnects automatically if the server goes away.
 */
@Service
@Slf4j(topic = "RailsimSseClient")
public class RailsimSseClient {

    private final ServiceBoardStore store;
    private final JsonMapper jsonMapper;
    private final URI eventsUri;
    private final Duration reconnectDelay;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private volatile boolean running = true;
    private Thread readerThread;

    public RailsimSseClient(ServiceBoardStore store, JsonMapper jsonMapper,
                            @Value("${railsim.server-url}") String serverUrl,
                            @Value("${railsim.reconnect-seconds:5}") int reconnectSeconds) {
        this.store = store;
        this.jsonMapper = jsonMapper;
        this.eventsUri = URI.create(serverUrl.replaceAll("/+$", "") + "/api/events");
        this.reconnectDelay = Duration.ofSeconds(reconnectSeconds);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        readerThread = Thread.ofVirtual().name("railsim-sse").start(this::readLoop);
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (readerThread != null) {
            readerThread.interrupt();
        }
        httpClient.shutdownNow();
    }

    private void readLoop() {
        while (running) {
            try {
                log.info("Connecting to " + eventsUri);
                HttpRequest request = HttpRequest.newBuilder(eventsUri)
                        .header("Accept", "text/event-stream")
                        .GET()
                        .build();
                HttpResponse<Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
                if (response.statusCode() != 200) {
                    log.warn("Railsimserver returned HTTP " + response.statusCode());
                    response.body().close();
                } else {
                    store.setConnected(true);
                    try (Stream<String> lines = response.body()) {
                        readEvents(lines.iterator());
                    }
                    log.warn("Railsimserver closed the event stream");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                if (running) {
                    log.warn("Event stream failed: " + e);
                }
            }
            if (store.isConnected()) {
                store.setConnected(false);
            }
            try {
                Thread.sleep(reconnectDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // SSE framing: "event:" and "data:" lines, terminated by a blank line.
    private void readEvents(Iterator<String> lines) {
        String eventName = null;
        StringBuilder data = new StringBuilder();
        while (running && lines.hasNext()) {
            String line = lines.next();
            if (line.isEmpty()) {
                if (eventName != null && !data.isEmpty()) {
                    dispatch(eventName, data.toString());
                }
                eventName = null;
                data.setLength(0);
            } else if (line.startsWith("event:")) {
                eventName = fieldValue(line);
            } else if (line.startsWith("data:")) {
                if (!data.isEmpty()) {
                    data.append('\n');
                }
                data.append(fieldValue(line));
            }
            // Comment lines (":") and other fields are ignored.
        }
    }

    private static String fieldValue(String line) {
        String value = line.substring(line.indexOf(':') + 1);
        return value.startsWith(" ") ? value.substring(1) : value;
    }

    private void dispatch(String eventName, String json) {
        try {
            MessageType type = MessageType.valueOf(eventName);
            if (type == MessageType.STATUS) {
                store.applyClock(jsonMapper.readValue(json, SystemStatus.class).simulatorTime());
            } else {
                store.applyService(type, jsonMapper.readValue(json, ServiceBoardEntry.class));
            }
        } catch (Exception e) {
            log.error("Could not process " + eventName + " event (" + json + ")", e);
        }
    }
}
