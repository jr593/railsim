package uk.co.raphel.railsim.client;/**
 * Created by johnr on 19/02/2017.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.common.RailSimMessage;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * * Created : 19/02/2017
 * * Author  : johnr
 **/

public interface RailListener {


    public void listen(String message);

    public void listen(RailSimMessage message);

}
