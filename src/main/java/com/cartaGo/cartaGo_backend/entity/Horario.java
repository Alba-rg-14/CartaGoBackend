package com.cartaGo.cartaGo_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.time.DayOfWeek;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Horario")
@Builder
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dia;

    private LocalTime apertura;
    private LocalTime cierre;

    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;


}