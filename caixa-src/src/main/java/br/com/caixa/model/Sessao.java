package br.com.caixa.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** Estado da compra em andamento (carrinho, CPF e pagamento). */
public class Sessao {
    private final ObservableList<ItemVenda> itens = FXCollections.observableArrayList();
    private String cpf;               // somente dígitos, ou null
    private String formaPagamento;    // "Dinheiro", "Cartão de débito", ...
    private long valorPagoCentavos;
    private long vendaId;

    public ObservableList<ItemVenda> itens() {
        return itens;
    }

    /** Adiciona o produto (ou soma na quantidade, se já estiver na lista) e devolve o item. */
    public ItemVenda adicionar(Produto produto, double quantidade) {
        if (!produto.porKg()) {
            for (ItemVenda i : itens) {
                if (i.produto().codigo().equals(produto.codigo())) {
                    i.somar(quantidade);
                    return i;
                }
            }
        }
        ItemVenda novo = new ItemVenda(produto, quantidade);
        itens.add(novo);
        return novo;
    }

    /** Quanto deste produto já está no carrinho (para conferir o estoque). */
    public double quantidadeDe(String codigo) {
        return itens.stream()
                .filter(i -> i.produto().codigo().equals(codigo))
                .mapToDouble(ItemVenda::quantidade)
                .sum();
    }

    public long totalCentavos() {
        return itens.stream().mapToLong(ItemVenda::totalCentavos).sum();
    }

    public long trocoCentavos() {
        return Math.max(0, valorPagoCentavos - totalCentavos());
    }

    public String cpf() {
        return cpf;
    }

    public void cpf(String cpf) {
        this.cpf = cpf;
    }

    public String formaPagamento() {
        return formaPagamento;
    }

    public void formaPagamento(String f) {
        this.formaPagamento = f;
    }

    public long valorPagoCentavos() {
        return valorPagoCentavos;
    }

    public void valorPagoCentavos(long v) {
        this.valorPagoCentavos = v;
    }

    public long vendaId() {
        return vendaId;
    }

    public void vendaId(long id) {
        this.vendaId = id;
    }

    public void limpar() {
        itens.clear();
        cpf = null;
        formaPagamento = null;
        valorPagoCentavos = 0;
        vendaId = 0;
    }
}
