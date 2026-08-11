package com.plantify.repository;

import com.plantify.entity.Category;
import com.plantify.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
    Page<Product> findByCategory(Category category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "(:categoryId IS NULL OR p.category.categoryId = :categoryId) AND " +
           "(:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> filterProducts(@Param("categoryId") Long categoryId, 
                                @Param("query") String query, 
                                Pageable pageable);

    List<Product> findByIsFeaturedTrueOrderByProductIdAsc();

    Boolean existsByNameIgnoreCaseAndCategory_CategoryId(String name, Long categoryId);

    Boolean existsByNameIgnoreCaseAndCategory_CategoryIdAndProductIdNot(String name, Long categoryId, Long productId);
}
