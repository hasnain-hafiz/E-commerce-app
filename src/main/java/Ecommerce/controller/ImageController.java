package Ecommerce.controller;

import Ecommerce.model.Image;
import Ecommerce.service.image.IImageService;
import Ecommerce.utils.dto.ImageDto;
import Ecommerce.utils.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/image")
public class ImageController {

    private final IImageService imageService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<List<ImageDto>>> saveImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam Long productId
    ) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("No files provided", null));
        }
        List<ImageDto> imageDtos = imageService.saveImages(files, productId);
        return ResponseEntity.ok(new ApiResponse<>("Upload success!", imageDtos));
    }

    @GetMapping("/download/{imageId}")
    public ResponseEntity<Resource> downloadImage(@PathVariable Long imageId) {
        Image image = imageService.getImageById(imageId);
        byte[] data = imageService.getImageData(imageId);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFileName() + "\"")
                .body(resource);
    }

    @PutMapping("/update/{imageId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> updateImage(@RequestParam MultipartFile file, @PathVariable Long imageId) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("No files provided", null));
        }
        imageService.updateImage(file, imageId);
        return ResponseEntity.ok(new ApiResponse<>("Update success!", null));
    }

    @DeleteMapping("delete/{imageId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long imageId) {
        imageService.deleteImageById(imageId);
        return ResponseEntity.ok(new ApiResponse<>("Delete success!", null));
    }
}
