package uk.co.raphel.railsim.client;/**
 * Created by johnr on 19/02/2017.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.common.RailSimMessage;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * * Created : 19/02/2017
 * * Author  : johnr
 **/
@Component
public class RailListener {

    @Autowired
    MainFrame mainFrame;

    public void listen(String message) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mainFrame.setTitle(df.format(new Date()));

        RailSimMessage railSimMessage = RailSimMessage.fromjson(message);

        System.out.println(railSimMessage.getMiscInfo());
    }

}
