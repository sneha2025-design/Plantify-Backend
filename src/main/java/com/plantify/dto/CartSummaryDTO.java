package com.plantify.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartSummaryDTO {
    private List<CartItemDTO> items;
    private Integer totalItems;
    private BigDecimal grandTotal;
}
