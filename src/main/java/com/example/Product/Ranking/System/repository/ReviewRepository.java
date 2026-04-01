package com.example.Product.Ranking.System.repository;


import com.example.Product.Ranking.System.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

}