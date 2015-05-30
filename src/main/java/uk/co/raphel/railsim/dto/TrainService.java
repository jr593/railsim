package uk.co.raphel.railsim.dto;/**
 * Created by johnr on 30/05/2015.
 */

/**
 * * Created : 30/05/2015
 * * Author  : johnr
 **/
public class TrainService {

    private Integer startTime;
    private String serviceName;

    // TODO: Lots more fields


    public TrainService(String csvLine) {

    }


    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
