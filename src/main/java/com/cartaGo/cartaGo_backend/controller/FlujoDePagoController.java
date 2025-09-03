package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.FlujoDePago.AddPlatoRequestDTO;
import com.cartaGo.cartaGo_backend.dto.FlujoDePago.AddPlatoResponseDTO;
import com.cartaGo.cartaGo_backend.dto.FlujoDePago.SalaCreateResponseDTO;
import com.cartaGo.cartaGo_backend.service.SalaPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/flujo-pago")
public class FlujoDePagoController {

    private final SalaPagoService salaPagoService;

    @PostMapping("/{restauranteId}/sala-pago")
    public ResponseEntity<SalaCreateResponseDTO> crearSala(
            @PathVariable Integer restauranteId,
            @RequestParam(name = "clienteId") Integer clienteId   // 👈 obligatorio
    ) {
        var dto = salaPagoService.crearSala(restauranteId, clienteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/{restauranteId}/sala-pago/join")
    public ResponseEntity<SalaCreateResponseDTO> unirseASala(
            @PathVariable Integer restauranteId,
            @RequestParam String codigo,
            @RequestParam Integer clienteId
    ) {
        var dto = salaPagoService.unirseASala(restauranteId, codigo, clienteId);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    // FlujoDePagoController.java
    @PostMapping("/sala-pago/{salaId}/platos")
    public ResponseEntity<AddPlatoResponseDTO> addPlato(
            @PathVariable Integer salaId,
            @RequestBody AddPlatoRequestDTO req
    ) {
        var dto = salaPagoService.addPlato(salaId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }




}
