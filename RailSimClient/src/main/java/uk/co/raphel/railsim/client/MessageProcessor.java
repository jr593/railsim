package uk.co.raphel.railsim.client;/**
 * Created by johnr on 05/05/2017.
 */


import uk.co.raphel.railsim.common.RailSimMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * * Created : 05/05/2017
 * * Author  : johnr
 **/
public class MessageProcessor implements RailListener {

    private List<TrackEventListener> listeners = new ArrayList<>();

    @Override
    public void listen(String message) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        RailSimMessage railSimMessage = RailSimMessage.fromjson(message);

        if(railSimMessage != null) {
            System.out.println(railSimMessage);
        }

    }


    public void addListener(TrackEventListener evtListener) {
            listeners.add(evtListener);
    }
}
