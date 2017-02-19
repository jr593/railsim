package uk.co.raphel.railsim;

/**
 * Created by johnr on 30/05/2015.
 */
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.co.raphel.railsim.services.ServiceFactory;


/**
 * * Created : 30/05/2015
 * * Author  : johnr
 **/
public class Application {
    public static void main(String[] args) throws Exception {

        ApplicationContext context = new ClassPathXmlApplicationContext("context.xml");

    }
}
