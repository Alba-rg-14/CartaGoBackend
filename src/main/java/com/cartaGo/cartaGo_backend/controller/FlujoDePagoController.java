package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.FlujoDePago.*;
import com.cartaGo.cartaGo_backend.service.MailService;
import com.cartaGo.cartaGo_backend.service.SalaPagoService;
import lombok.Data;
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
    private final MailService mailService;

    @PostMapping("/{restauranteId}/sala-pago")
    public ResponseEntity<SalaCreateResponseDTO> crearSala(
            @PathVariable Integer restauranteId,
            @RequestParam(name = "clienteId") Integer clienteId   // 👈 obligatorio
    ) {
        var dto = salaPagoService.crearSala(restauranteId, clienteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    //Unirse a sala de pago
    @PostMapping("/{restauranteId}/sala-pago/join")
    public ResponseEntity<SalaCreateResponseDTO> unirseASala(
            @PathVariable Integer restauranteId,
            @RequestParam String codigo,
            @RequestParam Integer clienteId
    ) {
        var dto = salaPagoService.unirseASala(restauranteId, codigo, clienteId);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    // Añadir plato a sala de pago
    @PostMapping("/sala-pago/{salaId}/platos")
    public ResponseEntity<AddPlatoResponseDTO> addPlato(
            @PathVariable Integer salaId,
            @RequestBody AddPlatoRequestDTO req
    ) {
        var dto = salaPagoService.addPlato(salaId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Eliminar plato de una sala
    @DeleteMapping("/sala-pago/{salaId}/platos/{platoSalaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlato(
            @PathVariable Integer salaId,
            @PathVariable Integer platoSalaId
    ) {
        salaPagoService.deletePlatoDeSala(salaId, platoSalaId);
    }


    //Actualizar participaciones de un platosala
    @PutMapping("/sala-pago/{salaId}/platos/{platoSalaId}/participantes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void replaceParticipantes(
            @PathVariable Integer salaId,
            @PathVariable Integer platoSalaId,
            @RequestBody ParticipantesRequestDTO req
    ) {
        salaPagoService.replaceParticipantesDeUnPlato(salaId, platoSalaId, req.getClienteIds());
    }


    // Añadir participantes a un plato
    @PostMapping("/sala-pago/{salaId}/platos/{platoSalaId}/participantes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addParticipantes(
            @PathVariable Integer salaId,
            @PathVariable Integer platoSalaId,
            @RequestBody ParticipantesRequestDTO req
    ) {
        salaPagoService.addParticipantesDeUnPlato(salaId, platoSalaId, req.getClienteIds());
    }

    // Eliminar participantes de un plato
    @DeleteMapping("/sala-pago/{salaId}/platos/{platoSalaId}/participantes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeParticipantes(
            @PathVariable Integer salaId,
            @PathVariable Integer platoSalaId,
            @RequestBody ParticipantesRequestDTO req
    ) {
        salaPagoService.removeParticipantesDeUnPlato(salaId, platoSalaId, req.getClienteIds());
    }

    @GetMapping("/sala-pago/{salaId}/resumen")
    public ResponseEntity<SalaResumenDTO> getResumen(@PathVariable Integer salaId) {
        return ResponseEntity.ok(salaPagoService.getResumenSala(salaId));
    }

    @PostMapping("/sala-pago/{salaId}/instrucciones-detalladas")
    public ResponseEntity<InstruccionesPagoDTO> generarInstruccionesDetalladas(
            @PathVariable Integer salaId,
            @RequestParam String modo // "igualitario" | "personalizado"
    ) {
        var dto = salaPagoService.generarInstruccionesDetalladas(salaId, modo);
        return ResponseEntity.ok(dto);
    }


    @PostMapping("/sala-pago/{salaId}/instrucciones-detalladas/email")
    public ResponseEntity<Void> generarYEnviarATodos(
            @PathVariable Integer salaId,
            @RequestParam String modo // "igualitario" | "personalizado"
    ) {
        var dto = salaPagoService.generarInstruccionesDetalladas(salaId, modo); // calcula y CIERRA sala
        mailService.enviarInstruccionesATodos(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sala-pago/{salaId}/instrucciones-detalladas/email/cliente/{clienteId}")
    public ResponseEntity<Void> generarYEnviarASoloUno(
            @PathVariable Integer salaId,
            @PathVariable Integer clienteId,
            @RequestParam String modo
    ) {
        var dto = salaPagoService.generarInstruccionesDetalladas(salaId, modo); // CIERRA sala
        var bloque = dto.getPorCliente().stream()
                .filter(c -> c.getClienteId().equals(clienteId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cliente no participa en la sala"));
        if (bloque.getEmail() == null || bloque.getEmail().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        mailService.enviarInstruccionesACliente(bloque.getEmail(), dto, bloque);
        return ResponseEntity.ok().build();
    }




}
