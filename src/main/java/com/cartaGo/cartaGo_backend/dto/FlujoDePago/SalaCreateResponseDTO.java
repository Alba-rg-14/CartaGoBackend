package com.cartaGo.cartaGo_backend.dto.FlujoDePago;

import lombok.*;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class SalaCreateResponseDTO {
    private Integer salaId;
    private String  codigo;
    private String  estado;        // "abierta"
    private String  fechaCreacion; // ISO-8601
}