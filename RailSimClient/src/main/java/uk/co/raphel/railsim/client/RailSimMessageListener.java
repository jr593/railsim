package uk.co.raphel.railsim.client;/**
 * Created by johnr on 07/05/2017.
 */

import uk.co.raphel.railsim.common.RailSimMessage;

/**
 * * Created : 07/05/2017
 * * Author  : johnr
 **/
public interface RailSimMessageListener {

    public void consume(RailSimMessage railSimMessage);

    public void recvSchedule(RailSimMessage railSimMessage);
}
