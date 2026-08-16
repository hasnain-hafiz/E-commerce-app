package Ecommerce.controller;

import Ecommerce.model.Product;
import Ecommerce.service.seller.SellerService;
import Ecommerce.utils.dto.ProductDto;
import Ecommerce.utils.request.AddProductRequest;
import Ecommerce.utils.request.UpdateProductRequest;
import Ecommerce.utils.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/seller")
public class SellerController {

    private final SellerService sellerService;

    @GetMapping("/products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getSellerProducts() {
        List<Product> product = sellerService.getSellerProducts();
        return ResponseEntity.ok(new ApiResponse<>("Products fetched successfully!", sellerService.getConvertedProducts(product)));
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductDto>> addProduct(@Valid @RequestBody AddProductRequest request) {
        Product product = sellerService.addProduct(request);
        return ResponseEntity.ok(new ApiResponse<>("Product added successfully!", sellerService.convertToDto(product)));
    }

    @PutMapping("/update/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(@Valid @RequestBody UpdateProductRequest request, @PathVariable Long productId) {
        Product product = sellerService.updateProduct(request, productId);
        return ResponseEntity.ok(new ApiResponse<>("Product updated successfully!", sellerService.convertToDto(product)));
    }

    @DeleteMapping("/delete/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        sellerService.deleteProductById(productId);
        return ResponseEntity.ok(new ApiResponse<>("Product deleted successfully!", null));
    }
}
