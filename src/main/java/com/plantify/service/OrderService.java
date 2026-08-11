package com.plantify.service;

import com.plantify.dto.OrderDTO;
import com.plantify.dto.OrderItemDTO;
import com.plantify.entity.*;
import com.plantify.exception.BadRequestException;
import com.plantify.exception.ResourceNotFoundException;
import com.plantify.exception.UnauthorizedException;
import com.plantify.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderDTO checkout(User user, String shippingAddress, String paymentMethodStr) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cannot place order with an empty cart");
        }

        // Validate stock for all items first
        for (CartItem item : cartItems) {
            if (item.getQuantity() > item.getProduct().getStock()) {
                throw new BadRequestException(String.format("Product '%s' has insufficient stock. Available: %d, Requested: %d",
                        item.getProduct().getName(), item.getProduct().getStock(), item.getQuantity()));
            }
        }

        BigDecimal grandTotal = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PaymentMethod pm = "COD".equalsIgnoreCase(paymentMethodStr) ? PaymentMethod.COD : PaymentMethod.RAZORPAY;

        Order order = Order.builder()
                .orderId(orderId)
                .user(user)
                .totalAmount(grandTotal)
                .status(OrderStatus.PENDING)
                .paymentMethod(pm)
                .paymentStatus(PaymentStatus.UNPAID)
                .shippingAddress(shippingAddress != null && !shippingAddress.trim().isEmpty()
                        ? shippingAddress
                        : "123 Botanical Avenue, Garden City, CA 90210")
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            BigDecimal price = product.getPrice();
            BigDecimal total = price.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            // Deduct stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .pricePerUnit(price)
                    .totalPrice(total)
                    .build();
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        // If COD, clear user's cart immediately (since COD skips payment verification)
        if (pm == PaymentMethod.COD) {
            cartItemRepository.deleteByUser(user);
        }

        return mapToDTO(savedOrder);
    }

    public List<OrderDTO> getUserOrders(User user) {
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return orders.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public OrderDTO getOrderDetails(User user, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You are not authorized to view this order");
        }

        return mapToDTO(order);
    }

    @Transactional
    public OrderDTO simulatePayment(User user, String orderId, boolean paymentSuccess) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You are not authorized to process payment for this order");
        }

        if (paymentSuccess) {
            order.setStatus(OrderStatus.SUCCESS);
        } else {
            order.setStatus(OrderStatus.FAILED);
            // Restore product stock on failure
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        return mapToDTO(orderRepository.save(order));
    }

    @Transactional
    public OrderDTO cancelOrder(User user, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You are not authorized to cancel this order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.FAILED) {
            throw new BadRequestException("Order is already " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        return mapToDTO(orderRepository.save(order));
    }

    public OrderDTO mapToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream().map(item -> {
            String catName = item.getProduct().getCategory() != null ? item.getProduct().getCategory().getCategoryName() : null;
            String mainImage = (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty())
                    ? item.getProduct().getImages().get(0).getImageUrl()
                    : ProductService.getFallbackUrl(catName, item.getProduct().getProductId());

            return OrderItemDTO.builder()
                    .id(item.getId())
                    .productId(item.getProduct().getProductId())
                    .productName(item.getProduct().getName())
                    .imageUrl(mainImage)
                    .quantity(item.getQuantity())
                    .pricePerUnit(item.getPricePerUnit())
                    .totalPrice(item.getTotalPrice())
                    .build();
        }).collect(Collectors.toList());

        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .userFullName(order.getUser().getFullName())
                .userEmail(order.getUser().getEmail())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .shippingAddress(order.getShippingAddress())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
