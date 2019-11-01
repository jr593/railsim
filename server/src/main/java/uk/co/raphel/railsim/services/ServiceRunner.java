package uk.co.raphel.railsim.services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import uk.co.raphel.railsim.common.*;

/**
 * * Created : 07/06/2015
 * * Author  : johnr
 **/

public class ServiceRunner extends Thread {


    private DataStore ds;

    private TrainService trainService;

    private RabbitTemplate rabbitTemplate;

    private final Logger log = LoggerFactory.getLogger(ServiceRunner.class);


    public ServiceRunner() { }

    public ServiceRunner(TrainService trainService,DataStore ds, RabbitTemplate rabbitTemplate) {
        this.trainService = trainService;
        this.ds = ds;
        this.rabbitTemplate = rabbitTemplate ;
    }

    @Override
    public void run() {
        int simTime = ds.getSimClock();

        rabbitTemplate.convertAndSend(new RailSimMessage(MessageType.SERVICESTART,simTimeAsClock(simTime),trainService,
                ds.getSectionName(trainService.getOccupiedSection()),
                SectionStatus.OCCUPIED,"Service Started").toJson());

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
                if(trainService.getServiceEvent().getServiceEventType() == ServiceEventType.ARRIVESTOP ||
                   trainService.getServiceEvent().getServiceEventType() == ServiceEventType.STOPPING) {
                    rdyExitTime += 1;
                    rdyExitTime = Math.max(rdyExitTime, trainService.getServiceEvent().getScheduledExitTime());
                        // Wait until ready to move on
                    while(simTime <= rdyExitTime) {
                       // log.info("T=" + simTimeAsClock(simTime) + " Service " + trainService.getServiceName() +
                       //         " waiting until (stopped) " + simTimeAsClock(rdyExitTime));
                        rabbitTemplate.convertAndSend(new RailSimMessage(MessageType.MOVEMENT,simTimeAsClock(simTime), trainService,
                                ds.getSectionName(trainService.getOccupiedSection()),
                                SectionStatus.HOLDING,"" + rdyExitTime).toJson());
                        Thread.sleep(1000);
                        simTime =  ds.getSimClock();
                    }

                }

                // Was it the last section ?
                if(trainService.getServiceEvent().getServiceEventType() == ServiceEventType.TERMINATING) {
                    trainService.setStarted(false);
                    ds.clearTrackSection(trainService.getOccupiedSection(), trainService.getId());
                    rabbitTemplate.convertAndSend(new RailSimMessage(MessageType.MOVEMENT,simTimeAsClock(simTime), trainService,
                            ds.getSectionName(trainService.getOccupiedSection()),
                            SectionStatus.CLEARED,"Section Cleared").toJson());
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
                                // Send clear section message
                                rabbitTemplate.convertAndSend(new RailSimMessage(MessageType.MOVEMENT,simTimeAsClock(simTime), trainService,
                                        ds.getSectionName(trainService.getOccupiedSection()),
                                        SectionStatus.CLEARED,"Section Cleared").toJson());
                                ds.occupySection(trainService.getNextEvent().getEventSection(), trainService.getId());
                                // Send occupy section message
                                rabbitTemplate.convertAndSend(new RailSimMessage(MessageType.MOVEMENT,simTimeAsClock(simTime), trainService,
                                        ds.getSectionName(trainService.getNextEvent().getEventSection()),
                                        SectionStatus.OCCUPIED,"Section Occupied").toJson());
                                trainService.step();
                                log.info("T=" + simTimeAsClock(simTime) + " Service " + trainService.getServiceName() + " occupied " + ds.getSectionName(trainService.getOccupiedSection()));
                            } else {
                                // Send blocking section message
                                rabbitTemplate.convertAndSend(new RailSimMessage(MessageType.BLOCKING,simTimeAsClock(simTime), trainService,
                                        ds.getSectionName(trainService.getOccupiedSection()),
                                        SectionStatus.OCCUPIED,"Section Held on red for section " + ds.getSectionName(trainService.getNextEvent().getEventSection())).toJson());
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
            log.error("Error processing " + trainService,ie);
        }
        ds.clearTrackSection(trainService.getOccupiedSection(), trainService.getId());
        // Send clear section message
        rabbitTemplate.convertAndSend(new RailSimMessage(MessageType.MOVEMENT,simTimeAsClock(simTime), trainService,
                ds.getSectionName(trainService.getOccupiedSection()),
                SectionStatus.CLEARED,"Section Cleared").toJson());
        // Send service terminated message
        rabbitTemplate.convertAndSend(new RailSimMessage(MessageType.COMPLETION,simTimeAsClock(simTime), trainService
                , "",  SectionStatus.CLEARED,"Service terminated").toJson());
        log.info("T=" + simTimeAsClock(simTime) + " Service " + trainService.getServiceName() + " terminated at " + ds.getSectionName(trainService.getOccupiedSection()));
    }

     private String simTimeAsClock(int simtime) {
         int hrs = simtime / 60;
         int mins = simtime % 60;
         return (hrs < 10 ? "0":"")  + hrs + ":" + (mins < 10 ? "0" : "") + mins;
     }


}
