package com.example.Product.Ranking.System;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ProductRankingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductRankingSystemApplication.class, args);
		System.out.println("Product Ranking System Application has been started");

	}

}
