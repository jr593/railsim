package uk.co.raphel.railsim.client.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.common.RailSimMessage;

@Slf4j(topic = "KafkaProducer")
@Component
@RequiredArgsConstructor
public class KafkaProducer {


    private final KafkaTemplate<String, RailSimMessage> kafkaTemplate;

    public void publish(String topic, RailSimMessage message) {
        String key =  message.getMsgKey().toString();
        kafkaTemplate.send(topic, key, message);
        log.info("Sent message: {}", message);
    }
}