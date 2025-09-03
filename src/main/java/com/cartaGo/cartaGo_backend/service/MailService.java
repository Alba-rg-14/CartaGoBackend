package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.FlujoDePago.DetallePlatoClienteDTO;
import com.cartaGo.cartaGo_backend.dto.FlujoDePago.InstruccionesPagoDTO;
import com.cartaGo.cartaGo_backend.dto.FlujoDePago.LineaInstruccionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    /** Envía a TODOS los clientes de la sala, usando el email que venga en cada LineaInstruccionDTO */
    public void enviarInstruccionesATodos(InstruccionesPagoDTO dto) {
        for (LineaInstruccionDTO cli : dto.getPorCliente()) {
            if (cli.getEmail() == null || cli.getEmail().isBlank()) continue; // sin email → lo saltamos
            enviarInstruccionesACliente(cli.getEmail(), dto, cli);
        }
    }

    /** Envía a un cliente concreto */
    public void enviarInstruccionesACliente(String to, InstruccionesPagoDTO dto, LineaInstruccionDTO cliente) {
        String asunto = "[CartaGo] Tu resumen de pago - " + safe(dto.getRestauranteNombre());
        String cuerpo  = renderCliente(dto, cliente);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(asunto);
        msg.setText(cuerpo);
        mailSender.send(msg);
    }

    private String renderCliente(InstruccionesPagoDTO dto, LineaInstruccionDTO cliente) {
        StringBuilder sb = new StringBuilder();
        sb.append("Restaurante: ").append(safe(dto.getRestauranteNombre())).append("\n");
        sb.append("Fecha: ").append(dto.getFechaGeneracion()).append("\n");
        sb.append("Modo de pago: ").append(safe(dto.getModo())).append("\n");
        sb.append("Total de la sala: ").append(dto.getSubtotal()).append(" €\n\n");

        sb.append("Hola ").append(safe(cliente.getClienteNombre())).append(", este es tu resumen:\n");
        if (cliente.getDetalles() != null) {
            for (DetallePlatoClienteDTO d : cliente.getDetalles()) {
                sb.append(" - ").append(safe(d.getPlatoNombre()))
                        .append(" → tu parte: ").append(d.getTuParte()).append(" €")
                        .append(" (precio plato: ").append(d.getPrecioPlato()).append(" €)")
                        .append("\n");
            }
        }
        sb.append("\nTOTAL A PAGAR: ").append(cliente.getTotalAPagar()).append(" €\n");
        sb.append("\nGracias por usar CartaGo.");
        return sb.toString();
    }

    private String safe(String s) { return s == null ? "" : s; }
}
