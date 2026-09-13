package uk.co.raphel.trainsimserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan("uk.co.raphel.railsim.common.entity")
public class TrainsimserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainsimserverApplication.class, args);
	}

}
