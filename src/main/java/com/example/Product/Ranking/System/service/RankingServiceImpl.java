package com.example.Product.Ranking.System.service;

import com.example.Product.Ranking.System.entity.ProductMetrics;
import com.example.Product.Ranking.System.repository.ProductMetricsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RankingServiceImpl implements RankingService {

    private final ProductMetricsRepository metricsRepository;

    // ⭐ FIXED: The constructor name now exactly matches the class name
    public RankingServiceImpl(ProductMetricsRepository metricsRepository){
        this.metricsRepository = metricsRepository;
    }

    @Override
    public double calculateScore(double ratingAverage,
                                 int salesCount,
                                 int reviewCount,
                                 int viewCount,
                                 int discountPercentage){

        return 0.4 * ratingAverage
                + 0.25 * Math.log(salesCount + 1)
                + 0.2 * reviewCount
                + 0.1 * viewCount
                + 0.05 * discountPercentage;
    }

    @Override
    public double calculateTrendingScore(int recentSales,
                                         int recentViews,
                                         int recentReviews,
                                         double ratingAverage){

        return 0.4 * recentViews
                + 0.3 * recentSales
                + 0.2 * recentReviews
                + 0.1 * ratingAverage;
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 3600000)
    public void updateRankingScores() {
        // 1. Fetches everything (attached to the transaction)
        List<ProductMetrics> metricsList = metricsRepository.findAll();

        for(ProductMetrics m : metricsList){
            double score = calculateScore(
                    m.getRatingAverage(),
                    m.getSalesCount(),
                    m.getReviewCount(),
                    m.getViewCount(),
                    0
            );
            double trendingScore = calculateTrendingScore(
                    m.getSalesCount(),
                    m.getViewCount(),
                    m.getReviewCount(),
                    m.getRatingAverage()
            );

            // 2. Modifying the object directly
            m.setRankingScore(score);
            m.setTrendingScore(trendingScore);
        }

        // Save all at once - much faster!
        metricsRepository.saveAll(metricsList);
    }
}