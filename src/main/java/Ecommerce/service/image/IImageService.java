package Ecommerce.service.image;

import Ecommerce.model.Image;
import Ecommerce.utils.dto.ImageDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IImageService {
    @Transactional
    byte[] getImageData(Long imageId);

    List<ImageDto> saveImages(List<MultipartFile> file, Long productId);
    void updateImage(MultipartFile file,Long productId);
    Image getImageById(Long id);
    void deleteImageById(Long id);
}
