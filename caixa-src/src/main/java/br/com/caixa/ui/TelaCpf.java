package br.com.caixa.ui;

import br.com.caixa.model.Cpf;
import br.com.caixa.model.Sessao;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;

public class TelaCpf {
    private final App app;
    private final Sessao sessao;

    public TelaCpf(App app, Sessao sessao) {
        this.app = app;
        this.sessao = sessao;
    }

    public Parent criar() {
        Label pergunta = new Label("Deseja CPF na nota?");
        pergunta.getStyleClass().add("texto-medio");

        Label erro = new Label();
        erro.getStyleClass().add("msg-erro");
        erro.setWrapText(true);

        // Campo editável: funciona com o teclado do computador E com o teclado da tela.
        TextField campo = new TextField(Cpf.mascara(sessao.cpf()));
        campo.setPromptText("000.000.000-00");
        campo.setMaxWidth(420);
        campo.getStyleClass().addAll("campo", "campo-grande");
        campo.textProperty().addListener((obs, antigo, novo) -> {
            String formatado = Cpf.mascara(novo); // só números, com pontos e traço
            if (!formatado.equals(novo)) {
                campo.setText(formatado);
            }
            campo.positionCaret(campo.getLength());
            erro.setText("");
        });

        Runnable confirmar = () -> {
            if (Cpf.aceito(campo.getText())) {
                sessao.cpf(Cpf.digitos(campo.getText()));
                app.irParaCompra();
            } else {
                erro.setText(Cpf.VALIDAR_DIGITOS
                        ? "CPF inválido. Confira os números ou toque em \"Não informar CPF\"."
                        : "Digite os 11 números do CPF ou toque em \"Não informar CPF\".");
            }
        };
        campo.setOnAction(e -> confirmar.run()); // Enter confirma

        VBox centro = new VBox(12, pergunta, campo, erro, new TecladoNumerico(campo, 11, false));
        centro.setAlignment(Pos.CENTER);
        centro.getStyleClass().add("cartao-etapa");
        centro.setMaxWidth(650);
        StackPane areaCentral = new StackPane(centro);
        areaCentral.getStyleClass().add("area-etapa");

        // Se já existe compra em andamento, VOLTAR retorna para ela (sem apagar os itens).
        Runnable voltar = () -> {
            if (sessao.itens().isEmpty()) {
                app.irParaInicio();
            } else {
                app.irParaCompra();
            }
        };

        BorderPane tela = new BorderPane();
        tela.setTop(Ui.topo("INFORME SEU CPF", app::abrirAjuda));
        tela.setCenter(areaCentral);
        tela.setBottom(Ui.barraRodape(
                Ui.rodape("VOLTAR", "btn-voltar", voltar),
                Ui.rodape("NÃO INFORMAR CPF", null, () -> {
                    sessao.cpf(null);
                    app.irParaCompra();
                }),
                Ui.rodape("CONFIRMAR", "btn-finalizar", confirmar)));

        Platform.runLater(campo::requestFocus);
        return tela;
    }
}
