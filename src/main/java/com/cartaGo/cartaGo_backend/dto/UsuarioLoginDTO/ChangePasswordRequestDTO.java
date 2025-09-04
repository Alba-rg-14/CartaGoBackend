package com.cartaGo.cartaGo_backend.dto.UsuarioLoginDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequestDTO {
    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;
}
