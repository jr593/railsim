package uk.co.raphel.railsim.services;/**
 * Created by johnr on 07/06/2015.
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import uk.co.raphel.railsim.dto.TerminatingEvent;

import uk.co.raphel.railsim.dto.TrainService;

/**
 * * Created : 07/06/2015
 * * Author  : johnr
 **/
@Component
@Scope("prototype")
public class ServiceRunner extends Thread {

    //@Autowired
    private DataStore ds;

    private TrainService trainService;


    private Logger log = LoggerFactory.getLogger(ServiceRunner.class);


    public ServiceRunner(TrainService trainService,DataStore ds) {
        this.trainService = trainService;
        this.ds = ds;

    }

    @Override
    public void run() {
        int simTime = ds.getSimClock();
        log.info("T=" + simTimeAsClock(simTime) + " Service " + trainService.getServiceName() + " started at " + ds.getSectionName(trainService.getOccupiedSection()));
        trainService.setStarted(true);
        try{



            trainService.setCurrentServiceEvent(0);

            do {
                // Process this section
                // Calc time in section

                // TODO: replace 0 with current speed
                int timeInSection = ds.calcTimeInSection(trainService.getServiceEvent().getEventSection(), 0);

                // Calculate Ready To Exit Time
                int rdyExitTime = simTime + timeInSection;

                // Wait until ready to move on
                while(simTime <= rdyExitTime) {
                    Thread.sleep(1000);
                   // log.info("T=" + simTimeAsClock(simTime) + " Service " + trainService.getServiceName() +
                   //      " waiting until (travelling) " + simTimeAsClock(rdyExitTime));
                    simTime =  ds.getSimClock();
                }
                // Is this a stopping section ?
                if(trainService.getServiceEvent().isStopping()) {
                    rdyExitTime += 1;
                    rdyExitTime = Math.max(rdyExitTime, trainService.getServiceEvent().getScheduledExitTime());
                        // Wait until ready to move on
                    while(simTime <= rdyExitTime) {
                       // log.info("T=" + simTimeAsClock(simTime) + " Service " + trainService.getServiceName() +
                       //         " waiting until (stopped) " + simTimeAsClock(rdyExitTime));
                        Thread.sleep(1000);
                        simTime =  ds.getSimClock();
                    }

                }

                // Was it the last section ?
                if(trainService.getServiceEvent() instanceof TerminatingEvent) {
                    trainService.setStarted(false);
                    ds.clearTrackSection(trainService.getOccupiedSection(), trainService.getId());
                } else {

                    // Move to next section (if open)
                    int nextSection = trainService.getNextEvent().getEventSection();
                    if(nextSection != 0) {
                        boolean lock = false;
                        while(!lock) {
                            lock = ds.getTrackLock();
                            Thread.sleep(50);
                        }
                        try {
                            if(ds.isSectionFree(nextSection)) {
                                ds.clearTrackSection(trainService.getOccupiedSection(),trainService.getId());
                                ds.occupySection(trainService.getNextEvent().getEventSection(), trainService.getId());
                                trainService.step();
                                log.info("T=" + simTimeAsClock(simTime) + " Service " + trainService.getServiceName() + " occupied " + ds.getSectionName(trainService.getOccupiedSection()));
                            } else {
                                log.info("T=" + simTimeAsClock(simTime) + " Service " + trainService.getServiceName() + " waiting on RED at "
                                        + ds.getSectionName(trainService.getOccupiedSection()) + " for "
                                        + ds.getSectionName(trainService.getNextEvent().getEventSection()));
                                Thread.sleep(1000);
                            }
                        } catch(NullPointerException npe) {
                            log.error("Could not get next track section " + nextSection + " for service " + trainService.getServiceName());
                            trainService.setStarted(false);
                        }
                        ds.releaseTrackLock();
                    }
                }
            } while(trainService.isStarted());


        } catch(Exception ie) {
            ie.printStackTrace();
        }
        ds.clearTrackSection(trainService.getOccupiedSection(), trainService.getId());
        log.info("T=" + simTimeAsClock(simTime) + " Service " + trainService.getServiceName() + " terminated at " + ds.getSectionName(trainService.getOccupiedSection()));
    }

     private String simTimeAsClock(int simtime) {
         int hrs = simtime / 60;
         int mins = simtime % 60;
         return "" + hrs + ":" + mins;
     }


}
