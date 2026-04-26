package com.example.Product.Ranking.System.service;

import com.example.Product.Ranking.System.dto.ReviewRequest;
import com.example.Product.Ranking.System.entity.Review;

public interface ReviewService {
    Review addReview(ReviewRequest request);
}