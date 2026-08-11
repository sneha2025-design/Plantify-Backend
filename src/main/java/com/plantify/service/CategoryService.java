package com.plantify.service;

import com.plantify.dto.CategoryDTO;
import com.plantify.entity.Category;
import com.plantify.exception.BadRequestException;
import com.plantify.exception.ResourceNotFoundException;
import com.plantify.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return mapToDTO(category);
    }

    public CategoryDTO createCategory(CategoryDTO dto) {
        if (categoryRepository.existsByCategoryNameIgnoreCase(dto.getCategoryName())) {
            throw new BadRequestException("Category already exists with name: " + dto.getCategoryName());
        }
        Category category = Category.builder()
                .categoryName(dto.getCategoryName())
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 99)
                .categoryImageUrl(dto.getCategoryImageUrl())
                .build();
        return mapToDTO(categoryRepository.save(category));
    }

    private CategoryDTO mapToDTO(Category category) {
        return CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .displayOrder(category.getDisplayOrder())
                .categoryImageUrl(category.getCategoryImageUrl())
                .build();
    }
}
