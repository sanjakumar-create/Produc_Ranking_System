package com.example.Product.Ranking.System;

import com.example.Product.Ranking.System.dto.ProductRequest;
import com.example.Product.Ranking.System.dto.ProductResponse;
import com.example.Product.Ranking.System.dto.ReviewRequest;
import com.example.Product.Ranking.System.entity.Product;
import com.example.Product.Ranking.System.entity.ProductMetrics;
import com.example.Product.Ranking.System.repository.ProductMetricsRepository;
import com.example.Product.Ranking.System.repository.ProductRepository;
import com.example.Product.Ranking.System.service.NotificationService;
import com.example.Product.Ranking.System.service.ProductService;
import com.example.Product.Ranking.System.service.RankingService;
import com.example.Product.Ranking.System.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@TestPropertySource(properties = {"ranking.default-weight=5.0"})
class ProductRankingSystemApplicationTests {

	@Autowired
	private ProductService productService;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private RankingService rankingService;

	@Autowired
	private ProductMetricsRepository metricsRepository;

	@Autowired
	private ProductRepository productRepository; // ⭐ ADDED THIS!

	@MockitoBean(name = "emailNotification")
	private NotificationService emailNotificationService;

	private Long testProductId;

	@BeforeEach
	void setUp() {
		ProductRequest request = new ProductRequest();
		request.setName("Test Smartphone");
		request.setDescription("A high-end smartphone");
		request.setPrice(999.99);
		request.setBrand("TechBrand");

		ProductResponse response = productService.createProduct(request);
		testProductId = response.getId();

		// ⭐ FIX: Safely grab the product using the repository, bypassing the service!
		Product product = productRepository.findById(testProductId).orElseThrow();

		ProductMetrics metrics = new ProductMetrics();
		metrics.setProduct(product); // Link the actual product!
		metrics.setRatingAverage(0.0);
		metrics.setReviewCount(0);
		metrics.setSalesCount(0);
		metrics.setViewCount(0);
		metrics.setRankingScore(5.0); // ⭐ Ensure it has a base score so Top 10 test passes
		metricsRepository.save(metrics);
	}

	@Test
	void contextLoads() {
		assertNotNull(productService);
	}

	@Test
	void testCreateProductTriggersNotification() {
		verify(emailNotificationService, times(1)).sendNotification(anyString());
	}

	@Test
	void testGetProductIncrementsViewsAndTrendingScore() {
		Product product = productService.getProductById(testProductId);

		assertNotNull(product);
		ProductMetrics metrics = metricsRepository.findById(testProductId).orElseThrow();

		assertEquals(1, metrics.getViewCount(), "View count should increment to 1");
		assertTrue(metrics.getTrendingScore() > 0, "Trending score should be calculated");
	}

	@Test
	void testAddReviewUpdatesRatingAndRankingScore() {
		ReviewRequest reviewRequest = new ReviewRequest();
		reviewRequest.setProductId(testProductId);
		reviewRequest.setRating(5);
		reviewRequest.setComment("Amazing product!");
		reviewService.addReview(reviewRequest);

		ProductMetrics metrics = metricsRepository.findById(testProductId).orElseThrow();
		assertEquals(1, metrics.getReviewCount());
		assertEquals(5.0, metrics.getRatingAverage());
	}

	@Test
	@DirtiesContext
	void testUpdateRankingScoresScheduledTask() {
		rankingService.updateRankingScores();

		List<ProductResponse> topProducts = productService.getTopRankedProducts();
		assertFalse(topProducts.isEmpty());
		assertEquals("Test Smartphone", topProducts.get(0).getName());
	}
}