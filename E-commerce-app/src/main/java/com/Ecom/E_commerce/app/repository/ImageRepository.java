package com.Ecom.E_commerce.app.repository;

import com.Ecom.E_commerce.app.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByProductId(Long productId);
}
