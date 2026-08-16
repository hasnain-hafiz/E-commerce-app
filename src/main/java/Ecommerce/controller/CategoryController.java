package Ecommerce.controller;

import Ecommerce.model.Category;
import Ecommerce.service.category.CategoryService;
import Ecommerce.utils.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/category")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        return ResponseEntity.ok(new ApiResponse<>("Categories fetched successfully!", categoryService.getAllCategories()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>("Category fetched successfully!", categoryService.getCategoryById(id)));
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<ApiResponse<Category>> getCategoryByName(@PathVariable String name) {
        return ResponseEntity.ok(new ApiResponse<>("Category fetched successfully!", categoryService.getCategoryByName(name)));
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> addCategory(@Valid @RequestBody Category category) {
        categoryService.addCategory(category);
        return ResponseEntity.ok(new ApiResponse<>("Category added successfully!", null));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@Valid @RequestBody Category category, @PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>("Category updated successfully!", categoryService.updateCategory(category, id)));
    }

    @DeleteMapping("/delete/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategoryById(categoryId);
        return ResponseEntity.ok(new ApiResponse<>("Category deleted successfully!", null));
    }
}
