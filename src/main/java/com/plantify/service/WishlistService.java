package com.plantify.service;

import com.plantify.dto.ProductDTO;
import com.plantify.entity.Product;
import com.plantify.entity.User;
import com.plantify.entity.WishlistItem;
import com.plantify.exception.ResourceNotFoundException;
import com.plantify.repository.ProductRepository;
import com.plantify.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public List<ProductDTO> getUserWishlist(User user) {
        List<WishlistItem> items = wishlistItemRepository.findByUserOrderByCreatedAtDesc(user);
        return items.stream()
                .map(item -> productService.mapToDTO(item.getProduct()))
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO addToWishlist(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Optional<WishlistItem> existing = wishlistItemRepository.findByUserAndProduct(user, product);
        if (existing.isEmpty()) {
            WishlistItem wishlistItem = WishlistItem.builder()
                    .user(user)
                    .product(product)
                    .build();
            wishlistItemRepository.save(wishlistItem);
        }

        return productService.mapToDTO(product);
    }

    @Transactional
    public void removeFromWishlist(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        wishlistItemRepository.deleteByUserAndProduct(user, product);
    }

    public boolean isProductWishlisted(User user, Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        return productOpt.map(product -> wishlistItemRepository.existsByUserAndProduct(user, product)).orElse(false);
    }
}
