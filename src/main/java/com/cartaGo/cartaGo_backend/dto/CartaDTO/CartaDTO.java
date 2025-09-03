package com.cartaGo.cartaGo_backend.dto.CartaDTO;

import com.cartaGo.cartaGo_backend.dto.CartaDTO.PlatosDTO.PlatoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartaDTO {
    private Integer id;
    private Integer restauranteId;
    private List<PlatoDTO> platos;
}
