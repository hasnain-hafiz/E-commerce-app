package com.Ecom.E_commerce.app.repository;

import com.Ecom.E_commerce.app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);

    boolean existsByName(String name);
}
