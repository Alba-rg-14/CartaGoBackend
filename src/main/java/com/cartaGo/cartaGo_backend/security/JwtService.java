package com.cartaGo.cartaGo_backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final long expiresMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expires-min}") long expiresMinutes
    ) {
        this.algorithm = Algorithm.HMAC256(secret); // usa una clave larga
        this.expiresMinutes = expiresMinutes;
    }

    public String generateAccessToken(Usuario u) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiresMinutes * 60);

        return JWT.create()
                .withSubject(u.getEmail())
                .withClaim("uid", u.getId())
                .withClaim("role", u.getRol().name())          // CLIENTE / RESTAURANTE
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .sign(algorithm);
    }

    public boolean isValid(String token) {
        try { JWT.require(algorithm).build().verify(token); return true; }
        catch (Exception e) { return false; }
    }

    public Integer getUserId(String token) {
        DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
        return jwt.getClaim("uid").asInt();
    }

    public String getRole(String token) {
        DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
        return jwt.getClaim("role").asString();
    }

    public String getSubject(String token) {
        return JWT.require(algorithm).build().verify(token).getSubject();
    }

    public long getExpiresSeconds() { return expiresMinutes * 60; }
}
