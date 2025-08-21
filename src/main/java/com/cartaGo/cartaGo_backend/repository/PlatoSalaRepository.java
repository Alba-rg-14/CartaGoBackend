package com.cartaGo.cartaGo_backend.repository;

import com.cartaGo.cartaGo_backend.entity.PlatoSala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatoSalaRepository extends JpaRepository<PlatoSala, Integer> {
    List<PlatoSala> findBySalaPagoId(Integer salaPagoId);
    List<PlatoSala> findByPlatoId(Integer platoId);
}
