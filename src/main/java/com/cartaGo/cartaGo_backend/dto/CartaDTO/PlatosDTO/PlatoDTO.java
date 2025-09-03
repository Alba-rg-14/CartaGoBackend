package com.cartaGo.cartaGo_backend.dto.CartaDTO.PlatosDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatoDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String seccion;
    private Integer orden;
    private String imagen;
    private List<AlergenoDTO> alergenos;
}
