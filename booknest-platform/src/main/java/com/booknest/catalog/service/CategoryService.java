package com.booknest.catalog.service;

import com.booknest.catalog.entity.Category;
import com.booknest.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // CREATE
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category '" + category.getName() + "' already exists!");
        }
        return categoryRepository.save(category);
    }

    // READ ALL
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // UPDATE
    public Category updateCategory(Long id, Category updatedData) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
        
        category.setName(updatedData.getName());
        category.setDescription(updatedData.getDescription());
        
        return categoryRepository.save(category);
    }

    // DELETE
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete. Category not found.");
        }
        categoryRepository.deleteById(id);
    }
}