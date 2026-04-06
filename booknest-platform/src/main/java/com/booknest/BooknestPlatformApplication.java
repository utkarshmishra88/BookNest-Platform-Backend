package com.booknest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
public class BooknestPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(BooknestPlatformApplication.class, args);
	}

}
