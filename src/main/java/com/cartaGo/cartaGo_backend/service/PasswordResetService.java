// src/main/java/com/cartaGo/cartaGo_backend/service/PasswordResetService.java
package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UsuarioRepository usuarioRepo;
    private final UsuarioService usuarioService; // ya tienes forceSetPasswordByEmail
    private final MailService mailService;       // ya te funciona en otros puntos

    // Config
    private static final int CODE_LEN = 6;
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(45);

    // Cache en memoria: un único código “vigente” por email
    private final Map<String, CodeEntry> cache = new ConcurrentHashMap<>();

    public void sendCode(String email) {
        // Devolvemos 204 SIEMPRE (no filtramos si existe o no)
        Optional<Usuario> ou = usuarioRepo.findByEmailIgnoreCase(email);
        if (ou.isEmpty()) return;

        String key = email.toLowerCase();
        CodeEntry last = cache.get(key);
        LocalDateTime now = LocalDateTime.now();

        // Rate limit sencillo
        if (last != null && last.createdAt.isAfter(now.minus(RESEND_COOLDOWN))) {
            return; // 204 igualmente
        }

        // Generar y guardar
        String code = generateNumericCode(CODE_LEN);
        CodeEntry entry = new CodeEntry(code, now, now.plus(TTL), false);
        cache.put(key, entry);

        // Email en texto plano
        String subject = "Tu código de recuperación";
        String body = """
                Estás recuperando tu contraseña en CartaGo.

                Tu código es: %s

                Este código caduca en %d minutos.
                Si no lo has solicitado, ignora este mensaje.
                """.formatted(code, TTL.toMinutes());

        mailService.send(email, subject, body);
    }

    public void confirm(String email, String code, String newPassword) {
        String key = email.toLowerCase();
        CodeEntry entry = cache.get(key);
        LocalDateTime now = LocalDateTime.now();

        if (entry == null || entry.consumed || isExpired(entry, now) || !entry.code.equals(code)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código inválido o caducado");
        }

        entry.consumed = true;
        cache.remove(key);

        usuarioService.forceSetPasswordByEmail(email, newPassword);
    }

    private boolean isExpired(CodeEntry e, LocalDateTime now) {
        return e.expiresAt.isBefore(now);
    }

    private String generateNumericCode(int len) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(r.nextInt(0, 10));
        return sb.toString();
    }


    private static class CodeEntry {
        final String code;
        final LocalDateTime createdAt;
        final LocalDateTime expiresAt;
        volatile boolean consumed;
        CodeEntry(String code, LocalDateTime createdAt, LocalDateTime expiresAt, boolean consumed) {
            this.code = code;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.consumed = consumed;
        }
    }
}
