package uk.co.raphel.railsim;

/**
 * Created by johnr on 30/05/2015.
 */
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.co.raphel.railsim.services.ServiceFactory;
import uk.co.raphel.railsim.services.TrackMonitor;

/**
 * * Created : 30/05/2015
 * * Author  : johnr
 **/
public class Application {
    public static void main(String[] args) throws Exception {

        ApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) context.getBean("taskExecutor");

        // Start the track monitor (builds hazelcast maps)
        TrackMonitor trackMonitor = (TrackMonitor) context.getBean("trackMonitor");
        trackMonitor.setName("Track-Monitor");
        taskExecutor.execute(trackMonitor);

        Thread.sleep(1000);

        // Start the train service factory (should wait for track monitor to be ready)
        ServiceFactory serviceFactory = (ServiceFactory) context.getBean("serviceFactory");
        serviceFactory.setName("Service-Factory");
        serviceFactory.setTaskExecutor(taskExecutor);
        taskExecutor.execute(serviceFactory);


        //check active thread, if zero then shut down the thread pool
        for (;;) {
            int count = taskExecutor.getActiveCount();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (count == 0) {
                taskExecutor.shutdown();
                break;
            }
        }

    }
}
