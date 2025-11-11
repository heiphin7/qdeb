package com.qdeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.qdeb")
public class QdebApplication {

	public static void main(String[] args) {
		SpringApplication.run(QdebApplication.class, args);
	}

}
