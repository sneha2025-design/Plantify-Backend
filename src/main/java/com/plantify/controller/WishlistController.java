package com.plantify.controller;

import com.plantify.dto.ApiResponse;
import com.plantify.dto.ProductDTO;
import com.plantify.dto.WishlistRequestDTO;
import com.plantify.entity.User;
import com.plantify.service.AuthService;
import com.plantify.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getUserWishlist() {
        User user = authService.getCurrentAuthenticatedUser();
        List<ProductDTO> wishlist = wishlistService.getUserWishlist(user);
        return ResponseEntity.ok(ApiResponse.success("Wishlist items retrieved", wishlist));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ProductDTO>> addToWishlist(@Valid @RequestBody WishlistRequestDTO request) {
        User user = authService.getCurrentAuthenticatedUser();
        ProductDTO addedProduct = wishlistService.addToWishlist(user, request.getProductId());
        return new ResponseEntity<>(ApiResponse.success("Product added to wishlist", addedProduct), HttpStatus.CREATED);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(@PathVariable Long productId) {
        User user = authService.getCurrentAuthenticatedUser();
        wishlistService.removeFromWishlist(user, productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist", "REMOVED"));
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkWishlistStatus(@PathVariable Long productId) {
        User user = authService.getCurrentAuthenticatedUser();
        boolean isWishlisted = wishlistService.isProductWishlisted(user, productId);
        return ResponseEntity.ok(ApiResponse.success("Wishlist status checked", Map.of("wishlisted", isWishlisted)));
    }
}
