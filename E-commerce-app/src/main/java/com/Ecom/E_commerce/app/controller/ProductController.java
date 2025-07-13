package com.Ecom.E_commerce.app.controller;

import com.Ecom.E_commerce.app.dto.ProductDto;
import com.Ecom.E_commerce.app.exceptions.ResourceNotFoundException;
import com.Ecom.E_commerce.app.model.Product;
import com.Ecom.E_commerce.app.request.AddProductRequest;
import com.Ecom.E_commerce.app.request.UpdateProductRequest;
import com.Ecom.E_commerce.app.response.ApiResponse;
import com.Ecom.E_commerce.app.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/product")
public class ProductController {
    private final IProductService productService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllProducts(){
        try {
            List<Product> products = productService.getAllProducts();
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new ApiResponse("Products fetched successfully!", convertedProducts));
        }
        catch (Exception e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error",e.getMessage()));
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long productId){
        try {
            Product product = productService.getProductById(productId);
            ProductDto productDto = productService.convertToDto(product);
            return ResponseEntity.ok(new ApiResponse("Product fetched successfully!", productDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(),null));
        }
    }

    @GetMapping("/products/by-name")
    public ResponseEntity<ApiResponse> getProductByName(@RequestParam String name){
        try {
            List<Product> products = productService.getProductsByName(name);
            if(products.isEmpty()){
                return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Product not found!",null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new ApiResponse("Products fetched successfully!", convertedProducts));
        }
        catch (Exception e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }

    @GetMapping("/products/by-brand")
    public ResponseEntity<ApiResponse> getProductsByBrand(@RequestParam String brand){
        try {
            List<Product> products = productService.getProductsByBrand(brand);
            if(products.isEmpty()){
                return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Product not found!",null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new ApiResponse("Products fetched successfully!",  convertedProducts));
        }
        catch (Exception e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }
    @GetMapping("/products/by-brand-and-name")
    public ResponseEntity<ApiResponse> getProductsByBrandAndName(@RequestParam String brandName, @RequestParam String productName){
        try {
            List<Product> products = productService.getProductsByBrandAndName(brandName, productName);
            if(products.isEmpty()){
                return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Products not found!",null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new ApiResponse("Products fetched successfully!",  convertedProducts));
        }
        catch (Exception e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }
    @GetMapping("/products/by-category/{category}")
    public ResponseEntity<ApiResponse> getProductsByCategory(@PathVariable String category){
        try {
            List<Product> products = productService.getProductsByCategory(category);
            if(products.isEmpty()){
                return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Product not found!",null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new ApiResponse("Products fetched successfully!",  convertedProducts));
        }
        catch (Exception e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }
    @GetMapping("/products/by-category-and-brand")
    public ResponseEntity<ApiResponse> getProductsByCategoryAndBrand(@RequestParam String categoryName, @RequestParam String brandName){
        try {
            List<Product> products = productService.getProductsByCategoryAndBrand(categoryName, brandName);
            if(products.isEmpty()){
                return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Product not found!",null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new ApiResponse("Products fetched successfully!",  convertedProducts));
        }
        catch (Exception e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }

    @GetMapping("/count/by-brand-and-name")
    public ResponseEntity<ApiResponse> countProductsByBrandAndName(@RequestParam String brand, @RequestParam String name) {
        try {
            var productCount = productService.countProductsByBrandAndName(brand, name);
            return ResponseEntity.ok(new ApiResponse("Product count!", productCount));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProduct(@RequestBody AddProductRequest request){
        try {
            Product product = productService.addProduct(request);
            ProductDto productDto = productService.convertToDto(product);
            return ResponseEntity.ok(new ApiResponse("Product added successfully!", productDto));
        }
        catch (Exception e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error", e.getMessage()));
        }
    }
    @PutMapping("/update/{productId}")
    public ResponseEntity<ApiResponse> updateProduct(@RequestBody UpdateProductRequest request, @PathVariable Long productId){
        try {
            Product product = productService.updateProduct(request, productId);
            ProductDto productDto = productService.convertToDto(product);
            return ResponseEntity.ok(new ApiResponse("Product updated successfully!", productDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(),null));
        }
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long productId){
        try {
            productService.deleteProductById(productId);
            return ResponseEntity.ok(new ApiResponse("Product deleted successfully!", null));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(),null));
        }
    }
}
