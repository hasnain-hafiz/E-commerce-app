package Ecommerce.repository;

import Ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByBrand(String brand);

    List<Product> findByName(String name);

    List<Product> findByCategoryName(String category);

    List<Product> findByBrandAndCategoryName(String brand, String category);

    List<Product> findByNameAndCategoryName(String name, String category);

    List<Product> findByNameAndBrand(String name, String brand);

    List<Product> findByNameAndBrandAndCategoryName(String name, String brand, String category);
    @Query("""
SELECT p FROM Product p
WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))
OR LOWER(p.category.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<Product> searchProducts(@Param("keyword") String keyword);

    List<Product> findBySellerId(Long sellerId);
}
