package com.example.Product.Ranking.System.service;

import com.example.Product.Ranking.System.entity.Product;
import com.example.Product.Ranking.System.entity.ProductOffer;
import com.example.Product.Ranking.System.repository.ProductOfferRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfferService {

    private final ProductOfferRepository offerRepository;

    public OfferService(ProductOfferRepository offerRepository){
        this.offerRepository = offerRepository;
    }

    public ProductOffer addOffer(ProductOffer offer){
        return offerRepository.save(offer);
    }

    public List<Product> getBestOffers(){
        return offerRepository.findBestOfferProducts(PageRequest.of(0,10));
    }
}