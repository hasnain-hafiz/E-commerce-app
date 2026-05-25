package Ecommerce.controller;

import Ecommerce.model.Product;
import Ecommerce.service.seller.SellerService;
import Ecommerce.utils.dto.ProductDto;
import Ecommerce.utils.exceptions.ResourceNotFoundException;
import Ecommerce.utils.request.AddProductRequest;
import Ecommerce.utils.request.UpdateProductRequest;
import Ecommerce.utils.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/seller")
public class SellerController {

    private final SellerService sellerService;

    @GetMapping("/products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse> getSellerProducts(){
        try {
            List<Product> product = sellerService.getSellerProducts();
            List<ProductDto> productDto = sellerService.getConvertedProducts(product);
            return ResponseEntity.ok(new ApiResponse("Products fetched successfully!", productDto));
        }
        catch (Exception e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse> addProduct(@Valid @RequestBody AddProductRequest request){
        try {
            Product product = sellerService.addProduct(request);
            ProductDto productDto = sellerService.convertToDto(product);
            return ResponseEntity.ok(new ApiResponse("Product added successfully!", productDto));
        }
        catch (Exception e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PutMapping("/update/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse> updateProduct(@Valid @RequestBody UpdateProductRequest request, @PathVariable Long productId){
        try {
            Product product = sellerService.updateProduct(request, productId);
            ProductDto productDto = sellerService.convertToDto(product);
            return ResponseEntity.ok(new ApiResponse("Product updated successfully!", productDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(),null));
        }
    }

    @DeleteMapping("/delete/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long productId){
        try {
            sellerService.deleteProductById(productId);
            return ResponseEntity.ok(new ApiResponse("Product deleted successfully!", null));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(),null));
        }
    }
}
