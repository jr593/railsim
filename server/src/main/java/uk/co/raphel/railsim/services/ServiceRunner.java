package uk.co.raphel.railsim.services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import uk.co.raphel.railsim.common.*;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.railsim.common.enums.SectionStatus;
import uk.co.raphel.railsim.common.enums.ServiceEventType;
import uk.co.raphel.railsim.kafka.KafkaProducer;

import java.util.UUID;

/**
 * * Created : 07/06/2015
 * * Author  : johnr
 **/

public class ServiceRunner extends Thread {


    private DataStore ds;

    private TrainServiceDto trainServiceDto;
    
    KafkaTemplate<String, RailSimMessage> kafkaTemplate;

    @Autowired
    private KafkaProducer kafkaProducer;

    private final Logger log = LoggerFactory.getLogger(ServiceRunner.class);

    public ServiceRunner() {

    }

    public ServiceRunner(TrainServiceDto trainServiceDto, DataStore ds) {
        this.trainServiceDto = trainServiceDto;
        this.ds = ds;
     }

    @Override
    public void run() {
        
        
        int simTime = ds.getSimClock();

        kafkaProducer.publish("railsim.queue",new RailSimMessage(UUID.randomUUID(),
                MessageType.SERVICESTART,simTimeAsClock(simTime), trainServiceDto,
                ds.getSectionName(trainServiceDto.getOccupiedSection()),
                SectionStatus.OCCUPIED,"Service Started"));

        log.info("T=" + simTimeAsClock(simTime) + " Service " + trainServiceDto.getServiceName() + " started at " + ds.getSectionName(trainServiceDto.getOccupiedSection()));
        trainServiceDto.setStarted(true);
        try{
            trainServiceDto.setCurrentServiceEvent(0);
            do {
                // Process this section
                // Calc time in section

                // TODO: replace 0 with current speed
                int timeInSection = ds.calcTimeInSection(trainServiceDto.getServiceEvent().getEventSection(), 0);

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
                if(trainServiceDto.getServiceEvent().getServiceEventType() == ServiceEventType.ARRIVESTOP ||
                   trainServiceDto.getServiceEvent().getServiceEventType() == ServiceEventType.STOPPING) {
                    rdyExitTime += 1;
                    rdyExitTime = Math.max(rdyExitTime, trainServiceDto.getServiceEvent().getScheduledExitTime());
                        // Wait until ready to move on
                    while(simTime <= rdyExitTime) {
                       // log.info("T=" + simTimeAsClock(simTime) + " Service " + trainService.getServiceName() +
                       //         " waiting until (stopped) " + simTimeAsClock(rdyExitTime));
                        kafkaProducer.publish("railsim.queue",
                                new RailSimMessage(UUID.randomUUID(),MessageType.MOVEMENT,simTimeAsClock(simTime), trainServiceDto,
                                ds.getSectionName(trainServiceDto.getOccupiedSection()),
                                SectionStatus.HOLDING,"" + rdyExitTime));
                        Thread.sleep(1000);
                        simTime =  ds.getSimClock();
                    }

                }

                // Was it the last section ?
                if(trainServiceDto.getServiceEvent().getServiceEventType() == ServiceEventType.TERMINATING) {
                    trainServiceDto.setStarted(false);
                    ds.clearTrackSection(trainServiceDto.getOccupiedSection(), trainServiceDto.getId());
                    kafkaProducer.publish("railsim.queue",new RailSimMessage(UUID.randomUUID(),MessageType.MOVEMENT,simTimeAsClock(simTime), trainServiceDto,
                            ds.getSectionName(trainServiceDto.getOccupiedSection()),
                            SectionStatus.CLEARED,"Section Cleared"));
                } else {

                    // Move to next section (if open)
                    int nextSection = trainServiceDto.getNextEvent().getEventSection();
                    if(nextSection != 0) {
                        boolean lock = false;
                        while(!lock) {
                            lock = ds.getTrackLock();
                            Thread.sleep(50);
                        }
                        try {
                            if(ds.isSectionFree(nextSection)) {
                                ds.clearTrackSection(trainServiceDto.getOccupiedSection(), trainServiceDto.getId());
                                // Send clear section message
                                kafkaProducer.publish("railsim.queue", new RailSimMessage(UUID.randomUUID(),MessageType.MOVEMENT,simTimeAsClock(simTime), trainServiceDto,
                                        ds.getSectionName(trainServiceDto.getOccupiedSection()),
                                        SectionStatus.CLEARED,"Section Cleared"));
                                ds.occupySection(trainServiceDto.getNextEvent().getEventSection(), trainServiceDto.getId());
                                // Send occupy section message
                                kafkaProducer.publish("railsim.queue",new RailSimMessage(UUID.randomUUID(),MessageType.MOVEMENT,simTimeAsClock(simTime), trainServiceDto,
                                        ds.getSectionName(trainServiceDto.getNextEvent().getEventSection()),
                                        SectionStatus.OCCUPIED,"Section Occupied"));
                                trainServiceDto.step();
                                log.info("T=" + simTimeAsClock(simTime) + " Service " +
                                        trainServiceDto.getServiceName() + " occupied " + ds.getSectionName(trainServiceDto.getOccupiedSection()));
                            } else {
                                // Send blocking section message
                                kafkaProducer.publish("railsim.queue",new RailSimMessage(UUID.randomUUID(),MessageType.BLOCKING,simTimeAsClock(simTime), trainServiceDto,
                                        ds.getSectionName(trainServiceDto.getOccupiedSection()),
                                        SectionStatus.OCCUPIED,
                                        "Section Held on red for section " + ds.getSectionName(trainServiceDto.getNextEvent().getEventSection())));
                                log.info("T=" + simTimeAsClock(simTime) + " Service " + trainServiceDto.getServiceName() + " waiting on RED at "
                                        + ds.getSectionName(trainServiceDto.getOccupiedSection()) + " for "
                                        + ds.getSectionName(trainServiceDto.getNextEvent().getEventSection()));
                                Thread.sleep(1000);
                            }
                        } catch(NullPointerException npe) {

                            log.error("Could not get next track section " + nextSection + " for service " + trainServiceDto.getServiceName());
                            trainServiceDto.setStarted(false);
                        }
                        ds.releaseTrackLock();
                    }
                }
            } while(trainServiceDto.isStarted());


        } catch(Exception ie) {
            log.error("Error processing " + trainServiceDto,ie);
        }
        ds.clearTrackSection(trainServiceDto.getOccupiedSection(), trainServiceDto.getId());
        // Send clear section message
        kafkaProducer.publish("railsim.queue",new RailSimMessage(UUID.randomUUID(),MessageType.MOVEMENT,simTimeAsClock(simTime), trainServiceDto,
                ds.getSectionName(trainServiceDto.getOccupiedSection()),
                SectionStatus.CLEARED,"Section Cleared"));
        // Send service terminated message
        kafkaProducer.publish("railsim.queue",new RailSimMessage(UUID.randomUUID(),MessageType.COMPLETION,simTimeAsClock(simTime), trainServiceDto
                , "",  SectionStatus.CLEARED,"Service terminated"));
        log.info("T=" + simTimeAsClock(simTime) + " Service " + trainServiceDto.getServiceName() + " terminated at " + ds.getSectionName(trainServiceDto.getOccupiedSection()));
    }

    private String simTimeAsClock(int simtime) {
         int hrs = simtime / 60;
         int mins = simtime % 60;
         return (hrs < 10 ? "0":"")  + hrs + ":" + (mins < 10 ? "0" : "") + mins;
     }


}
