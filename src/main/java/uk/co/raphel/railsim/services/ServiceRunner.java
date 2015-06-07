package uk.co.raphel.railsim.services;/**
 * Created by johnr on 07/06/2015.
 */

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.dto.TrainService;

/**
 * * Created : 07/06/2015
 * * Author  : johnr
 **/
@Component
@Scope("prototype")
public class ServiceRunner implements Runnable {

    private TrainService trainService;

    public ServiceRunner(TrainService trainService) {
        this.trainService = trainService;
    }

    @Override
    public void run() {
        System.out.println("Service " + trainService.getServiceName() + " is running.");
        trainService.setStarted(true);
        try{
            Thread.sleep(5000);
        } catch(InterruptedException ie) {
            // Do nothing
        }
        System.out.println("Service " + trainService.getServiceName() + " is complete.");
    }
}
