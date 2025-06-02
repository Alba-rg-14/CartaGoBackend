package com.cartaGo.cartaGo_backend.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String descripcion;
    private String email;
    private String contraseña;
    private String direccion;
    private Double lat;
    private Double lon;
    private String imagen;

    @Enumerated(EnumType.STRING)
    private EstadoRestaurante estado;

    public enum EstadoRestaurante {
        abierto, cerrado
    }
}
