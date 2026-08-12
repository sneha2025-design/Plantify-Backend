package com.plantify.controller;

import com.plantify.dto.ApiResponse;
import com.plantify.dto.CheckoutRequestDTO;
import com.plantify.dto.OrderDTO;
import com.plantify.entity.User;
import com.plantify.service.AuthService;
import com.plantify.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderDTO>> checkout(@RequestBody(required = false) CheckoutRequestDTO request) {
        User user = authService.getCurrentAuthenticatedUser();
        String shippingAddress = (request != null) ? request.getShippingAddress() : null;
        String paymentMethod = (request != null) ? request.getPaymentMethod() : "RAZORPAY";
        OrderDTO order = orderService.checkout(user, shippingAddress, paymentMethod);
        return new ResponseEntity<>(ApiResponse.success("Order created successfully", order), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getUserOrders() {
        User user = authService.getCurrentAuthenticatedUser();
        List<OrderDTO> orders = orderService.getUserOrders(user);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderDetails(@PathVariable String orderId) {
        User user = authService.getCurrentAuthenticatedUser();
        OrderDTO order = orderService.getOrderDetails(user, orderId);
        return ResponseEntity.ok(ApiResponse.success("Order details retrieved", order));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<OrderDTO>> simulatePayment(@PathVariable String orderId,
                                                                 @RequestParam(defaultValue = "true") boolean success) {
        User user = authService.getCurrentAuthenticatedUser();
        OrderDTO order = orderService.simulatePayment(user, orderId, success);
        String msg = success ? "Payment successful. Order confirmed!" : "Payment failed. Order status updated to FAILED.";
        return ResponseEntity.ok(ApiResponse.success(msg, order));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(@PathVariable String orderId) {
        User user = authService.getCurrentAuthenticatedUser();
        OrderDTO order = orderService.cancelOrder(user, orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully and product stock restored.", order));
    }
}
