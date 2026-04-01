package com.example.Product.Ranking.System.service;

import com.example.Product.Ranking.System.dto.ProductRequest;
import com.example.Product.Ranking.System.dto.ProductResponse;
import com.example.Product.Ranking.System.entity.Product;
import com.example.Product.Ranking.System.entity.ProductMetrics;
import com.example.Product.Ranking.System.repository.ProductMetricsRepository; // Required Import
import com.example.Product.Ranking.System.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMetricsRepository metricsRepository; // Fixed: Added this
    private final RankingService rankingService;             // Fixed: Added this

    // Fixed: Constructor must include ALL three dependencies
    public ProductService(ProductRepository productRepository,
                          ProductMetricsRepository metricsRepository,
                          RankingService rankingService) {
        this.productRepository = productRepository;
        this.metricsRepository = metricsRepository;
        this.rankingService = rankingService;
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setBrand(request.getBrand());

        Product saved = productRepository.save(product);

        ProductResponse response = new ProductResponse();
        response.setId(saved.getProductId());
        response.setName(saved.getName());
        response.setPrice(saved.getPrice());

        return response;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Fixed: Now correctly tracks views and updates scores
    public Product getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductMetrics metrics = metricsRepository.findById(id)
                .orElseGet(() -> {
                    ProductMetrics m = new ProductMetrics();
                    m.setProductId(id);
                    return m;
                });

        // Increment views
        metrics.setViewCount(metrics.getViewCount() + 1);

        // Use rankingService to update the trending score live
        double newTrendingScore = rankingService.calculateTrendingScore(
                metrics.getSalesCount(),
                metrics.getViewCount(),
                metrics.getReviewCount(),
                metrics.getRatingAverage()
        );
        metrics.setTrendingScore(newTrendingScore);

        metricsRepository.save(metrics);
        return product;
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true) // ⭐ Keeps connection open just for this read
    public List<ProductResponse> getTopRankedProducts() {

        List<ProductMetrics> topMetrics = metricsRepository.findTop10ByOrderByRankingScoreDesc();

        return topMetrics.stream()
                .map(metrics -> {
                    Product p = metrics.getProduct(); // Fetches the lazy product

                    // Map it to our safe DTO
                    ProductResponse response = new ProductResponse();
                    response.setId(p.getProductId());
                    response.setName(p.getName());
                    response.setPrice(p.getPrice());
                    return response;
                })
                .collect(Collectors.toList());
    }
}