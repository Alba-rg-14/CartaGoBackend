package com.cartaGo.cartaGo_backend.Repository;

import com.cartaGo.cartaGo_backend.Entity.Carta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartaRepository extends JpaRepository<Carta, Integer> {
}
