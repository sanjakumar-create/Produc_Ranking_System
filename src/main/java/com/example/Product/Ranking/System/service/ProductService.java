package com.example.Product.Ranking.System.service;

import com.example.Product.Ranking.System.dto.ProductRequest;
import com.example.Product.Ranking.System.dto.ProductResponse;
import com.example.Product.Ranking.System.entity.Product;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    List<Product> getAllProducts();
    Product getProductById(Long id);
    void deleteProduct(Long id);
    List<ProductResponse> getTopRankedProducts();
}