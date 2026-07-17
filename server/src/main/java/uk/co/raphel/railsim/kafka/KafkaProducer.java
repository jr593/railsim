package uk.co.raphel.railsim.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.common.RailSimMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {
    public static final String TOPIC = "railsim.queue";

    private final KafkaTemplate<String, RailSimMessage> kafkaTemplate;

    public void publish(String topic, RailSimMessage message) {
        String key =  message.getMsgKey().toString();
        kafkaTemplate.send(topic, key, message);
        log.info("Sent message: {}", message);
    }
}