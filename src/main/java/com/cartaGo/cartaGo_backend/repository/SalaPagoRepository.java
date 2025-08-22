package com.cartaGo.cartaGo_backend.repository;

import com.cartaGo.cartaGo_backend.entity.SalaPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SalaPagoRepository extends JpaRepository<SalaPago, Integer> {

    // por restaurante
    List<SalaPago> findByRestauranteId(Integer restauranteId);

    // por forma de pago (enum)
    List<SalaPago> findByFormaDePago(SalaPago.FormaDePago formaDePago);

    //por estado de la sala (enum)
//    List<SalaPago> findByEstadoSala(SalaPago.EstadoSala estado);

    // por fecha de creación de la sala
    List<SalaPago> findByFechaCreacion(LocalDateTime fechaCreacion);
    List<SalaPago> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    // por saldo
    List<SalaPago> findBySaldoGreaterThan(Double saldo);
    List<SalaPago> findBySaldoLessThan(Double saldo);
}
