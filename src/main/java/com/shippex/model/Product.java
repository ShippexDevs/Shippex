package com.shippex.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Document(collection = "product")
@NoArgsConstructor
public class Product {
    @Id
    private String id; // mongo db id
    private String name;
    private String brand;
    @Indexed(unique = true)
    private String sku; // internal id
    private String description;
    private String category;
    private String categorySlug;
    private List<String> images;
    private String currency;
    private BigDecimal currentPrice;
    private BigDecimal originalPrice;
    private String unit;
    private Double ratings;
    private Integer stock;
    private Boolean featured;
    private Boolean active;
    private Integer displayOrder;
    private String deliveryTime;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    private List<String> tags;

    public Product(String name, String brand, String sku, String description,
                   String category, String categorySlug, List<String> images,
                   String currency, BigDecimal currentPrice, BigDecimal originalPrice,
                   String unit, Integer stock, Integer displayOrder) {
        this.name = name;
        this.brand = brand;
        this.sku = sku;
        this.description = description;
        this.category = category;
        this.categorySlug = categorySlug;
        this.images = images;
        this.currency = currency;
        this.currentPrice = currentPrice;
        this.originalPrice = originalPrice;
        this.unit = unit;
        this.ratings = 5.0;
        this.stock = stock;
        this.featured = Boolean.FALSE;
        this.active = Boolean.FALSE;
        this.displayOrder = displayOrder;
        this.deliveryTime = "24hours";
    }

    @Override
    public String toString() {
        return "Product{" +
                "brand='" + brand + '\'' +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", currentPrice='" + currentPrice +" "+ currency + '\'' +
                ", unit='" + unit + '\'' +
                ", stock='" + stock + '\'' +
                '}';
    }
}
