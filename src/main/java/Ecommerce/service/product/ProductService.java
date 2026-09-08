package Ecommerce.service.product;

import Ecommerce.model.Image;
import Ecommerce.model.Product;
import Ecommerce.repository.CategoryRepository;
import Ecommerce.repository.ImageRepository;
import Ecommerce.repository.ProductRepository;
import Ecommerce.repository.ReviewRepository;
import Ecommerce.utils.dto.ImageDto;
import Ecommerce.utils.dto.ProductDto;
import Ecommerce.utils.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final ImageRepository imageRepository;
    // NEW (Phase 2b): needed to populate averageRating/reviewCount below.
    private final ReviewRepository reviewRepository;

    private static final int MAX_PAGE_SIZE = 60;

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("product not found"));
    }


    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Page<ProductDto> getAllProductsPaged(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        return productRepository.findAll(pageable).map(this::convertToDto);
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

        // NEW (Phase 2b): rating summary. Known limitation: this is one
        // extra query per product (matching the existing per-product image
        // query above), so a page of N products issues 2N+1 queries. Fine
        // at this app's scale; flagged for batch-loading in the
        // Performance phase if the catalog grows significantly.
        long reviewCount = reviewRepository.countByProductId(product.getId());
        productDto.setReviewCount(reviewCount);
        productDto.setAverageRating(reviewCount > 0
                ? reviewRepository.findAverageRatingByProductId(product.getId())
                : null);

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
