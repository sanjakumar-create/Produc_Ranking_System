package com.example.Product.Ranking.System.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ranking")
public class RankingProperties {

    private double defaultWeight;
    private int maxScore;

    // You MUST have getters and setters for this to work!
    public double getDefaultWeight() { return defaultWeight; }
    public void setDefaultWeight(double defaultWeight) { this.defaultWeight = defaultWeight; }

    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }
}