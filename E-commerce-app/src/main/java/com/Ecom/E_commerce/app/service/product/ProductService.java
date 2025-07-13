package com.Ecom.E_commerce.app.service.product;

import com.Ecom.E_commerce.app.dto.ImageDto;
import com.Ecom.E_commerce.app.dto.ProductDto;
import com.Ecom.E_commerce.app.exceptions.ResourceNotFoundException;
import com.Ecom.E_commerce.app.model.Category;
import com.Ecom.E_commerce.app.model.Image;
import com.Ecom.E_commerce.app.model.Product;
import com.Ecom.E_commerce.app.repository.CategoryRepository;
import com.Ecom.E_commerce.app.repository.ImageRepository;
import com.Ecom.E_commerce.app.repository.ProductRepository;
import com.Ecom.E_commerce.app.request.AddProductRequest;
import com.Ecom.E_commerce.app.request.UpdateProductRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final ImageRepository imageRepository;

    @Override
    public Product addProduct(AddProductRequest product) {

        Category category = Optional.ofNullable(categoryRepository.findByName(product.getCategory().getName()))
                .orElseGet(() ->{
                    Category newCategory = new Category(product.getCategory().getName());
                    return categoryRepository.save(newCategory);
                });
        product.setCategory(category);
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
    public void deleteProductById(Long id) {
        productRepository.findById(id)
                .ifPresentOrElse(productRepository::delete,
                        () -> {throw new ResourceNotFoundException("Product not found!");});
    }

    @Override

    public Product updateProduct(UpdateProductRequest product, Long prodId) {
       Product existingProduct = productRepository.findById(prodId)
               .orElseThrow(()-> new ResourceNotFoundException("product not found"));
       return productRepository.save(updateProduct(existingProduct,product));
    }

    private Product updateProduct(Product product, UpdateProductRequest request){
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setPrice(request.getPrice());
        product.setInventory(request.getInventory());
        Category category = categoryRepository.findByName(request.getCategory().getName());
        product.setCategory(category);
        return product;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category);
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategoryNameAndBrand(category,brand);
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand,name);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public Long countProductsByBrandAndName(String brand, String name) {
        return productRepository.countByBrandAndName(brand,name);
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
}