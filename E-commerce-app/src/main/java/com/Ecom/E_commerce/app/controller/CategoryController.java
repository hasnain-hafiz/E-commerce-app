package com.Ecom.E_commerce.app.controller;

import com.Ecom.E_commerce.app.utils.exceptions.AlreadyExistsException;
import com.Ecom.E_commerce.app.utils.exceptions.ResourceNotFoundException;
import com.Ecom.E_commerce.app.model.Category;
import com.Ecom.E_commerce.app.utils.response.ApiResponse;
import com.Ecom.E_commerce.app.service.category.CategoryService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/category")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/all")
    @PermitAll
    public ResponseEntity<ApiResponse> getAllCategories(){
        try {
            List<Category> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(new ApiResponse("Categories fetched successfully!", categories));
        }
        catch (Exception e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error",INTERNAL_SERVER_ERROR));
        }
    }

    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable Long id){
        try {
            Category category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(new ApiResponse("Category fetched successfully!", category));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse( e.getMessage(),null));
        }
    }

    @GetMapping("/by-name/{name}")
    @PermitAll
    public ResponseEntity<ApiResponse> getCategoryByName(@PathVariable String name){
        try {
            Category category = categoryService.getCategoryByName(name);
            return ResponseEntity.ok(new ApiResponse("Category fetched successfully!", category));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse( e.getMessage(),null));
        }
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> addCategory(@Valid @RequestBody Category category){
        try {
            categoryService.addCategory(category);
            return ResponseEntity.ok(new ApiResponse("Category added successfully!", null));
        }
        catch (AlreadyExistsException e){
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateCategory(@Valid @RequestBody Category category, @PathVariable Long id){
        try {
            Category updatedCategory = categoryService.updateCategory(category, id);
            return ResponseEntity.ok(new ApiResponse("Category updated successfully!", updatedCategory));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse( e.getMessage(),null));
        }
    }

    @DeleteMapping("/delete/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long categoryId){
        try {
            categoryService.deleteCategoryById(categoryId);
            return ResponseEntity.ok(new ApiResponse("Category deleted successfully!", null));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse( e.getMessage(),null));
        }
    }
}
