package uk.co.raphel.railsim.services;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import uk.co.raphel.railsim.common.*;

/**
 * * Created : 07/06/2015
 * * Author  : johnr
 **/

public class ServiceRunner extends Thread {


    private DataStore ds;

    private TrainService trainService;
    
    KafkaTemplate<String, RailSimMessage> kafkaTemplate;

    @Autowired
    NewTopic topic;

    private final Logger log = LoggerFactory.getLogger(ServiceRunner.class);


    public ServiceRunner() { }

    public ServiceRunner(TrainService trainService,DataStore ds, KafkaTemplate kafkaTemplate) {
        this.trainService = trainService;
        this.ds = ds;
        this.kafkaTemplate = kafkaTemplate ;
    }

    @Override
    public void run() {
        int simTime = ds.getSimClock();

        sendMessage(topic.name(),new RailSimMessage(MessageType.SERVICESTART,simTimeAsClock(simTime),trainService,
                ds.getSectionName(trainService.getOccupiedSection()),
                SectionStatus.OCCUPIED,"Service Started"));

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
                        sendMessage(topic.name(),new RailSimMessage(MessageType.MOVEMENT,simTimeAsClock(simTime), trainService,
                                ds.getSectionName(trainService.getOccupiedSection()),
                                SectionStatus.HOLDING,"" + rdyExitTime));
                        Thread.sleep(1000);
                        simTime =  ds.getSimClock();
                    }

                }

                // Was it the last section ?
                if(trainService.getServiceEvent().getServiceEventType() == ServiceEventType.TERMINATING) {
                    trainService.setStarted(false);
                    ds.clearTrackSection(trainService.getOccupiedSection(), trainService.getId());
                    sendMessage(topic.name(),new RailSimMessage(MessageType.MOVEMENT,simTimeAsClock(simTime), trainService,
                            ds.getSectionName(trainService.getOccupiedSection()),
                            SectionStatus.CLEARED,"Section Cleared"));
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
                                sendMessage(topic.name(), new RailSimMessage(MessageType.MOVEMENT,simTimeAsClock(simTime), trainService,
                                        ds.getSectionName(trainService.getOccupiedSection()),
                                        SectionStatus.CLEARED,"Section Cleared"));
                                ds.occupySection(trainService.getNextEvent().getEventSection(), trainService.getId());
                                // Send occupy section message
                                sendMessage(topic.name(),new RailSimMessage(MessageType.MOVEMENT,simTimeAsClock(simTime), trainService,
                                        ds.getSectionName(trainService.getNextEvent().getEventSection()),
                                        SectionStatus.OCCUPIED,"Section Occupied"));
                                trainService.step();
                                log.info("T=" + simTimeAsClock(simTime) + " Service " + trainService.getServiceName() + " occupied " + ds.getSectionName(trainService.getOccupiedSection()));
                            } else {
                                // Send blocking section message
                                sendMessage(topic.name(),new RailSimMessage(MessageType.BLOCKING,simTimeAsClock(simTime), trainService,
                                        ds.getSectionName(trainService.getOccupiedSection()),
                                        SectionStatus.OCCUPIED,"Section Held on red for section " + ds.getSectionName(trainService.getNextEvent().getEventSection())));
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
        sendMessage(topic.name(),new RailSimMessage(MessageType.MOVEMENT,simTimeAsClock(simTime), trainService,
                ds.getSectionName(trainService.getOccupiedSection()),
                SectionStatus.CLEARED,"Section Cleared"));
        // Send service terminated message
        sendMessage(topic.name(),new RailSimMessage(MessageType.COMPLETION,simTimeAsClock(simTime), trainService
                , "",  SectionStatus.CLEARED,"Service terminated"));
        log.info("T=" + simTimeAsClock(simTime) + " Service " + trainService.getServiceName() + " terminated at " + ds.getSectionName(trainService.getOccupiedSection()));
    }

    private void sendMessage(String topic, RailSimMessage message) {
        ListenableFuture<SendResult<String, RailSimMessage>> future =
                kafkaTemplate.send(topic, message);

        future.addCallback(new ListenableFutureCallback<SendResult<String, RailSimMessage>>() {

            @Override
            public void onSuccess(SendResult<String, RailSimMessage> result) {
                System.out.println("Sent message=[" + message +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }
            @Override
            public void onFailure(Throwable ex) {
                System.out.println("Unable to send message=["
                        + message + "] due to : " + ex.getMessage());
            }
        });
    }

    private String simTimeAsClock(int simtime) {
         int hrs = simtime / 60;
         int mins = simtime % 60;
         return (hrs < 10 ? "0":"")  + hrs + ":" + (mins < 10 ? "0" : "") + mins;
     }


}
