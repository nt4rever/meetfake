package com.example.meetfake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableWebSocket
public class MeetFakeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetFakeApplication.class, args);
	}

}
