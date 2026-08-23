package Ecommerce.service.seller;

import Ecommerce.model.Category;
import Ecommerce.model.Product;
import Ecommerce.model.user.User;
import Ecommerce.repository.CategoryRepository;
import Ecommerce.repository.ImageRepository;
import Ecommerce.repository.ProductRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.utils.exceptions.ForbiddenException;
import Ecommerce.utils.exceptions.ResourceNotFoundException;
import Ecommerce.utils.request.UpdateProductRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private ImageRepository imageRepository;

    @InjectMocks
    private SellerService sellerService;

    private User owner;
    private User otherSeller;
    private Product product;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@shop.com");

        otherSeller = new User();
        otherSeller.setId(2L);
        otherSeller.setEmail("attacker@shop.com");

        product = new Product();
        product.setId(100L);
        product.setName("Original Name");
        product.setSeller(owner);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(User user) {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null));
    }

    @Test
    void deleteProductById_throwsForbidden_whenCallerIsNotTheOwner() {
        loginAs(otherSeller);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThrows(ForbiddenException.class, () -> sellerService.deleteProductById(100L));
        verify(productRepository, never()).delete(any());
    }

    @Test
    void deleteProductById_succeeds_whenCallerIsTheOwner() {
        loginAs(owner);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertDoesNotThrow(() -> sellerService.deleteProductById(100L));
        verify(productRepository).delete(product);
    }

    @Test
    void updateProduct_throwsForbidden_whenCallerIsNotTheOwner() {
        loginAs(otherSeller);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Hijacked Name");
        request.setDescription("desc");
        request.setBrand("brand");
        request.setPrice(BigDecimal.TEN);
        request.setInventory(5);
        request.setCategory("Electronics");

        assertThrows(ForbiddenException.class, () -> sellerService.updateProduct(request, 100L));
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_succeeds_whenCallerIsTheOwner() {
        loginAs(owner);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(categoryRepository.findByName("Electronics"))
                .thenReturn(Optional.of(new Category("Electronics")));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Updated Name");
        request.setDescription("desc");
        request.setBrand("brand");
        request.setPrice(BigDecimal.TEN);
        request.setInventory(5);
        request.setCategory("Electronics");

        Product result = sellerService.updateProduct(request, 100L);

        assertEquals("Updated Name", result.getName());
        verify(productRepository).save(product);
    }

    @Test
    void deleteProductById_throwsNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sellerService.deleteProductById(999L));
    }
}
