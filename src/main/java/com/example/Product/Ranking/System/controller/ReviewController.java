package com.example.Product.Ranking.System.controller;



import com.example.Product.Ranking.System.dto.ReviewRequest;
import com.example.Product.Ranking.System.entity.Review;
import com.example.Product.Ranking.System.service.ReviewServiceImpl;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewServiceImpl reviewService;

    public ReviewController(ReviewServiceImpl reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review addReview(@RequestBody ReviewRequest request) {
        return reviewService.addReview(request);
    }
}