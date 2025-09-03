package com.cartaGo.cartaGo_backend.repository;

import com.cartaGo.cartaGo_backend.entity.Cliente;
import com.cartaGo.cartaGo_backend.entity.ParticipacionSala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ParticipacionSalaRepository extends JpaRepository<ParticipacionSala, Integer> {

    // por sala o por cliente (usando id)
    List<ParticipacionSala> findBySalaPagoId(Integer salaPagoId);
    List<ParticipacionSala> findByClienteId(Integer clienteId);

    // participación concreta de un cliente en una sala
    Optional<ParticipacionSala> findBySalaPagoIdAndClienteId(Integer salaPagoId, Integer clienteId);

    // por monto
    List<ParticipacionSala> findByMontoGreaterThan(BigDecimal monto);
    List<ParticipacionSala> findByMontoLessThan(BigDecimal monto);

    // salas de un restaurante
    List<ParticipacionSala> findBySalaPagoRestauranteId(Integer restauranteId);

    // grupos de clientes
    List<ParticipacionSala> findByClienteIdIn(Collection<Integer> clienteIds);
    List<ParticipacionSala> findBySalaPagoIdAndClienteIdIn(Integer salaPagoId, Collection<Integer> clienteIds);
    List<ParticipacionSala> findByClienteIn(Collection<Cliente> clientes);

    boolean existsBySalaPagoIdAndClienteId(Integer id, Integer id1);
}
