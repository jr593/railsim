package uk.co.raphel.railsim.client;

import uk.co.raphel.railsim.common.RailSimMessage;

/**
 * * Created : 19/02/2017
 * * Author  : johnr
 **/

public interface RailListener {


    void listen(String message);

    void listen(RailSimMessage message);

}
