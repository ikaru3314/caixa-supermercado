package br.com.caixa.model;

import java.util.Locale;

/**
 * Produto cadastrado. O preço fica em centavos para evitar erros de arredondamento.
 *
 * @param porKg     true quando o preço é por quilo (precisa de pesagem)
 * @param categoria "MERCEARIA", "FLV" (frutas, legumes e verduras), etc.
 * @param estoque   quantidade disponível (unidades, ou kg quando porKg)
 */
public record Produto(String codigo, String nome, long precoCentavos, boolean porKg,
                      String categoria, double estoque) {

    public String unidade() {
        return porKg ? "kg" : "un";
    }

    /** "100" para unidades, "12,500 kg" para itens pesados. */
    public String estoqueTexto() {
        if (porKg) {
            return String.format(Locale.forLanguageTag("pt-BR"), "%.3f kg", estoque);
        }
        return String.valueOf((long) estoque);
    }
}
