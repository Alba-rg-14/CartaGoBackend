package com.cartaGo.cartaGo_backend.entity;

import jakarta.persistence.*;
import lombok.*;


import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Carta")
public class Carta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @OneToMany(mappedBy = "carta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plato> platos;

    @OneToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;
}
