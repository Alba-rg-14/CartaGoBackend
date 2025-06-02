package com.cartaGo.cartaGo_backend.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String descripcion;

    private BigDecimal precio;
    private String seccion;

    @ManyToOne
    @JoinColumn(name = "carta_id")
    private Carta carta;
}