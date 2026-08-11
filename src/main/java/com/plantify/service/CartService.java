package com.plantify.service;

import com.plantify.dto.AddToCartRequest;
import com.plantify.dto.CartItemDTO;
import com.plantify.dto.CartSummaryDTO;
import com.plantify.dto.UpdateCartRequest;
import com.plantify.entity.CartItem;
import com.plantify.entity.Product;
import com.plantify.entity.User;
import com.plantify.exception.BadRequestException;
import com.plantify.exception.ResourceNotFoundException;
import com.plantify.repository.CartItemRepository;
import com.plantify.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartSummaryDTO getCartSummary(User user) {
        List<CartItem> items = cartItemRepository.findByUser(user);

        List<CartItemDTO> itemDTOs = items.stream().map(item -> {
            BigDecimal total = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            String catName = item.getProduct().getCategory() != null ? item.getProduct().getCategory().getCategoryName() : null;
            String mainImage = (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty())
                    ? item.getProduct().getImages().get(0).getImageUrl()
                    : ProductService.getFallbackUrl(catName, item.getProduct().getProductId());

            return CartItemDTO.builder()
                    .id(item.getId())
                    .productId(item.getProduct().getProductId())
                    .productName(item.getProduct().getName())
                    .productPrice(item.getProduct().getPrice())
                    .imageUrl(mainImage)
                    .quantity(item.getQuantity())
                    .availableStock(item.getProduct().getStock())
                    .totalPrice(total)
                    .build();
        }).collect(Collectors.toList());

        BigDecimal grandTotal = itemDTOs.stream()
                .map(CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = itemDTOs.stream().mapToInt(CartItemDTO::getQuantity).sum();

        return CartSummaryDTO.builder()
                .items(itemDTOs)
                .totalItems(totalItems)
                .grandTotal(grandTotal)
                .build();
    }

    public Integer getCartItemCount(User user) {
        List<CartItem> items = cartItemRepository.findByUser(user);
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    @Transactional
    public CartSummaryDTO addToCart(User user, AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        Optional<CartItem> existingItemOpt = cartItemRepository.findByUserAndProduct(user, product);

        int currentQtyInCart = existingItemOpt.map(CartItem::getQuantity).orElse(0);
        int newTotalQty = currentQtyInCart + request.getQuantity();

        if (newTotalQty > product.getStock()) {
            throw new BadRequestException(String.format("Cannot add quantity %d. Only %d items available in stock.", request.getQuantity(), product.getStock()));
        }

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(newTotalQty);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
        }

        return getCartSummary(user);
    }

    @Transactional
    public CartSummaryDTO updateCartQuantity(User user, UpdateCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        Optional<CartItem> existingItemOpt = cartItemRepository.findByUserAndProduct(user, product);

        if (existingItemOpt.isEmpty()) {
            throw new ResourceNotFoundException("Cart item not found for product id: " + request.getProductId());
        }

        CartItem item = existingItemOpt.get();

        // Business rule: Quantity reaching zero or negative removes the product automatically
        if (request.getQuantity() <= 0) {
            cartItemRepository.delete(item);
        } else {
            if (request.getQuantity() > product.getStock()) {
                throw new BadRequestException(String.format("Requested quantity (%d) exceeds available stock (%d)", request.getQuantity(), product.getStock()));
            }
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }

        return getCartSummary(user);
    }

    @Transactional
    public CartSummaryDTO removeFromCart(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        cartItemRepository.findByUserAndProduct(user, product).ifPresent(cartItemRepository::delete);

        return getCartSummary(user);
    }
}
