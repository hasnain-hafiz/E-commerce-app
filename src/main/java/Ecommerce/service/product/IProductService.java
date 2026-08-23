package Ecommerce.service.product;


import Ecommerce.model.Product;
import Ecommerce.utils.dto.ProductDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IProductService {
    Product getProductById(Long id);
    List<Product> getAllProducts();

    // NEW (Phase 2): paginated variant backing GET /product/all. The
    // unpaginated getAllProducts() above is left in place because
    // getFilteredProducts/searchProducts still return full lists this
    // phase (see PHASE_2_SUMMARY.md for why pagination there is deferred).
    Page<ProductDto> getAllProductsPaged(int page, int size);

    List<ProductDto> getConvertedProducts(List<Product> products);

    ProductDto convertToDto(Product product);

    List<Product> getFilteredProducts(String name, String brand, String category);

    List<Product> searchProducts(String keyword);
}
