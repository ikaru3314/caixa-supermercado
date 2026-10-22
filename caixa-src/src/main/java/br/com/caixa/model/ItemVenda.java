package br.com.caixa.model;

import java.util.Locale;

public class ItemVenda {
    private final Produto produto;
    private double quantidade;

    public ItemVenda(Produto produto, double quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public Produto produto() {
        return produto;
    }

    public double quantidade() {
        return quantidade;
    }

    public void somar(double q) {
        this.quantidade += q;
    }

    public long totalCentavos() {
        return Math.round(produto.precoCentavos() * quantidade);
    }

    /** "3" para unidades, "0,350 kg" para itens pesados. */
    public String quantidadeTexto() {
        if (produto.porKg()) {
            return String.format(Locale.forLanguageTag("pt-BR"), "%.3f kg", quantidade);
        }
        return String.valueOf((long) quantidade);
    }
}
