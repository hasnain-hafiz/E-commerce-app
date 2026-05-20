package Ecommerce.controller;

import Ecommerce.model.Image;
import Ecommerce.service.image.IImageService;
import Ecommerce.utils.dto.ImageDto;
import Ecommerce.utils.exceptions.ResourceNotFoundException;
import Ecommerce.utils.response.ApiResponse;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLException;
import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/image")
public class ImageController {

    private final IImageService imageService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse> saveImages(
            @RequestBody List<MultipartFile> files,
            @RequestParam Long productId
    ) {
        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse("No files provided", null));
            }

            System.out.println("Product ID: " + productId);
            List<ImageDto> imageDtos = imageService.saveImages(files, productId);
            System.out.println("Image Dtos: " + imageDtos);
            return ResponseEntity.ok(new ApiResponse("Upload success!", imageDtos));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Upload failed!", e.getMessage()));
        }
    }

    @GetMapping("/download/{imageId}")
    @PermitAll
    public ResponseEntity<Resource> downloadImage(@PathVariable Long imageId) {

        Image image = imageService.getImageById(imageId);
        byte[] data = imageService.getImageData(imageId);

        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + image.getFileName() + "\"") // ✅ FIXED
                .body(resource);
    }

    @PutMapping("/update/{imageId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse> updateImage(@RequestParam  MultipartFile file, @PathVariable Long imageId) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse("No files provided", null));
            }

            Image image = imageService.getImageById(imageId);
            if (image != null) {
                imageService.updateImage(file, imageId);
                return ResponseEntity.ok(new ApiResponse("Update success!", null));
            }


        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Update failed!", e.getMessage()));
        }
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Update failed!", INTERNAL_SERVER_ERROR));
    }

    @PutMapping("delete/{imageId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse> deleteImage(@PathVariable Long imageId) {
        try {
            Image image = imageService.getImageById(imageId);
            if (image != null) {
                imageService.deleteImageById(imageId);
                return ResponseEntity.ok(new ApiResponse("Delete success!", null));
            }

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Delete failed!", INTERNAL_SERVER_ERROR));
    }
}