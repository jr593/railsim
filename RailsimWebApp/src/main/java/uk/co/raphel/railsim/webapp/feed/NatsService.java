package uk.co.raphel.railsim.webapp.feed;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.Nats;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j(topic="NatsService")
public class NatsService {

    @Value("${railsim.nats.url}")
    private String natsUrl;

    private final ServiceBoardStore serviceBoardStore;

    public NatsService(ServiceBoardStore serviceBoardStore) {
        this.serviceBoardStore = serviceBoardStore;
    }


    @PostConstruct()
    public void init() {
        try (Connection connection = Nats.connect(natsUrl)) {

            System.out.println("Connected to NATS: " + natsUrl);

            // Create a dispatcher that processes messages asynchronously
            Dispatcher dispatcher = connection.createDispatcher(
                    this::onMessage
            );

            // Subscribe to every subject
            dispatcher.subscribe(">");

            System.out.println("Listening for messages on all subjects...");

            // Ensure the subscription is sent before continuing
            connection.flush(Duration.ofSeconds(5));

        } catch (InterruptedException | IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
    private  void onMessage(Message message) {

        String subject = message.getSubject();

        String body = new String(
                message.getData(),
                StandardCharsets.UTF_8
        );

        System.out.printf(
                "Received message on '%s': %s%n",
                subject,
                body
        );

        // Trigger your application logic
        // serviceBoardStore.applyClock() ;
        // OR
        // serviceBoardStore.applyService();
        handleMessage(subject, body);
    }

    private  void handleMessage(String subject, String body) {

        System.out.printf(
                "Processing message: subject=%s, body=%s%n",
                subject,
                body
        );

        // TODO: Add your business logic here.
        //
        // Examples:
        // - Call another service
        // - Process a broadcast event
        // - Update a database
        // - Trigger a workflow
    }



}
