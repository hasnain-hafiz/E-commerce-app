package com.Ecom.E_commerce.app.repository;

import com.Ecom.E_commerce.app.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryName(String category);

    List<Product> findByBrand(String brand);

    List<Product> findByCategoryNameAndBrand(String category, String brand);

    Long countByBrandAndName(String brand, String name);

    List<Product> findByBrandAndName(String brand, String name);

    List<Product> findByName(String name);

    void deleteProductById(Long id);
}
