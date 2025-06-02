package com.cartaGo.cartaGo_backend.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Cliente")
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String nombre;

    @Column(unique = true)
    private String email;

    private String contraseña;

    private String imagen;
}
