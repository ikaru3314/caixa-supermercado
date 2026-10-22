package br.com.caixa.ui;

import br.com.caixa.model.Moeda;
import br.com.caixa.model.Produto;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

/** Formulário para adicionar ou editar um produto (com validação dos campos). */
final class FormProduto {
    private static final ButtonType SALVAR = new ButtonType("SALVAR", ButtonBar.ButtonData.OK_DONE);
    private static final ButtonType CANCELAR = new ButtonType("CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE);
    private static final String[] CATEGORIAS =
            {"MERCEARIA", "FLV", "BEBIDAS", "LIMPEZA", "HIGIENE", "PADARIA", "OUTROS"};
    private static final Locale BR = Locale.forLanguageTag("pt-BR");

    private final Produto atual;               // null = produto novo
    private final Predicate<String> jaExiste;  // diz se um código já está cadastrado

    private final TextField codigo = new TextField();
    private final TextField nome = new TextField();
    private final TextField preco = new TextField();
    private final TextField estoque = new TextField();
    private final ComboBox<String> tipo = new ComboBox<>();
    private final ComboBox<String> categoria = new ComboBox<>();
    private final Label erro = new Label();

    private FormProduto(Produto atual, Predicate<String> jaExiste) {
        this.atual = atual;
        this.jaExiste = jaExiste;
    }

    /** Abre o formulário. Retorna o produto preenchido, ou vazio se o usuário cancelar. */
    static Optional<Produto> abrir(Window dono, Produto atual, Predicate<String> jaExiste) {
        return new FormProduto(atual, jaExiste).mostrar(dono);
    }

    private Optional<Produto> mostrar(Window dono) {
        Dialog<Produto> d = new Dialog<>();
        d.initOwner(dono);
        d.setTitle(atual == null ? "Adicionar produto" : "Editar produto");
        d.setHeaderText(atual == null ? "Preencha os dados do novo produto" : "Altere os dados do produto");
        DialogPane p = d.getDialogPane();
        p.getStylesheets().add(Dialogs.css());
        p.getButtonTypes().addAll(SALVAR, CANCELAR);

        tipo.getItems().addAll("Unidade (un)", "Quilo (kg)");
        categoria.getItems().addAll(CATEGORIAS);
        codigo.setPromptText("ex.: 7891000100103");
        nome.setPromptText("ex.: Leite Integral 1L");
        preco.setPromptText("ex.: 5,49");
        estoque.setPromptText("ex.: 100");

        if (atual == null) {
            tipo.getSelectionModel().select(0);
            categoria.getSelectionModel().select(0);
        } else {
            codigo.setText(atual.codigo());
            codigo.setDisable(true); // o código identifica o produto, não muda
            nome.setText(atual.nome());
            preco.setText(String.format(BR, "%.2f", atual.precoCentavos() / 100.0));
            estoque.setText(atual.porKg()
                    ? String.format(BR, "%.3f", atual.estoque())
                    : String.valueOf((long) atual.estoque()));
            tipo.getSelectionModel().select(atual.porKg() ? 1 : 0);
            if (!categoria.getItems().contains(atual.categoria())) {
                categoria.getItems().add(atual.categoria());
            }
            categoria.getSelectionModel().select(atual.categoria());
        }

        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(10);
        g.setPadding(new Insets(6));
        linha(g, 0, "Código de barras", codigo);
        linha(g, 1, "Nome", nome);
        linha(g, 2, "Preço (R$)", preco);
        linha(g, 3, "Vendido por", tipo);
        linha(g, 4, "Categoria", categoria);
        linha(g, 5, "Estoque", estoque);
        tipo.setMaxWidth(Double.MAX_VALUE);
        categoria.setMaxWidth(Double.MAX_VALUE);

        Label dica = new Label("FLV = frutas, legumes e verduras (aparecem no botão F3 do caixa).");
        dica.getStyleClass().add("selo-ia");
        erro.getStyleClass().add("msg-erro");
        erro.setWrapText(true);
        erro.setMaxWidth(420);
        p.setContent(new VBox(8, g, dica, erro));

        // não fecha a janela se houver campo inválido
        Button ok = (Button) p.lookupButton(SALVAR);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            if (ler() == null) {
                ev.consume();
            }
        });
        d.setResultConverter(bt -> bt == SALVAR ? ler() : null);

        Platform.runLater(() -> {
            if (codigo.isDisabled()) {
                nome.requestFocus();
            } else {
                codigo.requestFocus();
            }
        });
        return d.showAndWait();
    }

    private void linha(GridPane g, int linha, String rotulo, javafx.scene.Node campo) {
        Label l = new Label(rotulo);
        l.getStyleClass().add("rotulo-form");
        g.add(l, 0, linha);
        g.add(campo, 1, linha);
        GridPane.setHgrow(campo, javafx.scene.layout.Priority.ALWAYS);
    }

    /** Valida os campos. Devolve o produto, ou null (e mostra o motivo) se algo estiver errado. */
    private Produto ler() {
        String cod = codigo.getText().trim();
        String nm = nome.getText().trim();
        if (cod.isEmpty()) {
            return falha("Informe o código de barras.");
        }
        if (cod.length() > 20) {
            return falha("O código pode ter no máximo 20 caracteres.");
        }
        if (atual == null && jaExiste.test(cod)) {
            return falha("Já existe um produto com o código " + cod + ".");
        }
        if (nm.isEmpty()) {
            return falha("Informe o nome do produto.");
        }
        Optional<Long> centavos = Moeda.parseCentavos(preco.getText());
        if (centavos.isEmpty() || centavos.get() <= 0) {
            return falha("Preço inválido. Exemplo: 12,50");
        }
        Optional<Double> est = Moeda.parseQuantidade(estoque.getText());
        if (est.isEmpty() || est.get() < 0) {
            return falha("Estoque inválido. Exemplo: 100");
        }
        boolean kg = tipo.getSelectionModel().getSelectedIndex() == 1;
        if (!kg && est.get() % 1 != 0) {
            return falha("Para produtos por unidade, o estoque deve ser um número inteiro.");
        }
        erro.setText("");
        return new Produto(cod, nm, centavos.get(), kg, categoria.getValue(), est.get());
    }

    private Produto falha(String mensagem) {
        erro.setText(mensagem);
        return null;
    }
}
