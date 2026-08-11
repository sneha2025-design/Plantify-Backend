package com.plantify.service;

import com.plantify.dto.*;
import com.plantify.entity.*;
import com.plantify.exception.BadRequestException;
import com.plantify.exception.ResourceNotFoundException;
import com.plantify.exception.UnauthorizedException;
import com.plantify.repository.CartItemRepository;
import com.plantify.repository.OrderRepository;
import com.plantify.repository.ProductRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Transactional
    public CreatePaymentOrderResponseDTO createRazorpayOrder(User user, String internalOrderId) {
        Order order = orderRepository.findById(internalOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", internalOrderId));

        if (!order.getUser().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Not authorized to access this order");
        }

        long amountInPaise = order.getTotalAmount().multiply(new BigDecimal("100")).longValue();
        String razorpayOrderId = null;

        // Create Razorpay Order via Official SDK — requires real test/live keys
        log.info("Razorpay config: keyId='{}', keySecret present={}", keyId, (keySecret != null && !keySecret.isEmpty()));
        try {
            RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", internalOrderId);

            com.razorpay.Order rzpOrder = razorpayClient.orders.create(orderRequest);
            razorpayOrderId = rzpOrder.get("id");
            log.info("Razorpay order created successfully: {}", razorpayOrderId);
        } catch (Exception e) {
            log.error("Razorpay SDK order creation failed: {}", e.getMessage());
            throw new BadRequestException("Failed to create Razorpay order. Check your RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET environment variables. Error: " + e.getMessage());
        }

        order.setRazorpayOrderId(razorpayOrderId);
        orderRepository.save(order);

        return CreatePaymentOrderResponseDTO.builder()
                .razorpayOrderId(razorpayOrderId)
                .amount(amountInPaise)
                .currency("INR")
                .keyId(keyId)
                .internalOrderId(internalOrderId)
                .build();
    }

    @Transactional
    public OrderDTO verifyPayment(User user, VerifyPaymentRequestDTO request) {
        Order order = orderRepository.findById(request.getInternalOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getInternalOrderId()));

        if (!order.getUser().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Not authorized to verify payment for this order");
        }

        boolean isSignatureValid = false;

        // Using Razorpay Official Verification Algorithm (HMAC-SHA256)
        if (request.getRazorpaySignature() != null && !request.getRazorpaySignature().isEmpty()) {
            try {
                JSONObject options = new JSONObject();
                options.put("razorpay_order_id", request.getRazorpayOrderId());
                options.put("razorpay_payment_id", request.getRazorpayPaymentId());
                options.put("razorpay_signature", request.getRazorpaySignature());

                isSignatureValid = Utils.verifyPaymentSignature(options, keySecret);
            } catch (Exception e) {
                // Manual HMAC SHA256 verification fallback
                String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
                String expectedSignature = hmacSha256(payload, keySecret);
                isSignatureValid = expectedSignature.equalsIgnoreCase(request.getRazorpaySignature());
            }
        }

        // No bypass — only real Razorpay signatures are accepted

        if (isSignatureValid) {
            order.setStatus(OrderStatus.SUCCESS);
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setRazorpayOrderId(request.getRazorpayOrderId());
            order.setRazorpayPaymentId(request.getRazorpayPaymentId());
            Order savedOrder = orderRepository.save(order);

            // Clear cart after successful payment verification
            cartItemRepository.deleteByUser(user);

            return orderService.mapToDTO(savedOrder);
        } else {
            order.setStatus(OrderStatus.FAILED);
            // Restore product stock on payment verification failure
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
            orderRepository.save(order);
            throw new BadRequestException("Payment verification failed: Invalid Razorpay signature.");
        }
    }

    @Transactional
    public OrderDTO cancelPayment(User user, String internalOrderId) {
        Order order = orderRepository.findById(internalOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", internalOrderId));

        if (!order.getUser().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Not authorized to modify this order");
        }

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.FAILED);
            // Restore stock
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
            orderRepository.save(order);
        }

        return orderService.mapToDTO(order);
    }

    private String hmacSha256(String data, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHexString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("HMAC SHA256 error: ", e);
            return "";
        }
    }

    private String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return sb.toString();
    }
}
