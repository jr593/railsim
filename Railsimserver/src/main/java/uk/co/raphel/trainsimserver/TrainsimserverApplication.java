package uk.co.raphel.trainsimserver;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.co.raphel.trainsimserver.gui.TrainMessageListener;

@SpringBootApplication
@EnableScheduling
@EntityScan("uk.co.raphel.railsim.common.entity")
@EnableTransactionManagement
@EnableAsync
@StyleSheet(Lumo.STYLESHEET) // Use Aura.STYLESHEET to use Aura instead
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("styles.css") // Your custom styles
public class TrainsimserverApplication implements TrainMessageListener {

	public static void main(String[] args) {

		SpringApplication.run(TrainsimserverApplication.class, args);
	}

}
