package com.cartaGo.cartaGo_backend.dto.APIsDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrScanRequestDTO {
    // URLs públicas de Cloudinary (o similares)
    private List<String> imageUrls;
    // "replace" o "append"
    private String mode;
}
