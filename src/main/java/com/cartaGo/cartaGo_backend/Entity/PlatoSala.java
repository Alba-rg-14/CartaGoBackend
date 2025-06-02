package com.cartaGo.cartaGo_backend.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PlatoSala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sala_pago_id")
    private SalaPago salaPago;

    @ManyToOne
    @JoinColumn(name = "plato_id")
    private Plato plato;
}
