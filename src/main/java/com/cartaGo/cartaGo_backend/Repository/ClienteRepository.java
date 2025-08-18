package com.cartaGo.cartaGo_backend.Repository;

import com.cartaGo.cartaGo_backend.Entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente,Integer > {
}
