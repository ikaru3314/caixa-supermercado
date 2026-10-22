package br.com.caixa.ai;

import java.text.Normalizer;

/** Respostas prontas usadas quando não há chave de API ou a internet falha. */
final class AjudaLocal {
    private AjudaLocal() {
    }

    static String responder(String pergunta) {
        String p = Normalizer.normalize(pergunta.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");

        if (tem(p, "cpf")) {
            return "Na etapa \"Informar CPF\", digite os 11 números no teclado da tela e toque em CONFIRMAR. "
                    + "Se não quiser CPF na nota, toque em NÃO INFORMAR CPF.";
        }
        if (tem(p, "atendente", "funcionario", "humano", "chamar")) {
            return "Sem problema! Chame um funcionário do mercado que esteja perto do caixa e ele vai ajudar você.";
        }
        if (tem(p, "fruta", "legume", "verdura", "peso", "pesar", "balanca", "banana", "tomate")) {
            return "Toque em FRUTAS, LEGUMES E VERDURAS, escolha o item e digite o peso em kg (por exemplo 0,350).";
        }
        if (tem(p, "remover", "tirar", "excluir", "errei", "devolver")) {
            return "Toque no item na lista e depois em \"remover item selecionado\".";
        }
        if (tem(p, "codigo", "barra", "leitor", "escanear", "passar", "nao le", "nao le")) {
            return "Passe o código de barras do produto pelo leitor. Se não ler, toque em DIGITAR CÓDIGO DE BARRAS "
                    + "e digite os números que aparecem abaixo das barras.";
        }
        if (tem(p, "pix", "cartao", "dinheiro", "pagar", "pagamento", "troco", "debito", "credito")) {
            return "Quando terminar de passar os itens, toque em FINALIZAR COMPRA e escolha como pagar: "
                    + "dinheiro, cartão de débito, cartão de crédito ou Pix.";
        }
        if (tem(p, "finalizar", "terminar", "acabar", "concluir")) {
            return "Quando terminar de passar os itens, toque no botão verde FINALIZAR COMPRA.";
        }
        return "Posso ajudar com CPF, leitura de produtos, frutas e verduras e formas de pagamento. "
                + "Se precisar de mais ajuda, chame um funcionário.";
    }

    private static boolean tem(String texto, String... termos) {
        for (String t : termos) {
            if (texto.contains(t)) {
                return true;
            }
        }
        return false;
    }
}
