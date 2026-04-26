package com.example.Product.Ranking.System.service;

import com.example.Product.Ranking.System.dto.ReviewRequest;
import com.example.Product.Ranking.System.entity.Product;
import com.example.Product.Ranking.System.entity.ProductMetrics;
import com.example.Product.Ranking.System.entity.Review;
import com.example.Product.Ranking.System.repository.ProductMetricsRepository;
import com.example.Product.Ranking.System.repository.ProductRepository;
import com.example.Product.Ranking.System.repository.ReviewRepository;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl implements ReviewService { // ⭐ Implements Interface

    private final ReviewRepository reviewRepository;
    private final ProductMetricsRepository metricsRepository;
    private final ProductRepository productRepository;
    private final RankingService rankingService; // ⭐ Changed to Interface!

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             ProductMetricsRepository metricsRepository,
                             ProductRepository productRepository,
                             RankingService rankingService) {
        this.reviewRepository = reviewRepository;
        this.metricsRepository = metricsRepository;
        this.productRepository = productRepository;
        this.rankingService = rankingService;
    }

    @Override // ⭐ Added override
    public Review addReview(ReviewRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Review review = new Review();
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);

        ProductMetrics metrics = metricsRepository.findById(request.getProductId())
                .orElseGet(() -> {
                    ProductMetrics m = new ProductMetrics();
                    m.setProductId(request.getProductId());
                    return m;
                });

        metrics.setReviewCount(metrics.getReviewCount() + 1);

        double newRating = ((metrics.getRatingAverage() * (metrics.getReviewCount() - 1))
                + request.getRating()) / metrics.getReviewCount();

        metrics.setRatingAverage(newRating);

        double score = rankingService.calculateScore(
                newRating,
                metrics.getSalesCount(),
                metrics.getReviewCount(),
                metrics.getViewCount(),
                0);

        metrics.setRankingScore(score);
        metricsRepository.save(metrics);

        return savedReview;
    }
}