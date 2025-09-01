package com.cartaGo.cartaGo_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrScanResponseDTO {
    private Integer restauranteId;
    private Integer platosCreados;
    private List<PlatoDTO> detalles;
}
