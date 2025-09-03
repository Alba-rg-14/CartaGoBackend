package com.cartaGo.cartaGo_backend.dto.CartaDTO.PlatosDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlergenoDTO {
    private Integer id;
    private String nombre;
    private String imagen;
}
