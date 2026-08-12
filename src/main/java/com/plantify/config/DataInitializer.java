package com.plantify.config;

import com.plantify.entity.*;
import com.plantify.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final JwtTokenRepository jwtTokenRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeUsers();
        initializeCategoriesAndProducts();
        updateCategoriesSequenceAndImages();
        updateFeaturedPopularProducts();
        updateProductDetails();
    }

    private void initializeUsers() {
        userRepository.findByEmail("admin@plantify.com").ifPresentOrElse(
            u -> {
                u.setPassword(passwordEncoder.encode("Admin@123"));
                u.setRole(Role.ADMIN);
                userRepository.save(u);
            },
            () -> {
                User adminPlantify = User.builder()
                        .fullName("Plantify System Admin")
                        .username("admin_plantify")
                        .email("admin@plantify.com")
                        .mobileNumber("9876543210")
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(adminPlantify);
            }
        );

        if (!userRepository.existsByEmail("admin@gmail.com")) {
            String adminUsername = userRepository.existsByUsername("admin") ? "admin_gmail" : "admin";
            User adminGmail = User.builder()
                    .fullName("System Administrator")
                    .username(adminUsername)
                    .email("admin@gmail.com")
                    .mobileNumber("9876543211")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(adminGmail);
        }

        if (!userRepository.existsByEmail("customer@plantify.com")) {
            User customer = User.builder()
                    .fullName("Flora Gardener")
                    .username("flora")
                    .email("customer@plantify.com")
                    .mobileNumber("9123456789")
                    .password(passwordEncoder.encode("Customer@123"))
                    .role(Role.CUSTOMER)
                    .build();
            userRepository.save(customer);
        }
    }

    private void initializeCategoriesAndProducts() {
        List<String> categoryNames = Arrays.asList(
                "Plants", "Pots", "Soils", "Fertilisers", "Seeds",
                "Garden Tools", "Watering Solutions", "Pest Control", "Gardening Decor"
        );

        for (String catName : categoryNames) {
            if (!categoryRepository.existsByCategoryNameIgnoreCase(catName)) {
                categoryRepository.save(Category.builder().categoryName(catName).build());
            }
        }

        if (productRepository.count() == 0) {
            Category plants = categoryRepository.findByCategoryNameIgnoreCase("Plants").orElseThrow();
            Category pots = categoryRepository.findByCategoryNameIgnoreCase("Pots").orElseThrow();
            Category soils = categoryRepository.findByCategoryNameIgnoreCase("Soils").orElseThrow();
            Category fertilisers = categoryRepository.findByCategoryNameIgnoreCase("Fertilisers").orElseThrow();
            Category seeds = categoryRepository.findByCategoryNameIgnoreCase("Seeds").orElseThrow();
            Category tools = categoryRepository.findByCategoryNameIgnoreCase("Garden Tools").orElseThrow();
            Category watering = categoryRepository.findByCategoryNameIgnoreCase("Watering Solutions").orElseThrow();
            Category pest = categoryRepository.findByCategoryNameIgnoreCase("Pest Control").orElseThrow();
            Category decor = categoryRepository.findByCategoryNameIgnoreCase("Gardening Decor").orElseThrow();

            createProduct("Swiss Cheese Monstera Deliciosa", "Features broad, glossy emerald leaves with iconic natural split cuts (fenestrations). A striking tropical statement plant that purifies indoor air and brings lush rainforest vibes to your living space.", new BigDecimal("29.99"), 25, plants,
                    "https://i.pinimg.com/736x/11/09/ef/1109ef94c2d4cd27a037dca9a6f8511c.jpg", true);
            createProduct("Ficus Lyrata Fiddle-Leaf Fig Tree", "Architectural indoor tree showcasing bold, fiddle-shaped glossy leaves. Perfect as a dramatic focal point for bright living rooms, sunlit corners, and modern home offices.", new BigDecimal("44.50"), 15, plants,
                    "https://i.pinimg.com/1200x/23/55/68/2355683d9c41359853fa663facc3d3a3.jpg", true);
            createProduct("Variegated Snake Plant (Sansevieria Laurentii)", "Upright sword-like leaves edged with vibrant golden-yellow borders. Virtually indestructible and famous for nighttime oxygen release, making it the ultimate low-maintenance bedroom plant.", new BigDecimal("19.99"), 40, plants,
                    "https://i.pinimg.com/736x/ce/c0/84/cec0847ee6d867c8a419cef6d2d5e399.jpg", true);
            createProduct("White Bloom Peace Lily (Spathiphyllum)", "Deep green lance-shaped leaves paired with delicate snow-white blooms. Communicates when it needs water by gently drooping, and excels at filtering indoor environmental toxins.", new BigDecimal("24.00"), 20, plants,
                    "https://i.pinimg.com/736x/6d/ca/f3/6dcaf37868a7f5116bc6cb15853a0a15.jpg", true);

            createProduct("Hand-Crafted Terracotta Planter with Drainage Saucer", "Warm earthy clay planter crafted with porous terracotta walls for optimal root aeration. Includes a matching catch tray to protect tabletops and hardwood floors.", new BigDecimal("18.50"), 30, pots,
                    "https://i.pinimg.com/736x/b8/5b/f4/b85bf4138310a3ea1466b3af220a92bf.jpg", true);
            createProduct("Nordic Matte White Ceramic Cylinder Pot", "Sleek Scandinavian design featuring a smooth matte glaze with subtle granite speckles. Complements succulents, pothos, and desktop greenery with clean minimalist elegance.", new BigDecimal("22.00"), 25, pots,
                    "https://i.pinimg.com/736x/ac/92/72/ac9272eca591216edad477f345ba7c2b.jpg", true);

            createProduct("Nutrient-Enriched Organic Indoor Potting Soil (10L)", "Lightweight blend of coarse perlite, coco coir, worm castings, and aged compost. Prevents root rot, retains moisture evenly, and feeds house plants for up to 6 months.", new BigDecimal("14.99"), 50, soils,
                    "https://i.pinimg.com/1200x/54/f5/a1/54f5a15c17c7c4d3a1766c6e1cd8925a.jpg", true);

            createProduct("Bio-Active Liquid Bio-Nutrient Elixir (500ml)", "Concentrated 10-10-10 NPK liquid formula enriched with seaweed extract and micro-minerals. Promotes lush leaf expansion, strong root systems, and vibrant seasonal blooms.", new BigDecimal("12.50"), 45, fertilisers,
                    "https://i.pinimg.com/736x/bc/a0/7d/bca07d5a78017873133f9d6959e09cab.jpg", true);

            createProduct("Non-GMO Kitchen Herb & Culinary Seed Collection", "Premium collection of 12 non-hybrid heirloom seeds featuring Sweet Basil, Italian Parsley, Thyme, Cilantro, and Mint. High germination rates for windowsill and garden beds.", new BigDecimal("16.00"), 60, seeds,
                    "https://i.pinimg.com/1200x/d5/5d/86/d55d86f9605ece8573ee3d79b9e44e4a.jpg", true);

            createProduct("Heavy-Duty Ashwood & Stainless Steel Garden Tool Trio", "Forged stainless steel hand trowel, 3-prong cultivator, and transplanting spade with ergonomic smooth ashwood handles. Built for effortless soil turning, weeding, and planting.", new BigDecimal("27.99"), 20, tools,
                    "https://i.pinimg.com/1200x/5c/80/b5/5c80b58eaf4f17937799cf7a76213e97.jpg", true);

            createProduct("Cold-Pressed Pure Organic Neem Oil Spray (750ml)", "100% natural botanical pest deterrent spray. Safely eliminates mealybugs, aphids, spider mites, and powdery mildew while leaving leaves shiny and protected.", new BigDecimal("13.99"), 35, pest,
                    "https://images.unsplash.com/photo-1617576683096-00fc8eecb3af?auto=format&fit=crop&w=800&q=80", false);
            createProduct("Boho Hand-Woven Cotton Macrame Plant Hangers (Set of 3)", "Intricately knotted natural cotton rope hangers with durable wooden rings. Elevates trailing ivy, pothos, and ferns into beautiful vertical window gardens.", new BigDecimal("19.50"), 25, decor,
                    "https://images.unsplash.com/photo-1520412099551-62b6bafeb5bb?auto=format&fit=crop&w=800&q=80", false);
            createProduct("Vintage Brushed Copper Long-Spout Watering Can (1.5L)", "Rust-resistant brushed copper vessel with a narrow precision spout. Delivers smooth, splash-free hydration directly to dense foliage and indoor plant roots.", new BigDecimal("32.00"), 18, watering,
                    "https://images.unsplash.com/photo-1515150144380-bca9f1650ed9?auto=format&fit=crop&w=800&q=80", false);
        }
    }

    private void updateCategoriesSequenceAndImages() {
        Object[][] spec = {
            {"Plants", 1, "https://i.pinimg.com/1200x/d1/98/3f/d1983fa2068757008462591bbdac9bf5.jpg"},
            {"Pots", 2, "https://i.pinimg.com/736x/36/41/7d/36417d0b44eba4f3bc40a0f19d9bdc75.jpg"},
            {"Soils", 3, "https://i.pinimg.com/736x/c9/ee/58/c9ee582eea5e0744c5705e7966f04e46.jpg"},
            {"Fertilisers", 4, "https://i.pinimg.com/736x/f6/c9/42/f6c942c5212d2983f8e972023f6d35e5.jpg"},
            {"Seeds", 5, "https://i.pinimg.com/736x/fd/05/39/fd05397b789e4e70745cd160a878b38c.jpg"},
            {"Garden Tools", 6, "https://wonderlandgarden.in/cdn/shop/files/IMG_7545_512x342.jpg?v=1742905890"},
            {"Watering Solutions", 7, "https://i.pinimg.com/1200x/96/7f/06/967f06768d5422b6c16f5f56210c9a3b.jpg"},
            {"Pest Control", 8, "https://i.pinimg.com/736x/7c/22/27/7c222713f692167ed55cb05677bfa338.jpg"},
            {"Gardening Decor", 9, "https://i.pinimg.com/1200x/df/1c/7b/df1c7bd95f5bad19080986f75e411f58.jpg"}
        };

        for (Object[] item : spec) {
            String name = (String) item[0];
            Integer order = (Integer) item[1];
            String url = (String) item[2];

            Category cat = categoryRepository.findByCategoryNameIgnoreCase(name)
                    .orElseGet(() -> Category.builder().categoryName(name).build());
            cat.setDisplayOrder(order);
            cat.setCategoryImageUrl(url);
            categoryRepository.save(cat);
        }
        log.info("Successfully updated all 9 categories with display_order and new image URLs.");
    }

    private void updateFeaturedPopularProducts() {
        String[] featuredImages = {
            "https://i.pinimg.com/736x/47/f9/b8/47f9b8c79d15c8d79c5ed25330c92904.jpg",
            "https://i.pinimg.com/736x/24/76/2b/24762b2d78e056c58a251f84a1270cfc.jpg",
            "https://i.pinimg.com/736x/91/ab/b1/91abb17f52d1ac47cb428ced70c10ecb.jpg",
            "https://i.pinimg.com/1200x/f4/ff/bb/f4ffbb6643d7ad0ad4d44fe0b41348cb.jpg",
            "https://i.pinimg.com/736x/ad/ed/50/aded505ca6ac7fb82db1280e1aea9211.jpg",
            "https://i.pinimg.com/736x/8e/9d/df/8e9ddf831edfc2d9d90567036f1db0d9.jpg",
            "https://i.pinimg.com/736x/e8/f8/d2/e8f8d2ac4f6c7ea84ab060436817fde4.jpg",
            "https://i.pinimg.com/1200x/1d/1c/53/1d1c539a5d3aea7bf641a0c3718944fb.jpg",
            "https://i.pinimg.com/736x/a2/b1/44/a2b1441cfb08466332842567ebfe3fb7.jpg",
            "https://i.pinimg.com/1200x/f4/58/81/f458814d8ba70d5b1ccbeab5a4320e56.jpg"
        };

        // Reset is_featured to false for all products first
        List<Product> allProducts = productRepository.findAll();
        for (Product p : allProducts) {
            boolean shouldBeFeatured = p.getProductId() != null && p.getProductId() >= 1L && p.getProductId() <= 10L;
            p.setIsFeatured(shouldBeFeatured);

            if (shouldBeFeatured) {
                int idx = (int) (p.getProductId() - 1);
                String targetUrl = featuredImages[idx];

                if (p.getImages() == null) {
                    p.setImages(new ArrayList<>());
                }

                if (p.getImages().isEmpty()) {
                    ProductImage img = ProductImage.builder().product(p).imageUrl(targetUrl).build();
                    p.getImages().add(img);
                } else {
                    p.getImages().get(0).setImageUrl(targetUrl);
                }
            }
            productRepository.save(p);
        }
        log.info("Successfully updated products 1..10 as featured with target Pinterest images.");
    }

    private void createProduct(String name, String description, BigDecimal price, int stock, Category category, String imageUrl, boolean isFeatured) {
        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .category(category)
                .isFeatured(isFeatured)
                .build();

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .build();

        product.setImages(List.of(image));
        productRepository.save(product);
    }

    private void updateProductDetails() {
        // 1. Plants (IDs 1-19)
        updateSingleProduct(1L, "Monstera Deliciosa", "Iconic tropical plant with broad split leaves.");
        updateSingleProduct(2L, "Fiddle-Leaf Fig", "Elegant indoor tree with glossy violin leaves.");
        updateSingleProduct(3L, "Snake Plant", "Low-maintenance plant that purifies indoor air.");
        updateSingleProduct(4L, "Peace Lily", "Lush green plant producing elegant white flowers.");
        updateSingleProduct(5L, "Golden Pothos", "Fast-growing trailing vine for bright rooms.");
        updateSingleProduct(6L, "ZZ Plant", "Resilient indoor plant with shiny dark leaves.");
        updateSingleProduct(7L, "Rubber Tree", "Bold indoor plant with deep burgundy foliage.");
        updateSingleProduct(8L, "Calathea Orbifolia", "Stunning houseplant with striped round leaves.");
        updateSingleProduct(9L, "Aloe Vera", "Healing succulent plant for bright sunny spots.");
        updateSingleProduct(10L, "String of Pearls", "Cascading succulent with spherical bead-like leaves.");
        updateSingleProduct(11L, "Boston Fern", "Feathery green fronds for humid indoor spaces.");
        updateSingleProduct(12L, "Bird of Paradise", "Grand tropical plant with broad leafy fronds.");
        updateSingleProduct(13L, "Jade Plant", "Classic woody succulent that brings good luck.");
        updateSingleProduct(14L, "Majesty Palm", "Graceful indoor palm tree for living rooms.");
        updateSingleProduct(15L, "Philodendron Brasil", "Trailing heartleaf plant with yellow variegated stripes.");
        updateSingleProduct(16L, "Pink Anthurium", "Blooms vibrant pink flowers throughout the year.");
        updateSingleProduct(17L, "Spider Plant", "Air-purifying plant producing cute baby offsets.");
        updateSingleProduct(18L, "Chinese Money Plant", "Charming houseplant with round pancake leaves.");
        updateSingleProduct(19L, "Red Aglaonema", "Vibrant plant featuring bright pink foliage.");

        // 2. Pots (IDs 20-39)
        updateSingleProduct(20L, "Terracotta Planter", "Hand-crafted clay pot with drainage saucer.");
        updateSingleProduct(21L, "Nordic Ceramic Pot", "Sleek matte white cylinder planter for desks.");
        updateSingleProduct(22L, "Black Ribbed Pot", "Modern ribbed ceramic planter with matte finish.");
        updateSingleProduct(23L, "Concrete Hexagon Pot", "Industrial geometric pot for mini succulents.");
        updateSingleProduct(24L, "Seagrass Basket", "Hand-woven plant basket with waterproof liner.");
        updateSingleProduct(25L, "Self-Watering Pot", "Includes water reservoir to keep plants hydrated.");
        updateSingleProduct(26L, "Brass Metal Pot", "Polished brass planter with vintage filigree.");
        updateSingleProduct(27L, "Emerald Bonsai Pot", "Glazed ceramic pot with root drainage holes.");
        updateSingleProduct(28L, "Glass Terrarium", "Hanging geometric glass planter for air plants.");
        updateSingleProduct(29L, "Sandstone Pot", "Textured ceramic vessel with river rock finish.");
        updateSingleProduct(30L, "Pink Footed Pot", "Elevated ceramic planter with three short legs.");
        updateSingleProduct(31L, "Talavera Clay Pot", "Hand-painted Mexican clay planter for patios.");
        updateSingleProduct(32L, "Vertical Garden Pots", "3-tier stackable planter tower for balcony herbs.");
        updateSingleProduct(33L, "Weathered Stone Urn", "Classic stone urn planter with antique patina.");
        updateSingleProduct(34L, "Charcoal Square Pot", "Lightweight durable planter for indoor outdoors.");
        updateSingleProduct(35L, "Marble Gold Pot", "White marble glazed ceramic pot with gold rim.");
        updateSingleProduct(36L, "Fabric Grow Bag", "Breathable fabric pot promoting healthy root growth.");
        updateSingleProduct(37L, "Mini Succulent Pots", "Set of 4 mini colorful pots with catch trays.");
        updateSingleProduct(38L, "Mid-Century Pot", "Ceramic cylinder pot on solid walnut tripod stand.");
        updateSingleProduct(39L, "Ocean Teal Pot", "Reactive teal glaze ceramic pot for foliage.");

        // 3. Garden Tools (IDs 40-63)
        updateSingleProduct(40L, "Bent Scraper Tool", "Heavy-duty scraper for cleaning garden beds.");
        updateSingleProduct(41L, "Straight Blade Cutter", "D-handle cutter blade for trimming roots.");
        updateSingleProduct(42L, "Single Prong Weeder", "PVC handle weeder for pulling stubborn roots.");
        updateSingleProduct(43L, "Hand Garden Trowel", "Durable metal trowel for digging and planting.");
        updateSingleProduct(44L, "Single-Edge Sickle", "Sharp steel sickle for cutting tough grass.");
        updateSingleProduct(45L, "Bypass Pruner Shears", "Precision handheld shears for pruning branches.");
        updateSingleProduct(46L, "Single Prong Weeder", "Comfortable hand tool for removing weeds.");
        updateSingleProduct(47L, "Premium Gardening Kit", "Essential set of hand tools for indoor gardening.");
        updateSingleProduct(48L, "Transplanter Tool", "Narrow trowel with depth markings for seedlings.");
        updateSingleProduct(49L, "Hedge Shear 10-Inch", "Long wooden handle shears for trimming hedges.");
        updateSingleProduct(50L, "Double-Edge Sickle", "Dual-edge steel tool for clearing brush.");
        updateSingleProduct(51L, "Push Reel Mower", "Eco-friendly manual lawn mower for neat grass.");
        updateSingleProduct(52L, "Gardening Gloves Pair", "Protective gloves for handling thorny plants.");
        updateSingleProduct(53L, "Anvil Pruner Shears", "Heavy-duty shears for cutting thick dry stems.");
        updateSingleProduct(54L, "Folding Pruning Saw", "Compact folding hand saw for tree branches.");
        updateSingleProduct(55L, "Mini Garden Tool Set", "Handy 2-piece trowel and cultivator combo.");
        updateSingleProduct(56L, "Mini Bent Scraper", "Compact 1-inch scraper tool for precision work.");
        updateSingleProduct(57L, "Indoor Garden Set", "Complete indoor planting kit for house plants.");
        updateSingleProduct(58L, "Bamboo Support Sticks", "Pack of 3-foot wood stakes for supporting plants.");
        updateSingleProduct(59L, "Handy Pruning Clipper", "Lightweight hand clipper for deadheading flowers.");
        updateSingleProduct(60L, "3-Tine Cultivator", "Hand rake for loosening and aerating soil.");
        updateSingleProduct(61L, "Electric Lawn Mower", "Powerful mower for maintaining small garden lawns.");
        updateSingleProduct(62L, "Stainless Steel Spade", "Rust-proof spade for digging garden beds.");
        updateSingleProduct(63L, "Garden Weeding Hoe", "Sturdy garden hoe for breaking hard soil.");

        // 4. Gardening Decor (IDs 64-78)
        updateSingleProduct(64L, "Cotton Double Swing", "Comfortable double hammock swing for garden relaxation.");
        updateSingleProduct(65L, "Garden Gnome Statue", "Charming weather-resistant figurine for lawns.");
        updateSingleProduct(66L, "Wood Support Stakes", "Durable hardwood support stakes for tall stems.");
        updateSingleProduct(67L, "Coloured Pebbles (1kg)", "Decorative bright pebbles for topping flower pots.");
        updateSingleProduct(68L, "White Marble Pebbles", "Smooth white stones for plant pots and pathways.");
        updateSingleProduct(69L, "Yellow Polished Pebbles", "Glossy yellow stones for garden beds and planters.");
        updateSingleProduct(70L, "Blue Polished Pebbles", "Decorative sea-blue stones for aquariums and pots.");
        updateSingleProduct(71L, "Mayan Cotton Hammock", "Hand-woven breathable cotton hammock for yards.");
        updateSingleProduct(72L, "Ruby Polished Pebbles", "Vibrant red decorative pebbles for pot topping.");
        updateSingleProduct(73L, "Black Polished Pebbles", "Sleek dark stones for modern planter decor.");
        updateSingleProduct(74L, "Cotton Rope Swing", "Handcrafted rope swing chair for outdoor patios.");
        updateSingleProduct(75L, "Solar Garden Light", "Energy-efficient solar stake light for garden paths.");
        updateSingleProduct(76L, "Mixed Polished Pebbles", "Multi-color natural pebbles for planter dressing.");
        updateSingleProduct(77L, "Cotton Cushion Swing", "Plush outdoor hanging chair for relaxation.");
        updateSingleProduct(78L, "Barbecue Barrel Grill", "Compact barrel grill for outdoor garden gatherings.");

        // 5. Watering Solutions (IDs 79-98)
        updateSingleProduct(79L, "Sustee Water Meter (3 Pack)", "Sensing stakes that indicate when plants need water.");
        updateSingleProduct(80L, "Sustee Water Meter (2 Pack)", "Color-changing moisture indicators for houseplants.");
        updateSingleProduct(81L, "Sustee Water Meter Probe", "Simple moisture sensor stake for indoor pots.");
        updateSingleProduct(82L, "Copper Watering Can", "Classic long-spout metal watering can for plants.");
        updateSingleProduct(83L, "Drip Irrigation Kit", "Automated drip watering system for garden beds.");
        updateSingleProduct(84L, "Plant Mist Sprayer", "Continuous fine mist sprayer for ferns and orchids.");
        updateSingleProduct(85L, "Self-Watering Globes", "Glass bulbs that slowly release water into soil.");
        updateSingleProduct(86L, "Metal Hose Nozzle", "10-pattern spray nozzle with ergonomic grip.");
        updateSingleProduct(87L, "Expandable Garden Hose", "Kink-free 50ft hose for easy garden watering.");
        updateSingleProduct(88L, "Electronic Hose Timer", "Digital water timer for automated sprinklers.");
        updateSingleProduct(89L, "Galvanized Steel Can", "Galvanized metal watering can with shower rose.");
        updateSingleProduct(90L, "Cotton Wick Cord", "Self-watering cord for vacation plant care.");
        updateSingleProduct(91L, "2-Way Hose Splitter", "Heavy-duty brass valve for running two hoses.");
        updateSingleProduct(92L, "Watering Wand 24-Inch", "Long reach wand with soft rain shower spray.");
        updateSingleProduct(93L, "Rain Barrel Collector", "Collapsible tank for collecting rainwater.");
        updateSingleProduct(94L, "Submersible Water Pump", "Quiet pump for fountains and hydroponics.");
        updateSingleProduct(95L, "Tree Watering Bag", "Slow-release drip bag for deep root watering.");
        updateSingleProduct(96L, "Lawn Impact Sprinkler", "Rotating brass sprinkler covering wide lawn areas.");
        updateSingleProduct(97L, "Glass Watering Bulb", "Blown glass watering globe for indoor pots.");
        updateSingleProduct(98L, "Balcony Watering Can", "Compact 1-liter plastic watering can for desks.");

        // 6. Pest Control (IDs 99-115)
        updateSingleProduct(99L, "Bio Insecticide Spray", "Natural microbial spray that controls mealybugs.");
        updateSingleProduct(100L, "Organic Neem Oil Spray", "Botanical pest deterrent for aphids and mites.");
        updateSingleProduct(101L, "Insecticidal Soap Spray", "Natural soap spray for soft-bodied garden pests.");
        updateSingleProduct(102L, "Yellow Sticky Bug Traps", "Adhesive traps for catching gnats and whiteflies.");
        updateSingleProduct(103L, "Copper Fungicide Mist", "Protects plants from leaf spot, blight, and rust.");
        updateSingleProduct(104L, "Diatomaceous Earth Powder", "Natural mineral powder that repels crawling pests.");
        updateSingleProduct(105L, "Caterpillar Control Spray", "Biological spray targeting foliage-eating worms.");
        updateSingleProduct(106L, "Solar Ultrasonic Repeller", "Ultrasonic wave stake that deters garden animals.");
        updateSingleProduct(107L, "Slug & Snail Granules", "Safe iron phosphate pellets that protect beds.");
        updateSingleProduct(108L, "Beneficial Nematodes", "Microscopic organisms that control soil larvae.");
        updateSingleProduct(109L, "Tree Pest Barrier Tape", "Sticky trunk wrap that blocks climbing insects.");
        updateSingleProduct(110L, "Pyrethrin Insect Killer", "Fast-acting natural spray for vegetable crops.");
        updateSingleProduct(111L, "Leaf Shine & Pest Polish", "Restores leaf gloss while repelling dust and mites.");
        updateSingleProduct(112L, "Gnat Barrier Sand", "Quartz mineral dressing that stops breeding gnats.");
        updateSingleProduct(113L, "Garlic Chili Spray", "Natural pungent spray that deters garden pests.");
        updateSingleProduct(114L, "Sulfur Dust Powder", "Multi-purpose fungicide for powdery mildew.");
        updateSingleProduct(115L, "Mosquito Dunks (6 Pack)", "Biological rings that kill mosquito larvae.");

        // 7. Soils (IDs 116-132)
        updateSingleProduct(116L, "Coarse Sandy Soil", "Fast-draining natural sand for cacti and succulents.");
        updateSingleProduct(117L, "Rich Organic Loam", "Nutrient-dense black soil for healthy root growth.");
        updateSingleProduct(118L, "Perlite Potting Mix", "Lightweight aerated soil mix for indoor plants.");
        updateSingleProduct(119L, "Horticultural Vermiculite", "Retains soil moisture and essential minerals.");
        updateSingleProduct(120L, "Organic Vermicompost", "Pure worm castings that naturally feed plants.");
        updateSingleProduct(121L, "All-Purpose Potting Soil", "Balanced nutrient soil for indoor houseplants.");
        updateSingleProduct(122L, "Organic Peat Moss", "Improves water retention and soil structure.");
        updateSingleProduct(123L, "Premium Garden Soil", "Fertile rich soil ideal for flower garden beds.");
        updateSingleProduct(124L, "Organic Veggie Soil", "Specially formulated mix for vegetable plants.");
        updateSingleProduct(125L, "Natural Home Compost", "Decomposed organic matter that enriches beds.");
        updateSingleProduct(126L, "Natural Clay Soil", "Dense mineral soil ideal for heavy plantings.");
        updateSingleProduct(127L, "Nutrient Red Soil", "Iron-rich porous soil for potted garden plants.");
        updateSingleProduct(128L, "Aged Farmyard Manure", "Traditional organic fertilizer for green foliage.");
        updateSingleProduct(129L, "Organic Coco Coir Block", "Eco-friendly coconut coir block that expands.");
        updateSingleProduct(130L, "Aged Cow Manure", "Natural soil amendment for flowers and veggies.");
        updateSingleProduct(131L, "Bonsai Soil Mix", "Fast-draining volcanic grit blend for bonsai.");
        updateSingleProduct(132L, "Mineral Black Soil", "Moisture-retentive dark soil for lush gardens.");

        // 8. Fertilisers (IDs 133-154)
        updateSingleProduct(133L, "NPK 19-19-19 Fertilizer", "Balanced water-soluble fertilizer for foliage.");
        updateSingleProduct(134L, "Goat Manure Fertilizer", "Organic slow-release manure for soil enrichment.");
        updateSingleProduct(135L, "Groundnut Cake Meal", "Protein-rich organic cake fertilizer for blooms.");
        updateSingleProduct(136L, "Sheep Manure Fertilizer", "Natural soil booster rich in organic matter.");
        updateSingleProduct(137L, "Urea Fertilizer Granules", "High-nitrogen granules for fast leaf growth.");
        updateSingleProduct(138L, "Organic Vermicompost", "Odorless worm castings packed with microbes.");
        updateSingleProduct(139L, "Fulvic Acid Liquid", "Boosts nutrient absorption and root vitality.");
        updateSingleProduct(140L, "DAP Plant Fertilizer", "Phosphorus-rich fertilizer for strong roots.");
        updateSingleProduct(141L, "Earthworm Compost", "Bio-rich compost that improves soil health.");
        updateSingleProduct(142L, "Farm Yard Manure", "Traditional organic fertilizer for garden beds.");
        updateSingleProduct(143L, "MOP Potash Fertilizer", "Potassium fertilizer for bloom and fruit strength.");
        updateSingleProduct(144L, "Poultry Manure", "Nutrient-dense manure for vegetables and crops.");
        updateSingleProduct(145L, "Organic Plant Compost", "All-natural compost for feeding indoor plants.");
        updateSingleProduct(146L, "Mustard Cake Powder", "Traditional bio-fertilizer for healthy roots.");
        updateSingleProduct(147L, "Liquid Organic Food", "Gentle liquid plant food for daily watering.");
        updateSingleProduct(148L, "Liquid Seaweed Kelp", "Kelp extract that boosts plant stress tolerance.");
        updateSingleProduct(149L, "Humic Acid Concentrate", "Improves soil structure and nutrient uptake.");
        updateSingleProduct(150L, "Cow Dung Compost", "Aged organic manure for indoor and outdoor beds.");
        updateSingleProduct(151L, "Calcium Nitrate", "Provides calcium to prevent leaf and fruit rot.");
        updateSingleProduct(152L, "Castor Cake Meal", "Organic plant food that deters soil pests.");
        updateSingleProduct(153L, "Ammonium Sulphate", "Nitrogen and sulfur supplement for acid plants.");
        updateSingleProduct(154L, "Boron Micronutrient", "Essential mineral supplement for flowering.");

        // 9. Seeds (IDs 155-181)
        updateSingleProduct(155L, "Marigold Flower Seeds", "Bright yellow flower seeds that repel garden pests.");
        updateSingleProduct(156L, "Sweet Watermelon Seeds", "Yields juicy sweet red watermelons in summer.");
        updateSingleProduct(157L, "Okra Lady Finger Seeds", "High-yielding tender green okra vegetable seeds.");
        updateSingleProduct(158L, "Jasmine Flower Seeds", "Fragrant white climbing flower seeds for arbors.");
        updateSingleProduct(159L, "Holy Tulsi Basil Seeds", "Aromatic medicinal herb seeds for windowsill pots.");
        updateSingleProduct(160L, "Crisp Cucumber Seeds", "Fast-growing seeds producing crisp green cucumbers.");
        updateSingleProduct(161L, "Mini Watermelon Seeds", "Compact vine seeds yielding sweet personal melons.");
        updateSingleProduct(162L, "Pumpkin Seeds Pack", "Heirloom seeds producing large orange pumpkins.");
        updateSingleProduct(163L, "Green Coriander Seeds", "Fresh cilantro herb seeds for kitchen herb gardens.");
        updateSingleProduct(164L, "Sacred Lotus Seeds", "Aquatic plant seeds producing elegant pink lotus blooms.");
        updateSingleProduct(165L, "Fresh Spinach Seeds", "Nutrient-rich leafy green seeds ready in 30 days.");
        updateSingleProduct(166L, "Bottle Gourd Seeds", "Climbing vine seeds producing tender bottle gourds.");
        updateSingleProduct(167L, "Muskmelon Seeds", "Sweet cantaloupe seeds yielding fragrant orange melons.");
        updateSingleProduct(168L, "Cosmos Flower Seeds", "Colorful annual wildflower seeds for sunny gardens.");
        updateSingleProduct(169L, "Cilantro Herb Seeds", "Aromatic coriander seeds for cooking and garnishes.");
        updateSingleProduct(170L, "Chia Superfood Seeds", "Nutritious chia seeds for microgreens and garden.");
        updateSingleProduct(171L, "Ajwain Carom Seeds", "Medicinal culinary herb seeds for pot gardens.");
        updateSingleProduct(172L, "Indian Gooseberry Amla", "Vitamin C rich tree seeds for sunny outdoor beds.");
        updateSingleProduct(173L, "Star Jasmine Seeds", "Sweet-scented white vine flower seeds for trellises.");
        updateSingleProduct(174L, "Fennel Herb Seeds", "Aromatic licorice-flavored herb seeds for beds.");
        updateSingleProduct(175L, "Orange Marigold Seeds", "Vibrant orange flower seeds for garden borders.");
        updateSingleProduct(176L, "Golden Papaya Seeds", "Fast-growing tropical seeds for sweet fruits.");
        updateSingleProduct(177L, "Beefsteak Tomato Seeds", "Meaty red tomato seeds for slicing and salads.");
        updateSingleProduct(178L, "Green Bush Bean Seeds", "High-yielding tender stringless pole bean seeds.");
        updateSingleProduct(179L, "Moringa Drumstick Seeds", "Fast-growing superfood tree seeds for pods.");
        updateSingleProduct(180L, "Honey Muskmelon Seeds", "Sweet orange-fleshed cantaloupe melon seeds.");
        updateSingleProduct(181L, "Brown Flax Superfood Seeds", "Omega-3 rich flax seeds for home microgreen growing.");

        log.info("Successfully updated all 181 products in database with concise names and single sentence descriptions.");
    }

    private void updateSingleProduct(Long productId, String name, String description) {
        productRepository.findById(productId).ifPresent(p -> {
            p.setName(name);
            p.setDescription(description);
            productRepository.save(p);
        });
    }
}
