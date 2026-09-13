package uk.co.raphel.trainsimserver.railsimRunner;


import uk.co.raphel.railsim.common.entity.TrainService;

public class RailsimRunnerTask implements Runnable{

    private final TrainService trainService;

    public RailsimRunnerTask(TrainService trainService) {
        this.trainService = trainService;
    }

    public void run() {
        System.out.println("We run : " + trainService.toString());
    }
}
