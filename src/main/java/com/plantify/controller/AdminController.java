package com.plantify.controller;

import com.plantify.dto.*;
import com.plantify.entity.Role;
import com.plantify.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==========================================
    // DASHBOARD & OVERVIEW
    // ==========================================

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardStatsDTO>> getDashboardAnalytics() {
        AdminDashboardStatsDTO stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard analytics retrieved successfully", stats));
    }

    // ==========================================
    // 1. PRODUCT MANAGEMENT (ADMIN)
    // ==========================================

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO dto) {
        ProductDTO created = adminService.createProduct(dto);
        return new ResponseEntity<>(ApiResponse.success("Product created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(@PathVariable Long id,
                                                                 @Valid @RequestBody ProductDTO dto) {
        ProductDTO updated = adminService.updateProduct(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", "DELETED"));
    }

    // ==========================================
    // 2. USER MANAGEMENT (ADMIN)
    // ==========================================

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("All users retrieved successfully", users));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long userId) {
        UserDTO user = adminService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("User details retrieved successfully", user));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long userId,
                                                           @Valid @RequestBody AdminUpdateUserRequest request) {
        UserDTO updated = adminService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User details updated successfully", updated));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserRole(@PathVariable Long userId,
                                                               @RequestParam Role role) {
        UserDTO updated = adminService.updateUserRole(userId, role);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully to " + role, updated));
    }

    // ==========================================
    // 3. ORDER MANAGEMENT (ADMIN)
    // ==========================================

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrders() {
        List<OrderDTO> orders = adminService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success("All customer orders retrieved successfully", orders));
    }

    @PutMapping("/orders/{orderId}/mark-paid")
    public ResponseEntity<ApiResponse<OrderDTO>> markOrderAsPaid(@PathVariable String orderId) {
        OrderDTO updated = adminService.markOrderAsPaid(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order marked as paid successfully", updated));
    }

    // ==========================================
    // 4. BUSINESS ANALYTICS (ADMIN)
    // ==========================================

    @GetMapping("/analytics/daily")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getDailyRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AnalyticsResponse response = adminService.getDailyRevenue(date != null ? date : LocalDate.now());
        return ResponseEntity.ok(ApiResponse.success("Daily revenue analytics retrieved successfully", response));
    }

    @GetMapping("/analytics/monthly")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getMonthlyRevenue(
            @RequestParam int year,
            @RequestParam int month) {
        AnalyticsResponse response = adminService.getMonthlyRevenue(year, month);
        return ResponseEntity.ok(ApiResponse.success("Monthly revenue analytics retrieved successfully", response));
    }

    @GetMapping("/analytics/yearly")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getYearlyRevenue(
            @RequestParam int year) {
        AnalyticsResponse response = adminService.getYearlyRevenue(year);
        return ResponseEntity.ok(ApiResponse.success("Yearly revenue analytics retrieved successfully", response));
    }

    @GetMapping("/analytics/overall")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getOverallRevenue() {
        AnalyticsResponse response = adminService.getOverallRevenue();
        return ResponseEntity.ok(ApiResponse.success("Overall revenue analytics retrieved successfully", response));
    }
}
