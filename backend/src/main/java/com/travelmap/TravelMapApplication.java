package com.travelmap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TravelMapApplication implements CommandLineRunner {

	@Value("${youtube.api.application-name:NOT FOUND}")
	private String applicationName;

	public static void main(String[] args) {
		SpringApplication.run(TravelMapApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// System.out.println("youtube.api.application-name = " + applicationName);
	}
}
