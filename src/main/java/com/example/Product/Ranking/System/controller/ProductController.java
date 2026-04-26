package com.example.Product.Ranking.System.controller;

import com.example.Product.Ranking.System.dto.ProductRequest;
import com.example.Product.Ranking.System.dto.ProductResponse;
import com.example.Product.Ranking.System.entity.Product;
import com.example.Product.Ranking.System.service.ProductService; // ⭐ Changed to Interface
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService; // ⭐ Changed to Interface

    // ⭐ Injecting the Interface, not the concrete implementation!
    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @PostMapping
    public ProductResponse createProduct(@RequestBody ProductRequest request){
        return productService.createProduct(request);
    }

    @GetMapping
    public List<Product> getAllProducts(){
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id){
        return productService.getProductById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
    }

    @GetMapping("/top-ranked")
    public List<ProductResponse> getTopProducts(){
        return productService.getTopRankedProducts();
    }
}