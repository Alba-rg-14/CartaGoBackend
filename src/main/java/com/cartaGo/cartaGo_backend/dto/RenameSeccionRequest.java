package com.cartaGo.cartaGo_backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenameSeccionRequest {
    private String from;
    private String to;
}
