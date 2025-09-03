package com.cartaGo.cartaGo_backend.dto.FlujoDePago;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class AddPlatoResponseDTO {
    private Integer platoSalaId;
    private Integer platoId;
    private String  platoNombre;
    private BigDecimal precioUnitario;   // snapshot del precio
    private int participantes;
}
