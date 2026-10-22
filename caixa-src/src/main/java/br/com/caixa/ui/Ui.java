package br.com.caixa.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/** Pequenos construtores de componentes usados por todas as telas. */
final class Ui {
    private Ui() {
    }

    /** Faixa azul com o título + botão AJUDA, como na tela de referência. */
    static HBox topo(String titulo, Runnable aoAjudar) {
        Label banner = new Label(titulo);
        banner.getStyleClass().add("banner");
        banner.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(banner, Priority.ALWAYS);

        Button ajuda = new Button("AJUDA");
        ajuda.getStyleClass().add("btn-ajuda");
        ajuda.setFocusTraversable(false);
        ajuda.setOnAction(e -> aoAjudar.run());

        HBox topo = new HBox(8, banner, ajuda);
        topo.setPadding(new Insets(12, 14, 10, 14));
        return topo;
    }

    /** Botão do rodapé. {@code classeExtra} pode ser "btn-voltar", "btn-finalizar" ou null. */
    static Button rodape(String texto, String classeExtra, Runnable acao) {
        Button b = new Button(texto);
        b.getStyleClass().add("btn-rodape");
        if (classeExtra != null) {
            b.getStyleClass().add(classeExtra);
        }
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(66);
        b.setFocusTraversable(false);
        HBox.setHgrow(b, Priority.ALWAYS);
        b.setOnAction(e -> acao.run());
        return b;
    }

    static HBox barraRodape(Button... botoes) {
        HBox h = new HBox(10, botoes);
        h.setPadding(new Insets(8, 12, 10, 12));
        return h;
    }
}
