package com.cartaGo.cartaGo_backend.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@IdClass(ParticipacionSalaId.class)
public class ParticipacionSala {

    @Id
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Id
    @ManyToOne
    @JoinColumn(name = "sala_pago_id")
    private SalaPago salaPago;

    private BigDecimal monto;
}
