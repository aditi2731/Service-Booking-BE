package com.example.ServiceBooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ServiceBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceBookingApplication.class, args);
	}

}
