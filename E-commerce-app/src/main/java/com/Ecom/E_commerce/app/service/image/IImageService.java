package com.Ecom.E_commerce.app.service.image;

import com.Ecom.E_commerce.app.dto.ImageDto;
import com.Ecom.E_commerce.app.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IImageService {
    List<ImageDto> saveImages(List<MultipartFile> file, Long productId);
    void updateImage(MultipartFile file,Long productId);
    Image getImageById(Long id);
    void deleteImageById(Long id);
}
