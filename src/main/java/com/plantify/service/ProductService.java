package com.plantify.service;

import com.plantify.dto.ProductDTO;
import com.plantify.entity.Category;
import com.plantify.entity.Product;
import com.plantify.entity.ProductImage;
import com.plantify.exception.ResourceNotFoundException;
import com.plantify.repository.CategoryRepository;
import com.plantify.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<ProductDTO> getProducts(Long categoryId, String query, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.filterProducts(categoryId, query, pageable).map(this::mapToDTO);
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToDTO(product);
    }

    public List<ProductDTO> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueOrderByProductIdAsc()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(category)
                .rating(dto.getRating() != null ? dto.getRating() : 4.5)
                .reviewCount(dto.getReviewCount() != null ? dto.getReviewCount() : 42)
                .build();

        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            List<ProductImage> images = dto.getImageUrls().stream()
                    .map(url -> ProductImage.builder().product(product).imageUrl(url).build())
                    .collect(Collectors.toList());
            product.setImages(images);
        }

        return mapToDTO(productRepository.save(product));
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(category);
        if (dto.getRating() != null) product.setRating(dto.getRating());
        if (dto.getReviewCount() != null) product.setReviewCount(dto.getReviewCount());

        if (dto.getImageUrls() != null) {
            product.getImages().clear();
            for (String url : dto.getImageUrls()) {
                product.getImages().add(ProductImage.builder().product(product).imageUrl(url).build());
            }
        }

        return mapToDTO(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepository.delete(product);
    }

    private static final java.util.Map<String, String[]> CATEGORY_POOLS = java.util.Map.of(
        "Plants", new String[]{
            "https://i.pinimg.com/736x/8d/61/8f/8d618f504cf10a8d795b8d2348508e67.jpg",
            "https://i.pinimg.com/1200x/68/df/31/68df311e959ecb00efdd61a298bf246b.jpg",
            "https://i.pinimg.com/736x/11/4a/1c/114a1c5d01ec15c898b96f5b9d3e5dfd.jpg",
            "https://i.pinimg.com/736x/32/3a/ae/323aae8ee8fb559779dfcf1e75a6c382.jpg",
            "https://i.pinimg.com/1200x/f8/f4/22/f8f422b4bf37f9eaecab7fef24cb11ef.jpg",
            "https://i.pinimg.com/736x/77/80/7e/77807ed7a3bd095368a49c6d3bc0a693.jpg",
            "https://i.pinimg.com/736x/07/04/b5/0704b509ef490bf7c5ca72f44ce2f56f.jpg",
            "https://i.pinimg.com/736x/ec/eb/21/eceb21a8eb0e2aa5ff5ee02ed50ff7c7.jpg",
            "https://i.pinimg.com/736x/cb/aa/e4/cbaae4c0ebacb64e03f0b2f5d9620023.jpg",
            "https://i.pinimg.com/736x/2a/fa/d3/2afad37e954acac5d2cfbaefcc9bb535.jpg"
        },
        "Pots", new String[]{
            "https://ik.imagekit.io/stringstackShakthi/New%20Folder/Screenshot%202026-0804%20145436.png",
            "https://ik.imagekit.io/stringstackShakthi/New%20Folder/Screenshot%202026-0804%20145503.png",
            "https://ik.imagekit.io/stringstackShakthi/New%20Folder/Screenshot%202026-0804%20145519.png",
            "https://ik.imagekit.io/stringstackShakthi/New%20Folder/Screenshot%202026-0804%20145355.png",
            "https://ik.imagekit.io/stringstackShakthi/New%20Folder/Screenshot%202026-0804%20145257.png",
            "https://ik.imagekit.io/stringstackShakthi/New%20Folder/Screenshot%202026-0804%20145334.png",
            "https://ik.imagekit.io/stringstackShakthi/New%20Folder/Screenshot%202026-0804%20145244.png",
            "https://ik.imagekit.io/stringstackShakthi/New%20Folder/Screenshot%202026-0804%20145308.png",
            "https://ik.imagekit.io/stringstackShakthi/New%20Folder/Screenshot%202026-0804%20145344.png",
            "https://ik.imagekit.io/stringstackShakthi/New%20Folder/Screenshot%202026-0804%20145318.png"
        },
        "Watering Solutions", new String[]{
            "https://i.pinimg.com/736x/6b/27/ae/6b27ae5c5f796b990064c73591baba2e.jpg",
            "https://i.pinimg.com/1200x/4a/40/8f/4a408f17aa881b15774b5fd132ac5dcc.jpg",
            "https://i.pinimg.com/736x/c2/69/7b/c2697b07996d75c3ce0c7ffe5c35c16e.jpg",
            "https://i.pinimg.com/736x/0b/ec/af/0becaf4fcae436b68735d4c4df8041a7.jpg",
            "https://i.pinimg.com/736x/0f/49/57/0f49573ec6fd7b9c90dfae7d85ffb97a.jpg",
            "https://i.pinimg.com/1200x/55/ae/f1/55aef1e05443c6144cf6e962b6ddf9b6.jpg",
            "https://i.pinimg.com/736x/6e/18/35/6e183586b11b367a8221dac9fa6f3c02.jpg",
            "https://i.pinimg.com/736x/82/95/ce/8295cecb86c9239d31eb1e813799f011.jpg",
            "https://i.pinimg.com/1200x/a7/a0/0b/a7a00b86a3b2b80ea970f90e54d8b9ec.jpg",
            "https://i.pinimg.com/736x/bd/86/e1/bd86e118939c36fb526dc822d057a66b.jpg",
            "https://i.pinimg.com/736x/95/9b/6c/959b6cddb32df510ec0ceab3f4a0a4ff.jpg"
        }
    );

    public static String getFallbackUrl(String categoryName, Long productId) {
        return getCategoryFallbackImage(categoryName, productId);
    }

    private static String getCategoryFallbackImage(String categoryName, Long productId) {
        String[] pool = CATEGORY_POOLS.get(categoryName);
        if (pool != null && pool.length > 0) {
            int index = (int) Math.abs(productId % pool.length);
            return pool[index];
        }
        return "https://images.unsplash.com/photo-1545241047-6083a3684587?auto=format&fit=crop&w=800&q=80";
    }

    public ProductDTO mapToDTO(Product product) {
        List<String> imageUrls = product.getImages() != null && !product.getImages().isEmpty()
                ? product.getImages().stream().map(ProductImage::getImageUrl).collect(Collectors.toList())
                : List.of(getCategoryFallbackImage(
                      product.getCategory() != null ? product.getCategory().getCategoryName() : "General",
                      product.getProductId() != null ? product.getProductId() : 0L));

        return ProductDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .isFeatured(product.getIsFeatured())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                .imageUrls(imageUrls)
                .build();
    }
}
