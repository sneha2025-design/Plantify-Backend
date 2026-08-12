package com.plantify.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistRequestDTO {
    @NotNull(message = "Product ID is required")
    private Long productId;
}
