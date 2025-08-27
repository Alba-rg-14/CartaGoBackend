package com.cartaGo.cartaGo_backend.repository;

import com.cartaGo.cartaGo_backend.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Integer> {
    List<Horario> findByRestauranteId(Integer restauranteId);
    List<Horario> findByDia(DayOfWeek dia);
    List<Horario> findByRestauranteIdAndDia(Integer restauranteId, DayOfWeek dia);
    List<Horario> findByRestauranteIdAndDiaAndAperturaLessThanEqualAndCierreGreaterThan(
            Integer restauranteId, DayOfWeek dia, LocalTime ahora, LocalTime ahora2
    );
}
