package com.example.Product.Ranking.System.entity;



import jakarta.persistence.*;


@Entity
@Table(name="product_metrics")
public class ProductMetrics {

    @Id
    private Long productId;
    @OneToOne(fetch = FetchType.LAZY) //
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "sales_count")
    private int salesCount;

    @Column(name = "review_count")
    private int reviewCount;

    @Column(name = "rating_average")
    private double ratingAverage;

    @Column(name = "view_count")
    private int viewCount;

    @Column(name = "ranking_score")
    private double rankingScore;

    @Column(name = "trending_score")
    private double trendingScore;
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public double getRatingAverage() {
        return ratingAverage;
    }

    public void setRatingAverage(double ratingAverage) {
        this.ratingAverage = ratingAverage;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public double getRankingScore() {
        return rankingScore;
    }

    public void setRankingScore(double rankingScore) {
        this.rankingScore = rankingScore;
    }
    public double getTrendingScore() {
        return trendingScore;
    }

    public void setTrendingScore(double trendingScore) {
        this.trendingScore = trendingScore;
    }
    public Product getProduct() {
        return product;
    }
    // Add this to link the actual Product entity!


    public void setProduct(Product product) {
        this.product = product;
    }
}