package uk.co.raphel.railsim.client;

import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.common.MessageType;
import uk.co.raphel.railsim.common.RailSimMessage;

import java.util.ArrayList;
import java.util.List;

@Component
@EnableKafka
public class KafkaConsumer {


    private List<DestinationBoard> listeners = new ArrayList<>();

    public void addListener(DestinationBoard brd) {
        this.listeners.add(brd);

    }
    @KafkaListener(topics = "railsim.queue")
    public void listenGroupFoo(RailSimMessage message) {

        System.out.println(message);
        if (message != null) {
            if (message.getMessageType() == MessageType.SCHEDULE) {
                listeners.forEach(m -> m.recvSchedule(message));
            } else {
                listeners.forEach(m -> m.consume(message));
            }
        }
    }

}
