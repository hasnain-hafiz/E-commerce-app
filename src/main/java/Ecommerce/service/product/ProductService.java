package Ecommerce.service.product;

import Ecommerce.model.Category;
import Ecommerce.model.Image;
import Ecommerce.model.Product;
import Ecommerce.repository.CategoryRepository;
import Ecommerce.repository.ImageRepository;
import Ecommerce.repository.ProductRepository;
import Ecommerce.utils.dto.ImageDto;
import Ecommerce.utils.dto.ProductDto;
import Ecommerce.utils.exceptions.ResourceNotFoundException;
import Ecommerce.utils.request.AddProductRequest;
import Ecommerce.utils.request.UpdateProductRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final ImageRepository imageRepository;

    @Override
    @Transactional
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
                category
        );
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("product not found"));
    }

    @Override
    @Transactional
    public void deleteProductById(Long id) {
        productRepository.findById(id)
                .ifPresentOrElse(productRepository::delete,
                        () -> {throw new ResourceNotFoundException("Product not found!");});
    }

    @Override
    @Transactional
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
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products){
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public ProductDto convertToDto(Product product){
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        List< Image> images = imageRepository.findByProductId(product.getId());
        List<ImageDto> imageDtos = images.stream().map(image -> modelMapper.map(image, ImageDto.class)).toList();
        productDto.setImageList(imageDtos);
        return productDto;
    }

    @Override
    public List<Product> getFilteredProducts(String name, String brand, String category) {

        if (name != null && brand != null && category != null) {
            return productRepository.findByNameAndBrandAndCategoryName(name, brand, category);
        }

        if (name != null && brand != null) {
            return productRepository.findByNameAndBrand(name, brand);
        }

        if (name != null && category != null) {
            return productRepository.findByNameAndCategoryName(name, category);
        }

        if (brand != null && category != null) {
            return productRepository.findByBrandAndCategoryName(brand, category);
        }

        if (name != null) {
            return productRepository.findByName(name);
        }

        if (brand != null) {
            return productRepository.findByBrand(brand);
        }

        if (category != null) {
            return productRepository.findByCategoryName(category);
        }

        return productRepository.findAll();
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }
}