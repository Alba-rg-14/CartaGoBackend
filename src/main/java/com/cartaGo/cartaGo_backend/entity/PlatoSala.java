package com.cartaGo.cartaGo_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
