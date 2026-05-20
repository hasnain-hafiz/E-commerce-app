package Ecommerce.service.product;


import Ecommerce.model.Product;
import Ecommerce.utils.dto.ProductDto;
import Ecommerce.utils.request.AddProductRequest;
import Ecommerce.utils.request.UpdateProductRequest;

import java.util.List;

public interface IProductService {
    Product addProduct(AddProductRequest product);
    Product getProductById(Long id);
    void deleteProductById(Long id);
    Product updateProduct(UpdateProductRequest product, Long prodId);
    List<Product> getAllProducts();

    List<ProductDto> getConvertedProducts(List<Product> products);

    ProductDto convertToDto(Product product);

    List<Product> getFilteredProducts(String name, String brand, String category);

    List<Product> searchProducts(String keyword);
}
