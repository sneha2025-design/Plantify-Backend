package com.plantify.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardStatsDTO {
    private Long totalProducts;
    private Long totalUsers;
    private Long totalOrders;
    private BigDecimal overallRevenue;
}
