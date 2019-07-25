package com.alag.mmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MmallApplication {

	public static void main(String[] args) {
		SpringApplication.run(MmallApplication.class, args);
	}

}
