package com.example.Product.Ranking.System.controller;

import com.example.Product.Ranking.System.entity.ProductMetrics;
import com.example.Product.Ranking.System.repository.ProductMetricsRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trending")
public class TrendingController {

    private final ProductMetricsRepository metricsRepository;

    public TrendingController(ProductMetricsRepository metricsRepository){
        this.metricsRepository = metricsRepository;
    }

    @GetMapping
    public List<ProductMetrics> getTrendingProducts(){
        return metricsRepository.findTop10ByOrderByTrendingScoreDesc();
    }
    @PostMapping("/{id}/view")
    public String addView(@PathVariable Long id){

        ProductMetrics metrics =
                metricsRepository.findById(id).orElseThrow();

        metrics.setViewCount(metrics.getViewCount()+1);

        metricsRepository.save(metrics);

        return "View added";
    }
}