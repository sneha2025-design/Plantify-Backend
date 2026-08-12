package com.plantify.controller;

import com.plantify.dto.*;
import com.plantify.entity.User;
import com.plantify.service.AuthService;
import com.plantify.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final AuthService authService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartSummaryDTO>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        User user = authService.getCurrentAuthenticatedUser();
        CartSummaryDTO summary = cartService.addToCart(user, request);
        return ResponseEntity.ok(ApiResponse.success("Product added to cart", summary));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<CartSummaryDTO>> getCartItems() {
        User user = authService.getCurrentAuthenticatedUser();
        CartSummaryDTO summary = cartService.getCartSummary(user);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", summary));
    }

    @GetMapping("/items/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount() {
        User user = authService.getCurrentAuthenticatedUser();
        Integer count = cartService.getCartItemCount(user);
        return ResponseEntity.ok(ApiResponse.success("Cart count retrieved", count));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartSummaryDTO>> updateCartQuantity(@Valid @RequestBody UpdateCartRequest request) {
        User user = authService.getCurrentAuthenticatedUser();
        CartSummaryDTO summary = cartService.updateCartQuantity(user, request);
        return ResponseEntity.ok(ApiResponse.success("Cart updated successfully", summary));
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<ApiResponse<CartSummaryDTO>> removeFromCart(@PathVariable Long productId) {
        User user = authService.getCurrentAuthenticatedUser();
        CartSummaryDTO summary = cartService.removeFromCart(user, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", summary));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<CartSummaryDTO>> removeFromCartQueryParam(@RequestParam Long productId) {
        User user = authService.getCurrentAuthenticatedUser();
        CartSummaryDTO summary = cartService.removeFromCart(user, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", summary));
    }
}
