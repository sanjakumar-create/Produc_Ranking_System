package com.example.Product.Ranking.System.service;

public interface RankingService {
    double calculateScore(double ratingAverage, int salesCount, int reviewCount, int viewCount, int discountPercentage);
    double calculateTrendingScore(int recentSales, int recentViews, int recentReviews, double ratingAverage);
    void updateRankingScores();
}