package com.cartaGo.cartaGo_backend.dto.APIsDTO;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class GeocodingDTO {
    private double lat;       // coordenada
    private double lon;       // coordenada
    private String direccion; // texto bonito/normalizado
}