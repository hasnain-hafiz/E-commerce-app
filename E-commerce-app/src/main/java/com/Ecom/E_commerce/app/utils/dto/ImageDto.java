package com.Ecom.E_commerce.app.utils.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ImageDto {
    @NotNull
    private Long Id;
    @NotBlank
    private String fileName;
    @NotBlank
    private String fileUrl;
}
