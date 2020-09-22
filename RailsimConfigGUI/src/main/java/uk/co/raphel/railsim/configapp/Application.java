package uk.co.raphel.railsim.configapp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.awt.*;


/**
 * * Created : 30/05/2015
 * * Author  : johnr
 **/
@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Exception {

        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(MainFrame.class)
                .headless(false).run(args);

        EventQueue.invokeLater(() -> {

            MainFrame ex = ctx.getBean(MainFrame.class);
            ex.init();
            ex.setVisible(true);
        });
    }
}
