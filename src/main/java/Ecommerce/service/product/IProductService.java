package Ecommerce.service.product;


import Ecommerce.model.Product;
import Ecommerce.utils.dto.ProductDto;
import Ecommerce.utils.request.AddProductRequest;
import Ecommerce.utils.request.UpdateProductRequest;

import java.util.List;

public interface IProductService {
    Product getProductById(Long id);
    List<Product> getAllProducts();

    List<ProductDto> getConvertedProducts(List<Product> products);

    ProductDto convertToDto(Product product);

    List<Product> getFilteredProducts(String name, String brand, String category);

    List<Product> searchProducts(String keyword);
}
