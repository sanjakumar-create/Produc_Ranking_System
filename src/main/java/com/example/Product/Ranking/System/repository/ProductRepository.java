package com.example.Product.Ranking.System.repository;


import java.util.List;

import com.example.Product.Ranking.System.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findTop10ByOrderByProductIdDesc();

}