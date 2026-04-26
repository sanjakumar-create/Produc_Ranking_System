package com.example.Product.Ranking.System;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProductRankingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductRankingSystemApplication.class, args);
	}

	// This runs automatically exactly once when the application starts
	@Bean
	public CommandLineRunner printBeans(ApplicationContext context) {
		return args -> {
			System.out.println("===== ALL LOADED SPRING BEANS =====");
			String[] beanNames = context.getBeanDefinitionNames();
			for (String beanName : beanNames) {
				// To avoid spamming the console, we only print our own beans
				if(beanName.contains("Product") || beanName.contains("Service")) {
					System.out.println("Loaded: " + beanName);
				}
			}
			System.out.println("===================================");
		};
	}
}