package com.example.Product.Ranking.System.controller;


import com.example.Product.Ranking.System.entity.Product;
import com.example.Product.Ranking.System.entity.ProductOffer;
import com.example.Product.Ranking.System.repository.ProductOfferRepository;
import com.example.Product.Ranking.System.service.OfferService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService){
        this.offerService = offerService;
    }

    @PostMapping
    public ProductOffer addOffer(@RequestBody ProductOffer offer){
        return offerService.addOffer(offer);
    }

    @GetMapping("/best-offers")
    public List<Product> bestOffers(){

        return offerService.getBestOffers();
    }
}
