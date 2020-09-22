package uk.co.raphel.railsim.client;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.common.MessageType;
import uk.co.raphel.railsim.common.RailSimMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * * Created : 07/05/2017
 * * Author  : johnr
 **/
public class MessageProcessor  {



    private List<RailSimMessageListener> listeners = new ArrayList<>();

    public void listen(String message) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        RailSimMessage railSimMessage = RailSimMessage.fromjson(message);

        System.out.println(railSimMessage);
        if (railSimMessage != null) {
            if (railSimMessage.getMessageType() == MessageType.SCHEDULE) {
                listeners.forEach(m -> m.recvSchedule(railSimMessage));
            } else {
                listeners.forEach(m -> m.consume(railSimMessage));
            }
        }
    }

    public void listen(RailSimMessage railSimMessage) {
        if (railSimMessage != null) {
            if (railSimMessage.getMessageType() == MessageType.SCHEDULE) {
                listeners.forEach(m -> m.recvSchedule(railSimMessage));
            } else {
                listeners.forEach(m -> m.consume(railSimMessage));
            }
        }
    }

    void addListener(RailSimMessageListener listener) {
        listeners.add(listener);
    }
}
