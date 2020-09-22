package uk.co.raphel.railsim;

/**
 * Created by johnr on 30/05/2015.
 */


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * * Created : 30/05/2015
 * * Author  : johnr
 **/
@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Exception {
        SpringApplicationBuilder application = new SpringApplicationBuilder(Application.class);

        application.run(args);

   }
}
