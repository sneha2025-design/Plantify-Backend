package com.plantify.controller;

import com.plantify.dto.*;
import com.plantify.entity.User;
import com.plantify.service.AuthService;
import com.plantify.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthService authService;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<CreatePaymentOrderResponseDTO>> createOrder(@RequestBody CreatePaymentOrderRequestDTO request) {
        User user = authService.getCurrentAuthenticatedUser();
        CreatePaymentOrderResponseDTO response = paymentService.createRazorpayOrder(user, request.getInternalOrderId());
        return ResponseEntity.ok(ApiResponse.success("Razorpay order created successfully", response));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<OrderDTO>> verifyPayment(@RequestBody VerifyPaymentRequestDTO request) {
        User user = authService.getCurrentAuthenticatedUser();
        OrderDTO order = paymentService.verifyPayment(user, request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully. Order confirmed!", order));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelPayment(@RequestParam String internalOrderId) {
        User user = authService.getCurrentAuthenticatedUser();
        OrderDTO order = paymentService.cancelPayment(user, internalOrderId);
        return ResponseEntity.ok(ApiResponse.success("Payment cancelled. Order marked failed.", order));
    }
}
