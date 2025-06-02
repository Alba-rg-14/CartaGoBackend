package com.cartaGo.cartaGo_backend.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Carta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;

    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;
}
