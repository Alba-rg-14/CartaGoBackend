package com.cartaGo.cartaGo_backend.repository;

import com.cartaGo.cartaGo_backend.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RestauranteRepository extends JpaRepository<Restaurante, Integer> {
    Optional<Restaurante> findByNombreContainingIgnoreCase(String nombre);
    Optional<Restaurante> findByEstado(Restaurante.Estado estado);
    @Query("""
           select distinct r
           from Restaurante r
           join r.horarios h
           where h.dia = :dia
             and h.apertura <= :hora
             and h.cierre   >  :hora
           """)
    List<Restaurante> findAbiertosAhora(DayOfWeek dia, LocalTime hora);
    // Trae candidatos dentro de una “caja” aproximada
    List<Restaurante> findByLatBetweenAndLonBetween(
            double minLat, double maxLat,
            double minLon, double maxLon
    );
}
