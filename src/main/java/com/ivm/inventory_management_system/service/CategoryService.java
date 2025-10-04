package com.ivm.inventory_management_system.service;

import com.ivm.inventory_management_system.entity.Category;
import com.ivm.inventory_management_system.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    public Category createCategory(String name, boolean isCustom) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setName(name);
                    cat.setIsCustom(isCustom);
                    return categoryRepository.save(cat);
                });
    }

    public void updateCategory(Category updatedCategory) {
        categoryRepository.save(updatedCategory);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
