package com.shippex.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
@Document("category")
public class Category {
    @Id private String id;
    @Indexed(unique = true) private String nameKey;
    @Indexed(unique = true) private String slug;
    @Indexed(unique = true) private String skuPrefix;
    private String name;
    private String description;
    private String imageUrl;
    private Boolean active = true;
    @CreatedDate private LocalDateTime createdAt;
    @LastModifiedDate private LocalDateTime updatedAt;
}
