package com.cartaGo.cartaGo_backend.dto.CartaDTO.PlatosDTO;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PlatoRequestDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String seccion;   // obligatorio
    private Integer orden;    // opcional; si es null se coloca al final de la sección
    private String imagen;
    private List<Integer> alergenosIds; // IDs de Alergeno
}
