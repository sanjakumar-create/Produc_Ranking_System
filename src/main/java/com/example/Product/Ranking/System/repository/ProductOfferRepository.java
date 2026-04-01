package com.example.Product.Ranking.System.repository;
import com.example.Product.Ranking.System.entity.Product;
import com.example.Product.Ranking.System.entity.ProductOffer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductOfferRepository
        extends JpaRepository<ProductOffer, Long> {
    @Query("""
    SELECT o.product
    FROM ProductOffer o
    ORDER BY o.discountPercentage DESC
    """)
    List<Product> findBestOfferProducts(Pageable pageable);
}