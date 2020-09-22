package uk.co.raphel.railsim.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;
import java.awt.*;


/**
 * * Created : 30/05/2015
 * * Author  : johnr
 **/
@SpringBootApplication
public class Application {

    private static MainFrame frame;

    @Bean
    public MainFrame mainFrame() {
        return frame;
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> frame = new MainFrame());

        SpringApplication app = new SpringApplication(Application.class);
        app.run(args);


    }
}
