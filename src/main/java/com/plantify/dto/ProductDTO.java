package com.plantify.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Double rating;
    private Integer reviewCount;
    private Long categoryId;
    private String categoryName;
    private Boolean isFeatured;
    private List<String> imageUrls;
}
