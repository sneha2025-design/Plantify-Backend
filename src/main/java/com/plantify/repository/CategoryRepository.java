package com.plantify.repository;

import com.plantify.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);
    Boolean existsByCategoryNameIgnoreCase(String categoryName);
    java.util.List<Category> findAllByOrderByDisplayOrderAsc();
}
