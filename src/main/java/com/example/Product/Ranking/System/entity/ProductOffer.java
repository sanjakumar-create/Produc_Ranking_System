package com.example.Product.Ranking.System.entity;


import jakarta.persistence.*;

@Entity
@Table(name="product_offers")
public class ProductOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long offerId;

    private int discountPercentage;
    //OneToOne(fetch = FetchType.LAZY) //
    @ManyToOne(fetch = FetchType.LAZY) //
    @JoinColumn(name="product_id")
    private Product product;

    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }

    public int getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
