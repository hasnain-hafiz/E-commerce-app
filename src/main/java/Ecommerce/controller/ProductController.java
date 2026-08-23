package Ecommerce.controller;

import Ecommerce.model.Product;
import Ecommerce.service.product.IProductService;
import Ecommerce.utils.dto.ProductDto;
import Ecommerce.utils.exceptions.ResourceNotFoundException;
import Ecommerce.utils.response.ApiResponse;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/product")
@CrossOrigin(origins = "https://ecommerce-frontend-sigma-lilac.vercel.app")
public class ProductController {
    private final IProductService productService;

    // CHANGED: previously returned the entire product table in one
    // response. Now paginated; defaults preserve a reasonable page size
    // (12) so existing callers that don't pass page/size still get a
    // sensible first page instead of erroring. Frontend updated in the
    // same change (see Home.jsx) to read `content`/`totalPages` instead of
    // treating the response as a flat array.
    @GetMapping("/all")
    @PermitAll
    public ResponseEntity<ApiResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ){
        Page<ProductDto> products = productService.getAllProductsPaged(page, size);
        return ResponseEntity.ok(new ApiResponse("Products fetched successfully!", products));
    }

    @GetMapping("/{productId}")
    @PermitAll
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

    @GetMapping
    public ResponseEntity<ApiResponse> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category
    ) {
        List<Product> products = productService.getFilteredProducts(name, brand, category);
        return ResponseEntity.ok(
                new ApiResponse("Products fetched", productService.getConvertedProducts(products))
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(
                new ApiResponse("Products fetched", productService.getConvertedProducts(products))
        );
    }
}
