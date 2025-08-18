package com.cartaGo.cartaGo_backend.Repository;

import com.cartaGo.cartaGo_backend.Entity.Cliente;
import com.cartaGo.cartaGo_backend.Entity.ParticipacionPlato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipacionPlatoRepository extends JpaRepository<ParticipacionPlato, Cliente> {
}
