package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.FlujoDePago.DetallePlatoClienteDTO;
import com.cartaGo.cartaGo_backend.dto.FlujoDePago.InstruccionesPagoDTO;
import com.cartaGo.cartaGo_backend.dto.FlujoDePago.LineaInstruccionDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    @Value("${brevo.apiKey}")        private String brevoApiKey;
    @Value("${app.mail.from.name}")  private String fromName;
    @Value("${app.mail.from.email}") private String fromEmail;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String BREVO_ENDPOINT = "https://api.brevo.com/v3/smtp/email";

    @PostConstruct
    public void initBrevo() {
        if (brevoApiKey != null) {
            brevoApiKey = brevoApiKey.trim().replace("\"", "");
        }
        String masked = (brevoApiKey == null || brevoApiKey.isBlank())
                ? "NULL"
                : brevoApiKey.substring(0, Math.min(8, brevoApiKey.length())) + "...";
        log.info("Brevo API key cargada: {}", masked);
        log.info("From configurado: {} <{}>", fromName, fromEmail);
    }

    @GetMapping("/_brevo/ping")
    public ResponseEntity<String> brevoPing() {
        HttpHeaders h = new HttpHeaders();
        h.set("api-key", brevoApiKey);
        h.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(null, h);

        ResponseEntity<String> res = new RestTemplate().exchange(
                "https://api.brevo.com/v3/account",
                HttpMethod.GET,
                entity,
                String.class
        );
        log.info("/v3/account -> status {}", res.getStatusCodeValue());
        return ResponseEntity.status(res.getStatusCode()).body(res.getBody());
    }


    /** Envía a TODOS los clientes de la sala */
    public void enviarInstruccionesATodos(InstruccionesPagoDTO dto) {
        for (LineaInstruccionDTO cli : dto.getPorCliente()) {
            if (cli.getEmail() == null || cli.getEmail().isBlank()) continue;
            try {
                enviarInstruccionesACliente(cli.getEmail(), dto, cli);
            } catch (Exception e) {
                log.error("Fallo enviando a {}: {}", cli.getEmail(), e.getMessage(), e);
            }
        }
    }

    /** Envía a un cliente concreto */
    public void enviarInstruccionesACliente(String to, InstruccionesPagoDTO dto, LineaInstruccionDTO cliente) {
        String subject = "[CartaGo] Tu resumen de pago - " + safe(dto.getRestauranteNombre());
        String html    = renderClienteHtml(dto, cliente);
        String text    = renderClienteTxt(dto, cliente);
        sendViaBrevo(to, subject, html, text);
    }

    /** Método simple compatible con tu código previo */
    public void send(String to, String subject, String text) {
        String html = "<pre style='font-family:monospace;white-space:pre-wrap'>" + escape(text) + "</pre>";
        sendViaBrevo(to, subject, html, text);
    }

    // ===== Implementación Brevo API =====
    private void sendViaBrevo(String to, String subject, String html, String text) {
        RestTemplate rest = new RestTemplate();

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("api-key", brevoApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("email", fromEmail, "name", fromName));
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", subject);
        body.put("htmlContent", html);
        body.put("textContent", text);             // mejor entregabilidad en clientes sin HTML
        body.put("replyTo", Map.of("email", fromEmail, "name", fromName));

        ResponseEntity<String> res = rest.postForEntity(BREVO_ENDPOINT, new HttpEntity<>(body, h), String.class);
        if (!res.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Brevo error: " + res.getStatusCodeValue() + " -> " + res.getBody());
        }
        log.info("Email enviado a {} (status {})", to, res.getStatusCodeValue());
    }

    // ===== Render =====
    private String renderClienteHtml(InstruccionesPagoDTO dto, LineaInstruccionDTO cliente) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family:Inter,Arial,sans-serif;line-height:1.5;'>");
        sb.append("<h2 style='margin:0 0 12px'>Resumen de pago</h2>");
        sb.append("<p style='margin:0'>Restaurante: <strong>").append(esc(dto.getRestauranteNombre())).append("</strong></p>");
        sb.append("<p style='margin:0'>Fecha: <strong>").append(dto.getFechaGeneracion().format(FORMATTER)).append("</strong></p>");
        sb.append("<p style='margin:0 0 12px'>Modo de pago: <strong>").append(esc(dto.getModo())).append("</strong></p>");

        sb.append("<p>Hola ").append(esc(cliente.getClienteNombre())).append(", este es tu resumen:</p>");
        if (cliente.getDetalles() != null && !cliente.getDetalles().isEmpty()) {
            sb.append("<table cellpadding='6' cellspacing='0' style='border-collapse:collapse;width:100%;'>");
            sb.append("<thead><tr>")
                    .append("<th align='left' style='border-bottom:1px solid #ddd'>Plato</th>")
                    .append("<th align='right' style='border-bottom:1px solid #ddd'>Tu parte (€)</th>")
                    .append("<th align='right' style='border-bottom:1px solid #ddd'>Precio plato (€)</th>")
                    .append("</tr></thead><tbody>");
            for (DetallePlatoClienteDTO d : cliente.getDetalles()) {
                sb.append("<tr>")
                        .append("<td>").append(esc(d.getPlatoNombre())).append("</td>")
                        .append("<td align='right'>").append(d.getTuParte()).append("</td>")
                        .append("<td align='right'>").append(d.getPrecioPlato()).append("</td>")
                        .append("</tr>");
            }
            sb.append("</tbody></table>");
        }
        sb.append("<p style='margin-top:16px'><strong>TOTAL A PAGAR: ")
                .append(cliente.getTotalAPagar()).append(" €</strong></p>");
        sb.append("<p style='margin-top:24px'>Gracias por usar CartaGo.</p>");
        sb.append("</div>");
        return sb.toString();
    }

    private String renderClienteTxt(InstruccionesPagoDTO dto, LineaInstruccionDTO cliente) {
        StringBuilder sb = new StringBuilder();
        sb.append("Restaurante: ").append(safe(dto.getRestauranteNombre())).append("\n");
        sb.append("Fecha: ").append(dto.getFechaGeneracion().format(FORMATTER)).append("\n");
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
    private String esc(String s) { return safe(s).replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }
    private String escape(String s) { return esc(s).replace("\"","&quot;").replace("'","&#39;"); }
}
