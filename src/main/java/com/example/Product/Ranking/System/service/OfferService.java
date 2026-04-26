package com.example.Product.Ranking.System.service;

import com.example.Product.Ranking.System.entity.Product;
import com.example.Product.Ranking.System.entity.ProductOffer;
import java.util.List;

public interface OfferService {
    ProductOffer addOffer(ProductOffer offer);
    List<Product> getBestOffers();
}