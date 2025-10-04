package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.dto.CategoryResponseDto;
import com.ivm.inventory_management_system.entity.Category;
import com.ivm.inventory_management_system.entity.Item;
import com.ivm.inventory_management_system.enums.CategoryType;
import com.ivm.inventory_management_system.repository.ItemRepository;
import com.ivm.inventory_management_system.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final ItemRepository itemRepository;

    public CategoryController(CategoryService categoryService, ItemRepository itemRepository) {
        this.categoryService = categoryService;
        this.itemRepository = itemRepository;
    }

    @GetMapping("/list")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/owner/{ownerId}/create")
    public Category createCategory(@RequestBody String name) {
        return categoryService.createCategory(name.trim().toUpperCase(), true);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCategory(@PathVariable Long id, @RequestBody String name) {
        return categoryService.getCategoryById(id)
                .map(existing -> {
                    existing.setName(name.trim().toUpperCase());
                    categoryService.updateCategory(existing);
                    return ResponseEntity.ok("Category updated successfully");
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(existing -> {
                    if (!Boolean.TRUE.equals(existing.getIsCustom())) {
                        return ResponseEntity.badRequest().body("Cannot delete predefined category");
                    }
                    categoryService.deleteCategory(id);
                    return ResponseEntity.ok("Category deleted successfully");
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/types/{ownerId}")
    public CategoryResponseDto getCategoryTypes(@PathVariable Long ownerId) {
        CategoryResponseDto response = new CategoryResponseDto();

        List<String> categories = Arrays.stream(CategoryType.values())
                .map(Enum::name)
                .toList();
        response.setCategories(categories);

        Optional<Item> lastItem = itemRepository.findTopByUserIdOrderByCreatedAtDesc(ownerId);
        String defaultCategory = lastItem
                .map(item -> item.getCategory() != null ? item.getCategory().getName() : null)
                .orElse(null);
        response.setDefaultCategory(defaultCategory);

        return response;
    }
}
