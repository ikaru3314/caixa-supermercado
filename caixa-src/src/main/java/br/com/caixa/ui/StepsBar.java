package br.com.caixa.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Navegação vertical lateral das etapas do autoatendimento. */
public class StepsBar extends VBox {
    public static final String[] ETAPAS = {
            "Início", "Informar CPF", "Efetuar compra", "Forma de pagamento", "Pagamento", "Finalização"
    };

    private final Label[] labels = new Label[ETAPAS.length];

    public StepsBar() {
        getStyleClass().add("etapas-lateral");
        Label marca = new Label("FreshCart");
        marca.getStyleClass().add("etapas-marca");
        Label subtitulo = new Label("CAIXA DE\nAUTOATENDIMENTO");
        subtitulo.getStyleClass().add("etapas-subtitulo");
        VBox marcaBox = new VBox(5, marca, subtitulo);
        marcaBox.getStyleClass().add("etapas-cabecalho");
        getChildren().add(marcaBox);

        Label secao = new Label("ETAPAS DA COMPRA");
        secao.getStyleClass().add("etapas-secao");
        getChildren().add(secao);

        for (int i = 0; i < ETAPAS.length; i++) {
            Label l = new Label(String.format("%02d   %s", i + 1, ETAPAS[i]));
            l.getStyleClass().add("etapa");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setMinHeight(48);
            l.setAlignment(Pos.CENTER_LEFT);
            l.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
            l.setWrapText(false);
            VBox.setVgrow(l, Priority.NEVER);
            labels[i] = l;
            getChildren().add(l);
        }

        Label rodape = new Label("Compras simples.\nPagamento seguro.");
        rodape.getStyleClass().add("etapas-rodape");
        VBox.setVgrow(rodape, Priority.ALWAYS);
        getChildren().add(rodape);
    }

    public void definir(int etapa) {
        for (int i = 0; i < labels.length; i++) {
            labels[i].getStyleClass().remove("etapa-atual");
            if (i == etapa) labels[i].getStyleClass().add("etapa-atual");
        }
    }
}
