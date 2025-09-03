package com.cartaGo.cartaGo_backend.dto.CartaDTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenameSeccionRequest {
    private String from;
    private String to;
}
