package com.cartaGo.cartaGo_backend.entity.id;

import java.io.Serializable;
import java.util.Objects;

public class PlatoAlergenosId implements Serializable{

    private Integer plato;
    private Integer alergeno;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlatoAlergenosId that)) return false;
        return Objects.equals(plato, that.plato) && Objects.equals(alergeno, that.alergeno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plato, alergeno);
    }
}
