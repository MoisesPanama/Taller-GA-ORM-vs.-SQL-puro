package ec.edu.uteq.taller;

import java.math.BigDecimal;

public record Producto(
        Long id,
        String nombre,
        BigDecimal precio,
        Integer stock
) {}