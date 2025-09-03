package com.cartaGo.cartaGo_backend.dto.FlujoDePago;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Data @Builder
public class SalaResumenDTO {
    private Integer salaId;
    private String  codigo;
    private String  estado;
    private Integer restauranteId;
    private LocalDateTime fechaCreacion;

    private List<ComensalDTO> comensales;
    private List<PlatoSalaResumenDTO> platos;

    private BigDecimal subtotal;
}
