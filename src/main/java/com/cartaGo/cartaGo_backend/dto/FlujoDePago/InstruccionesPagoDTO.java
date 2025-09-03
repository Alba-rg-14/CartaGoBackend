package com.cartaGo.cartaGo_backend.dto.FlujoDePago;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class InstruccionesPagoDTO {
    private Integer salaId;
    private String restauranteNombre;
    private java.time.LocalDateTime fechaGeneracion;
    private String modo;         // "igualitario" | "personalizado"
    private java.math.BigDecimal subtotal;
    private java.util.List<LineaInstruccionDTO> porCliente;
}