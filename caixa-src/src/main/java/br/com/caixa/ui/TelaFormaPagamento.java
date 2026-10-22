package br.com.caixa.ui;

import br.com.caixa.model.Moeda;
import br.com.caixa.model.Sessao;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;

public class TelaFormaPagamento {
    private final App app;
    private final Sessao sessao;

    public TelaFormaPagamento(App app, Sessao sessao) {
        this.app = app;
        this.sessao = sessao;
    }

    public Parent criar() {
        Label total = new Label("Total a pagar: " + Moeda.formatar(sessao.totalCentavos()));
        total.getStyleClass().add("texto-grande");

        GridPane opcoes = new GridPane();
        opcoes.setHgap(16);
        opcoes.setVgap(16);
        opcoes.setAlignment(Pos.CENTER);
        String[] formas = {"Dinheiro", "Cartão de débito", "Cartão de crédito", "Pix"};
        for (int i = 0; i < formas.length; i++) {
            String forma = formas[i];
            Button b = new Button(forma.toUpperCase());
            b.getStyleClass().add("btn-opcao");
            b.setOnAction(e -> {
                sessao.formaPagamento(forma);
                app.irParaPagamento();
            });
            opcoes.add(b, i % 2, i / 2);
        }

        VBox centro = new VBox(28, total, opcoes);
        centro.setAlignment(Pos.CENTER);
        centro.getStyleClass().add("cartao-etapa");
        centro.setMaxWidth(780);
        StackPane areaCentral = new StackPane(centro);
        areaCentral.getStyleClass().add("area-etapa");

        BorderPane tela = new BorderPane();
        tela.setTop(Ui.topo("ESCOLHA A FORMA DE PAGAMENTO", app::abrirAjuda));
        tela.setCenter(areaCentral);
        tela.setBottom(Ui.barraRodape(Ui.rodape("VOLTAR", "btn-voltar", app::irParaCompra)));
        return tela;
    }
}
