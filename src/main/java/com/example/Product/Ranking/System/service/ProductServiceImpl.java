package com.example.Product.Ranking.System.service;

import com.example.Product.Ranking.System.dto.ProductRequest;
import com.example.Product.Ranking.System.dto.ProductResponse;
import com.example.Product.Ranking.System.entity.Product;
import com.example.Product.Ranking.System.entity.ProductMetrics;
import com.example.Product.Ranking.System.repository.ProductMetricsRepository;
import com.example.Product.Ranking.System.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Qualifier; // ⭐ Required Import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final NotificationService notificationService;
    private final ProductRepository productRepository;
    private final ProductMetricsRepository metricsRepository;
    private final RankingService rankingService;

    // ⭐ 1. Add @Qualifier directly inside the constructor parameter
    public ProductServiceImpl(@Qualifier("emailNotification") NotificationService notificationService,
                              ProductRepository productRepository,
                              ProductMetricsRepository metricsRepository,
                              RankingService rankingService) {
        this.notificationService = notificationService;
        this.productRepository = productRepository;
        this.metricsRepository = metricsRepository;
        this.rankingService = rankingService;
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setBrand(request.getBrand());

        Product saved = productRepository.save(product);

        // ⭐ 2. Actually trigger the notification to prove the injected bean works!
        notificationService.sendNotification("New product created: " + saved.getName());

        ProductResponse response = new ProductResponse();
        response.setId(saved.getProductId());
        response.setName(saved.getName());
        response.setPrice(saved.getPrice());

        return response;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductMetrics metrics = metricsRepository.findById(id)
                .orElseGet(() -> {
                    ProductMetrics m = new ProductMetrics();
                    m.setProductId(id);
                    return m;
                });

        metrics.setViewCount(metrics.getViewCount() + 1);

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

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getTopRankedProducts() {
        List<ProductMetrics> topMetrics = metricsRepository.findTop10ByOrderByRankingScoreDesc();

        return topMetrics.stream()
                .map(metrics -> {
                    Product p = metrics.getProduct();
                    ProductResponse response = new ProductResponse();
                    response.setId(p.getProductId());
                    response.setName(p.getName());
                    response.setPrice(p.getPrice());
                    return response;
                })
                .collect(Collectors.toList());
    }
}