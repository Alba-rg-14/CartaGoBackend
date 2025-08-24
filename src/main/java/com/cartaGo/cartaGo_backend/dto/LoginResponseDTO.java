package com.cartaGo.cartaGo_backend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private String tokenType; // "Bearer"
    private long   expiresIn; // segundos
    private Integer userId;
    private String role;
}
