package com.Ecom.E_commerce.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Blob;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;

    @Lob
    private Blob image;
    private String fileUrl;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
