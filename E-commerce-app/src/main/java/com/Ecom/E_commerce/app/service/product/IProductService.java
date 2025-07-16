package com.Ecom.E_commerce.app.service.product;


import com.Ecom.E_commerce.app.utils.dto.ProductDto;
import com.Ecom.E_commerce.app.model.Product;
import com.Ecom.E_commerce.app.utils.request.AddProductRequest;
import com.Ecom.E_commerce.app.utils.request.UpdateProductRequest;

import java.util.List;

public interface IProductService {
    Product addProduct(AddProductRequest product);
    Product getProductById(Long id);
    void deleteProductById(Long id);
    Product updateProduct(UpdateProductRequest product, Long prodId);
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(String category);
    List<Product> getProductsByBrand(String brand);
    List<Product> getProductsByCategoryAndBrand(String category,String brand);
    List<Product> getProductsByBrandAndName(String brand,String name);
    List<Product> getProductsByName(String name);
    Long countProductsByBrandAndName(String brand,String name);

    List<ProductDto> getConvertedProducts(List<Product> products);

    ProductDto convertToDto(Product product);
}
