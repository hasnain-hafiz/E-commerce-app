package com.Ecom.E_commerce.app.service.category;

import com.Ecom.E_commerce.app.exceptions.AlreadyExistsException;
import com.Ecom.E_commerce.app.exceptions.ResourceNotFoundException;
import com.Ecom.E_commerce.app.model.Category;
import com.Ecom.E_commerce.app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService{

    private final CategoryRepository categoryRepository;

    @Override
    public Category addCategory(Category category ){
        return Optional.of(category).filter(c -> !categoryRepository.existsByName(c.getName()))
                .map(categoryRepository::save)
                .orElseThrow(() -> new AlreadyExistsException(category.getName() + "category already exists"));
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("category not found"));
    }

    @Override
    public void deleteCategoryById(Long id) {
        categoryRepository.findById(id).ifPresentOrElse(categoryRepository::delete,
                () -> {throw new ResourceNotFoundException("category not found");});
    }

    @Override
    public Category updateCategory(Category category, Long id) {
        return Optional.ofNullable(getCategoryById(id)).map(oldCategory -> {
            oldCategory.setName(category.getName());
            return categoryRepository.save(oldCategory);
        }).orElseThrow(() -> new ResourceNotFoundException("category not found"));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
}
