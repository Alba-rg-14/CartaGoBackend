package com.cartaGo.cartaGo_backend.dto.FlujoDePago;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class PlatoSalaResumenDTO {
    private Integer platoSalaId;
    private Integer platoId;
    private String  platoNombre;
    private BigDecimal precioActual;
    private List<ComensalDTO> participantes;
}
