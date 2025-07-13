package com.Ecom.E_commerce.app.service.category;

import com.Ecom.E_commerce.app.model.Category;

import java.util.Calendar;
import java.util.List;

public interface ICategoryService {
    Category addCategory(Category category);
    Category getCategoryById(Long id);
    void deleteCategoryById(Long id);
    Category updateCategory(Category category, Long id);
    List<Category> getAllCategories();
    Category getCategoryByName(String name);
}
