package com.plantify.service;

import com.plantify.dto.*;
import com.plantify.entity.*;
import com.plantify.exception.BadRequestException;
import com.plantify.exception.ResourceNotFoundException;
import com.plantify.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductService productService;

    // ==========================================
    // 1. PRODUCT MANAGEMENT (ADMIN)
    // ==========================================

    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new BadRequestException("Product name is required");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Product price must be greater than zero");
        }
        if (dto.getStock() == null || dto.getStock() < 0) {
            throw new BadRequestException("Product stock quantity cannot be negative");
        }
        if (dto.getCategoryId() == null) {
            throw new BadRequestException("Product category ID is required");
        }

        // Validate category exists
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Invalid category: Category ID " + dto.getCategoryId() + " does not exist"));

        // Reject duplicate product (same name + category)
        if (productRepository.existsByNameIgnoreCaseAndCategory_CategoryId(dto.getName().trim(), category.getCategoryId())) {
            throw new BadRequestException("A product with name '" + dto.getName().trim() + "' already exists in category '" + category.getCategoryName() + "'");
        }

        Product product = Product.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription() != null ? dto.getDescription().trim() : "")
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(category)
                .isFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false)
                .build();

        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            List<ProductImage> images = dto.getImageUrls().stream()
                    .filter(StringUtils::hasText)
                    .map(url -> ProductImage.builder().product(product).imageUrl(url.trim()).build())
                    .collect(Collectors.toList());
            product.setImages(images);
        }

        Product saved = productRepository.save(product);
        log.info("Admin successfully created new product: {} (ID: {})", saved.getName(), saved.getProductId());
        return productService.mapToDTO(saved);
    }

    @Transactional
    public ProductDTO updateProduct(Long productId, ProductDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!StringUtils.hasText(dto.getName())) {
            throw new BadRequestException("Product name is required");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Product price must be greater than zero");
        }
        if (dto.getStock() == null || dto.getStock() < 0) {
            throw new BadRequestException("Product stock quantity cannot be negative");
        }

        Category category = product.getCategory();
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new BadRequestException("Invalid category: Category ID " + dto.getCategoryId() + " does not exist"));
        }

        // Reject duplicate name in same category excluding current product
        if (productRepository.existsByNameIgnoreCaseAndCategory_CategoryIdAndProductIdNot(dto.getName().trim(), category.getCategoryId(), productId)) {
            throw new BadRequestException("Another product with name '" + dto.getName().trim() + "' already exists in category '" + category.getCategoryName() + "'");
        }

        product.setName(dto.getName().trim());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription().trim());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(category);
        if (dto.getIsFeatured() != null) product.setIsFeatured(dto.getIsFeatured());

        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            product.getImages().clear();
            List<ProductImage> newImages = dto.getImageUrls().stream()
                    .filter(StringUtils::hasText)
                    .map(url -> ProductImage.builder().product(product).imageUrl(url.trim()).build())
                    .collect(Collectors.toList());
            product.getImages().addAll(newImages);
        }

        Product saved = productRepository.save(product);
        return productService.mapToDTO(saved);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Delete references in cart and wishlist to avoid FK constraints
        cartItemRepository.deleteByProduct(product);
        wishlistItemRepository.deleteByProduct(product);

        productRepository.delete(product);
        log.info("Admin deleted product ID: {}", productId);
    }

    // ==========================================
    // 2. USER MANAGEMENT (ADMIN)
    // ==========================================

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapUserToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapUserToDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate username
        if (!StringUtils.hasText(request.getUsername())) {
            throw new BadRequestException("Username cannot be empty");
        }
        String newUsername = request.getUsername().trim();
        if (!newUsername.equalsIgnoreCase(user.getUsername()) && userRepository.existsByUsername(newUsername)) {
            throw new BadRequestException("Username '" + newUsername + "' is already taken");
        }

        // Validate email
        if (!StringUtils.hasText(request.getEmail())) {
            throw new BadRequestException("Email address cannot be empty");
        }
        String newEmail = request.getEmail().trim();
        if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new BadRequestException("Email address '" + newEmail + "' is already registered");
        }

        // Validate role
        if (request.getRole() == null) {
            throw new BadRequestException("User role must be specified (CUSTOMER or ADMIN)");
        }

        user.setUsername(newUsername);
        user.setEmail(newEmail);
        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName().trim());
        }
        if (StringUtils.hasText(request.getMobileNumber())) {
            user.setMobileNumber(request.getMobileNumber().trim());
        }
        user.setRole(request.getRole());

        // Password update with BCrypt encoding if provided
        if (StringUtils.hasText(request.getPassword())) {
            if (request.getPassword().length() < 6) {
                throw new BadRequestException("Password must be at least 6 characters long");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            log.info("Admin updated password for user ID: {} (Hashed with BCrypt)", userId);
        }

        User saved = userRepository.save(user);
        log.info("Admin updated user details for ID: {} (Role: {})", saved.getUserId(), saved.getRole());
        return mapUserToDTO(saved);
    }

    @Transactional
    public UserDTO updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (newRole == null) {
            throw new BadRequestException("Role cannot be null");
        }
        user.setRole(newRole);
        User saved = userRepository.save(user);
        return mapUserToDTO(saved);
    }

    // ==========================================
    // 3. BUSINESS ANALYTICS (ADMIN)
    // ==========================================

    public AnalyticsResponse getDailyRevenue(LocalDate date) {
        if (date == null) date = LocalDate.now();
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        return buildAnalyticsResponse("DAILY", date.toString(), orders);
    }

    public AnalyticsResponse getMonthlyRevenue(int year, int month) {
        if (year <= 2000 || month < 1 || month > 12) {
            throw new BadRequestException("Invalid year or month specified");
        }
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        return buildAnalyticsResponse("MONTHLY", String.format("%d-%02d", year, month), orders);
    }

    public AnalyticsResponse getYearlyRevenue(int year) {
        if (year <= 2000) {
            throw new BadRequestException("Invalid year specified");
        }
        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(year, 12, 31).atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        return buildAnalyticsResponse("YEARLY", String.valueOf(year), orders);
    }

    public AnalyticsResponse getOverallRevenue() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        return buildAnalyticsResponse("OVERALL", "ALL_TIME", orders);
    }

    public AdminDashboardStatsDTO getDashboardStats() {
        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();

        List<Order> allOrders = orderRepository.findAll();
        BigDecimal overallRevenue = allOrders.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID || o.getStatus() == OrderStatus.SUCCESS || o.getStatus() == OrderStatus.PENDING)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminDashboardStatsDTO.builder()
                .totalProducts(totalProducts)
                .totalUsers(totalUsers)
                .totalOrders(totalOrders)
                .overallRevenue(overallRevenue.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private AnalyticsResponse buildAnalyticsResponse(String period, String filterValue, List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return AnalyticsResponse.builder()
                    .period(period)
                    .filterValue(filterValue)
                    .totalRevenue(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .totalOrders(0)
                    .transactions(new ArrayList<>())
                    .build();
        }

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderDTO> dtos = orders.stream()
                .map(this::mapOrderToDTO)
                .collect(Collectors.toList());

        return AnalyticsResponse.builder()
                .period(period)
                .filterValue(filterValue)
                .totalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP))
                .totalOrders(dtos.size())
                .transactions(dtos)
                .build();
    }

    private UserDTO mapUserToDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private OrderDTO mapOrderToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = new ArrayList<>();
        if (order.getItems() != null) {
            itemDTOs = order.getItems().stream().map(item -> {
                String imgUrl = null;
                Product p = item.getProduct();
                if (p != null && p.getImages() != null && !p.getImages().isEmpty()) {
                    imgUrl = p.getImages().get(0).getImageUrl();
                }
                return OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(p != null ? p.getProductId() : null)
                        .productName(p != null ? p.getName() : "Product")
                        .imageUrl(imgUrl)
                        .quantity(item.getQuantity())
                        .pricePerUnit(item.getPricePerUnit())
                        .totalPrice(item.getTotalPrice())
                        .build();
            }).collect(Collectors.toList());
        }

        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser() != null ? order.getUser().getUserId() : null)
                .userFullName(order.getUser() != null ? order.getUser().getFullName() : "Customer")
                .userEmail(order.getUser() != null ? order.getUser().getEmail() : "")
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .shippingAddress(order.getShippingAddress())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        return orders.stream().map(this::mapOrderToDTO).collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO markOrderAsPaid(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setPaymentStatus(PaymentStatus.PAID);
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.SUCCESS);
        }
        Order saved = orderRepository.save(order);
        return mapOrderToDTO(saved);
    }
}
