package com.cartaGo.cartaGo_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.time.DayOfWeek;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;


}