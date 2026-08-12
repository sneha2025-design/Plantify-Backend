package com.plantify.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsResponse {
    private String period; // DAILY, MONTHLY, YEARLY, OVERALL
    private String filterValue; // e.g. "2026-08-05", "2026-08", "2026", "ALL"
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    
    @Builder.Default
    private List<OrderDTO> transactions = new ArrayList<>();
}
