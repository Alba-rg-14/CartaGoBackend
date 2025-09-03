package com.cartaGo.cartaGo_backend.dto.FlujoDePago;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
public class LineaInstruccionDTO {
    private Integer clienteId;
    private String  clienteNombre;
    private String  email; // opcional
    private java.math.BigDecimal totalAPagar;
    private java.util.List<DetallePlatoClienteDTO> detalles;
}
