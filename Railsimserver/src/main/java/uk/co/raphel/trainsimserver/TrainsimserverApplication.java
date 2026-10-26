package uk.co.raphel.trainsimserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EntityScan("uk.co.raphel.railsim.common.entity")
@EnableTransactionManagement
@EnableAsync
public class TrainsimserverApplication {

	public static void main(String[] args) {

		SpringApplication.run(TrainsimserverApplication.class, args);
	}

}
