package Ecommerce.controller;

import Ecommerce.model.Product;
import Ecommerce.service.product.IProductService;
import Ecommerce.utils.dto.ProductDto;
import Ecommerce.utils.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/product")
public class ProductController {
    private final IProductService productService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(new ApiResponse<>("Products fetched successfully!", productService.getConvertedProducts(products)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(new ApiResponse<>("Product fetched successfully!", productService.convertToDto(product)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category
    ) {
        List<Product> products = productService.getFilteredProducts(name, brand, category);
        return ResponseEntity.ok(new ApiResponse<>("Products fetched", productService.getConvertedProducts(products)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDto>>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(new ApiResponse<>("Products fetched", productService.getConvertedProducts(products)));
    }
}
