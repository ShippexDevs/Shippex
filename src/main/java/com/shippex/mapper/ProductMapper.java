package com.shippex.mapper;

import com.shippex.dto.product.ProductResponse;
import com.shippex.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public static ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .sku(product.getSku())
                .description(product.getDescription())
                .category(product.getCategory())
                .categorySlug(product.getCategorySlug())
                .images(product.getImages())
                .currency(product.getCurrency())
                .currentPrice(product.getCurrentPrice())
                .originalPrice(product.getOriginalPrice())
                .unit(product.getUnit())
                .ratings(product.getRatings())
                .stock(product.getStock())
                .featured(product.getFeatured())
                .active(product.getActive())
                .displayOrder(product.getDisplayOrder())
                .deliveryTime(product.getDeliveryTime())
                .tags(product.getTags())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}