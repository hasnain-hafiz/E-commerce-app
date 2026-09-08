package Ecommerce.service.seller;

import Ecommerce.model.Category;
import Ecommerce.model.Image;
import Ecommerce.model.Product;
import Ecommerce.model.user.User;
import Ecommerce.repository.CategoryRepository;
import Ecommerce.repository.ImageRepository;
import Ecommerce.repository.ProductRepository;
import Ecommerce.repository.ReviewRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.utils.dto.ImageDto;
import Ecommerce.utils.dto.ProductDto;
import Ecommerce.utils.exceptions.ForbiddenException;
import Ecommerce.utils.exceptions.ResourceNotFoundException;
import Ecommerce.utils.exceptions.UserNotFoundException;
import Ecommerce.utils.request.AddProductRequest;
import Ecommerce.utils.request.UpdateProductRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerService implements ISellerService {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final ImageRepository imageRepository;
    // NEW (Phase 2b): kept consistent with ProductService.convertToDto so
    // a seller sees the same rating summary on their own product list.
    private final ReviewRepository reviewRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    // NEW: ownership guard. Previously updateProduct/deleteProductById only
    // checked that the product existed, so any authenticated SELLER could
    // modify or delete ANY seller's product by guessing/incrementing an id.
    private void assertOwnsProduct(Product product) {
        Long currentUserId = getCurrentUser().getId();
        if (product.getSeller() == null || !product.getSeller().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to modify this product");
        }
    }

    @Override
    public List<Product> getSellerProducts(){
        return productRepository.findBySellerId(getCurrentUser().getId());
    }

    @Transactional
    @Override
    public Product addProduct(AddProductRequest product) {

        // CHANGED: product.getCategory() is now a plain String (see
        // AddProductRequest) instead of a Category entity.
        Category category = categoryRepository.findByName(product.getCategory())
                .orElseThrow(()-> new ResourceNotFoundException("Category not found!"));

        return productRepository.save(createProduct(product,category));
    }

    private Product createProduct(AddProductRequest request, Category category){
        return new Product(
                request.getName(),
                request.getDescription(),
                request.getBrand(),
                request.getPrice(),
                request.getInventory(),
                category,
                getCurrentUser()
        );
    }

    @Transactional
    @Override
    public void deleteProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));

        // CHANGED: ownership check added (previously missing entirely).
        assertOwnsProduct(product);

        productRepository.delete(product);
    }

    @Transactional
    @Override
    public Product updateProduct(UpdateProductRequest product, Long prodId) {
        Product existingProduct = productRepository.findById(prodId)
                .orElseThrow(()-> new ResourceNotFoundException("product not found"));

        // CHANGED: ownership check added (previously missing entirely).
        assertOwnsProduct(existingProduct);

        return productRepository.save(updateProduct(existingProduct,product));
    }

    private Product updateProduct(Product product, UpdateProductRequest request){
        // CHANGED: request.getCategory() is now a plain String (see
        // UpdateProductRequest) instead of a Category entity.
        Category category = categoryRepository.findByName(request.getCategory())
                .orElseThrow(()-> new ResourceNotFoundException("Category not found!"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setPrice(request.getPrice());
        product.setInventory(request.getInventory());
        product.setCategory(category);
        return product;
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products){
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public ProductDto convertToDto(Product product){
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        List<Image> images = imageRepository.findByProductId(product.getId());
        List<ImageDto> imageDtos = images.stream().map(image -> modelMapper.map(image, ImageDto.class)).toList();
        productDto.setImageList(imageDtos);

        long reviewCount = reviewRepository.countByProductId(product.getId());
        productDto.setReviewCount(reviewCount);
        productDto.setAverageRating(reviewCount > 0
                ? reviewRepository.findAverageRatingByProductId(product.getId())
                : null);

        return productDto;
    }

}
