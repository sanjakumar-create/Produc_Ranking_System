package com.example.Product.Ranking.System.controller;

import com.example.Product.Ranking.System.entity.ProductMetrics;
import com.example.Product.Ranking.System.repository.ProductMetricsRepository;
import com.example.Product.Ranking.System.service.RankingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ranking")
public class RankingController {

    private final RankingService rankingService;
    private final ProductMetricsRepository metricsRepository;

    public RankingController(RankingService rankingService,
                             ProductMetricsRepository metricsRepository){
        this.rankingService = rankingService;
        this.metricsRepository = metricsRepository;
    }

    @PostMapping("/update")
    public String updateRanking(){

        rankingService.updateRankingScores();
        return "Ranking updated";
    }

    @GetMapping("/top-products")
    public List<ProductMetrics> getTopProducts(){

        return metricsRepository.findTop10ByOrderByRankingScoreDesc();
    }
}