package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.entity.Category;
import com.ivm.inventory_management_system.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/owner/{ownerId}/create")
    public Category createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return categoryService.getCategoryById(id)
                .map(existingCategory -> {
                    existingCategory.setName(category.getName());
                    categoryService.updateCategory(existingCategory);
                    return ResponseEntity.ok("Category with ID " + id + " got updated to " + existingCategory.getName());
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(c -> {
                categoryService.deleteCategory(id);
                return ResponseEntity.ok("Category "+c.getName()+" got deleted successfully");
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
