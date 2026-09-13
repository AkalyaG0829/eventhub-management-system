package com.eventhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EventhubBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventhubBackendApplication.class, args);
	}

}
