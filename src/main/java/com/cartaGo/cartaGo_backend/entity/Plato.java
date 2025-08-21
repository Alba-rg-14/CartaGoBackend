package com.cartaGo.cartaGo_backend.entity;

import jakarta.persistence.*;
import lombok.*;


import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Plato")
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal precio;

    @Column(length = 100)
    private String seccion;

    private Integer orden;

    @Column(length = 255)
    private String imagen;

    @ManyToOne
    @JoinColumn(name = "carta_id")
    private Carta carta;

    @OneToMany(mappedBy = "plato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlatoAlergenos> alergenos;

}