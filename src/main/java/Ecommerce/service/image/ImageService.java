package Ecommerce.service.image;

import Ecommerce.model.Image;
import Ecommerce.model.Product;
import Ecommerce.repository.ImageRepository;
import Ecommerce.service.product.ProductService;
import Ecommerce.utils.dto.ImageDto;
import Ecommerce.utils.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService implements IImageService {

    private final ImageRepository imageRepository;
    private final ProductService productService;

    @Transactional
    @Override
    public byte[] getImageData(Long imageId) {
        try {
            Image image = getImageById(imageId);
            return image.getImage().getBytes(1, (int) image.getImage().length());
        } catch (SQLException e) {
            throw new RuntimeException("Error reading image", e);
        }
    }

    @Override
    @Transactional
    public List<ImageDto> saveImages(List<MultipartFile> files, Long productId) {

        Product product = productService.getProductById(productId);

        List<ImageDto> savedImageDto = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                Image image = new Image();
                image.setFileName(file.getOriginalFilename());
                image.setFileType(file.getContentType());
                image.setImage(new SerialBlob(file.getBytes()));
                image.setProduct(product);

                // ✅ FIRST SAVE (so ID is generated)
                System.out.println("Saving image...");
                Image savedImage = imageRepository.save(image);

                System.out.println("Saved image ID: " + savedImage.getId());

                // ✅ NOW build correct URL using ID
                String fileUrl = "/api/v1/image/download/" + savedImage.getId();

                savedImage.setFileUrl(fileUrl);

                // ✅ SAVE AGAIN with URL
                imageRepository.save(savedImage);

                // DTO
                ImageDto dto = new ImageDto();
                dto.setId(savedImage.getId());
                dto.setFileName(savedImage.getFileName());
                dto.setFileUrl(savedImage.getFileUrl());

                savedImageDto.add(dto);

            } catch (IOException | SQLException e) {
                throw new RuntimeException("Error saving image", e);
            }
        }

        return savedImageDto;
    }

    @Override
    @Transactional
    public void updateImage(MultipartFile file, Long productId) {
        Image image = getImageById(productId);
        try {
            image.setFileName(file.getOriginalFilename());
            image.setImage(new SerialBlob(file.getBytes()));

            imageRepository.save(image);

        } catch (IOException | SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public Image getImageById(Long id) {
        return imageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("image not found with id" + id));
    }

    @Override
    @Transactional
    public void deleteImageById(Long id) {
        imageRepository.findById(id).ifPresentOrElse(imageRepository::delete,
                () -> {throw new ResourceNotFoundException("image not found with id" + id);});

    }
}
