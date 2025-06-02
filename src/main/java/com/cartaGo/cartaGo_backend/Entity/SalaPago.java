package com.cartaGo.cartaGo_backend.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class SalaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime fecha_creacion = LocalDateTime.now();

    private Double saldo;

    @Enumerated(EnumType.STRING)
    private FormaDePago formaDePago;

    @Enumerated(EnumType.STRING)
    private EstadoSala estado;

    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;

    public enum FormaDePago {
        pago_igualitario, pago_personalizado
    }

    public enum EstadoSala {
        abierta, cerrada
    }
}
