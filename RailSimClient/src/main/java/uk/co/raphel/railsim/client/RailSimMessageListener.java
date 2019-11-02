package uk.co.raphel.railsim.client;

import uk.co.raphel.railsim.common.RailSimMessage;

/**
 * * Created : 07/05/2017
 * * Author  : johnr
 **/
public interface RailSimMessageListener {

    void consume(RailSimMessage railSimMessage);

    void recvSchedule(RailSimMessage railSimMessage);
}
