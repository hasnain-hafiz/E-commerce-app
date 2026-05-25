package Ecommerce.service.seller;

import Ecommerce.model.Category;
import Ecommerce.model.Image;
import Ecommerce.model.Product;
import Ecommerce.model.user.User;
import Ecommerce.repository.CategoryRepository;
import Ecommerce.repository.ImageRepository;
import Ecommerce.repository.ProductRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.utils.dto.ImageDto;
import Ecommerce.utils.dto.ProductDto;
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

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public List<Product> getSellerProducts(){
        return productRepository.findBySellerId(getCurrentUser().getId());
    }

    @Transactional
    @Override
    public Product addProduct(AddProductRequest product) {

        Category category = categoryRepository.findByName(product.getCategory().getName())
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
        productRepository.findById(id)
                .ifPresentOrElse(productRepository::delete,
                        () -> {throw new ResourceNotFoundException("Product not found!");});
    }

    @Transactional
    @Override
    public Product updateProduct(UpdateProductRequest product, Long prodId) {
        Product existingProduct = productRepository.findById(prodId)
                .orElseThrow(()-> new ResourceNotFoundException("product not found"));
        return productRepository.save(updateProduct(existingProduct,product));
    }

    private Product updateProduct(Product product, UpdateProductRequest request){
        Category category = categoryRepository.findByName(request.getCategory().getName())
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
        return productDto;
    }

}