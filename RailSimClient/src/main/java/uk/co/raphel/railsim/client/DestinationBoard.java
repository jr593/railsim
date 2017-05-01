package uk.co.raphel.railsim.client;/**
 * Created by johnr on 01/05/2017.
 */

import uk.co.raphel.railsim.common.RailSimMessage;

import javax.swing.*;
import java.text.SimpleDateFormat;

/**
 * * Created : 01/05/2017
 * * Author  : johnr
 **/
public class DestinationBoard extends JPanel implements  RailListener {


    @Override
    public void listen(String message) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        RailSimMessage railSimMessage = RailSimMessage.fromjson(message);

        if(railSimMessage != null) {
            System.out.println(railSimMessage);
        }

    }


}
