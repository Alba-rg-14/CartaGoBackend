package com.cartaGo.cartaGo_backend.dto.FlujoDePago;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePlatoClienteDTO {
    private Integer platoSalaId;
    private Integer platoId;
    private String  platoNombre;
    private BigDecimal precioPlato;
    private BigDecimal tuParte;
}
