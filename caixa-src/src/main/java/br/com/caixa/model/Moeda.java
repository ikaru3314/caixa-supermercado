package br.com.caixa.model;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

public final class Moeda {
    private static final Locale BR = Locale.forLanguageTag("pt-BR");

    private Moeda() {
    }

    /** 1234 -> "R$ 12,34" */
    public static String formatar(long centavos) {
        return String.format(BR, "R$ %,.2f", centavos / 100.0);
    }

    /** 1234 -> "12,34" */
    public static String semSimbolo(long centavos) {
        return String.format(BR, "%,.2f", centavos / 100.0);
    }

    /** "12,50" ou "12.50" -> 1250 centavos. Retorna vazio se for inválido. */
    public static Optional<Long> parseCentavos(String texto) {
        try {
            String t = texto.trim();
            if (t.contains(",")) {
                t = t.replace(".", "").replace(',', '.'); // 1.234,56 -> 1234.56
            }
            BigDecimal v = new BigDecimal(t);
            return Optional.of(v.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).longValueExact());
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** "0,350" -> 0.35. Retorna vazio se for inválido. */
    public static Optional<Double> parseQuantidade(String texto) {
        try {
            return Optional.of(Double.parseDouble(texto.trim().replace(',', '.')));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
