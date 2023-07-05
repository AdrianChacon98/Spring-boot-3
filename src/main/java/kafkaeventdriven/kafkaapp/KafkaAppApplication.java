package kafkaeventdriven.kafkaapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
public class KafkaAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaAppApplication.class, args);
	}

}
