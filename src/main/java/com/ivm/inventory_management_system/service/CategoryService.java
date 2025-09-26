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

    public Category createCategory(Category category) {
        return categoryRepository.save( category);
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    public void updateCategory(Category updatedcategory) {
        categoryRepository.save(updatedcategory);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }


}
