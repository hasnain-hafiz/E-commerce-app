package Ecommerce.service.seller;

import Ecommerce.model.Product;
import Ecommerce.utils.dto.ProductDto;
import Ecommerce.utils.request.AddProductRequest;
import Ecommerce.utils.request.UpdateProductRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ISellerService {
    List<Product> getSellerProducts();

    @Transactional
    Product addProduct(AddProductRequest product);

    @Transactional
    void deleteProductById(Long id);

    @Transactional
    Product updateProduct(UpdateProductRequest product, Long prodId);

    List<ProductDto> getConvertedProducts(List<Product> products);

    ProductDto convertToDto(Product product);
}
