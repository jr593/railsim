package uk.co.raphel.railsim.configapp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.co.raphel.railsim.configapp.repository.DestinationRepository;

import java.awt.*;


/**
 * * Created : 30/05/2015
 * * Author  : johnr
 **/
@SpringBootApplication
@EnableJpaRepositories(basePackages = "uk.co.raphel.railsim.configapp.repository")
@EntityScan("uk.co.raphel.railsim.common.entity")
public class Application {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext ctx =
                new SpringApplicationBuilder(Application.class)
                        .headless(false)
                        .run(args);

        EventQueue.invokeLater(() -> {

            MainFrame ex = ctx.getBean(MainFrame.class);
            ex.init();
            ex.setVisible(true);
        });
    }

}
