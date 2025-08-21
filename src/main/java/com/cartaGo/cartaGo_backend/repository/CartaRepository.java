package com.cartaGo.cartaGo_backend.repository;

import com.cartaGo.cartaGo_backend.entity.Carta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartaRepository extends JpaRepository<Carta, Integer> {
    Optional<Carta> findByRestauranteId(Integer restauranteId);
}
