package com.example.Product.Ranking.System.repository;

import java.util.List;

import com.example.Product.Ranking.System.entity.Product;
import com.example.Product.Ranking.System.entity.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMetricsRepository
        extends JpaRepository<ProductMetrics, Long> {
    List<ProductMetrics> findTop10ByOrderByRankingScoreDesc();


    List<ProductMetrics> findTop10ByOrderByTrendingScoreDesc();
}
