package br.com.caixa.ui;

import br.com.caixa.Config;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

public class TelaInicio {
    private final App app;

    public TelaInicio(App app) {
        this.app = app;
    }

    public Parent criar() {
        Label eyebrow = new Label("CAIXA DE AUTOATENDIMENTO");
        eyebrow.getStyleClass().add("inicio-eyebrow");

        Label boasVindas = new Label("Suas compras, do seu jeito.");
        boasVindas.getStyleClass().add("inicio-titulo");
        boasVindas.setWrapText(true);

        Label instrucao = new Label("Passe seus produtos, confira o total e finalize o pagamento com praticidade.");
        instrucao.getStyleClass().add("inicio-descricao");
        instrucao.setWrapText(true);
        instrucao.setMaxWidth(620);
        instrucao.setAlignment(Pos.CENTER);

        Button comecar = new Button("INICIAR MINHA COMPRA   →");
        comecar.getStyleClass().add("btn-grande");
        comecar.setOnAction(e -> app.irParaCpf());
        comecar.setMaxWidth(480);
        comecar.setPrefWidth(480);

        Label passo1 = new Label("✓  Identifique-se");
        passo1.getStyleClass().add("inicio-passo");
        Label passo2 = new Label("✓  Adicione produtos");
        passo2.getStyleClass().add("inicio-passo");
        Label passo3 = new Label("✓  Escolha como pagar");
        passo3.getStyleClass().add("inicio-passo");
        HBox passos = new HBox(18, passo1, passo2, passo3);
        passos.setAlignment(Pos.CENTER);
        passos.setStyle("-fx-padding: 8 0 0 0;");

        VBox conteudo = new VBox(18, eyebrow, boasVindas, instrucao, comecar, passos);
        conteudo.setAlignment(Pos.CENTER);
        conteudo.setMaxWidth(700);
        VBox.setVgrow(conteudo, Priority.NEVER);

        // Cartão inicial sem ilustração de carrinho, com conteúdo centralizado.
        VBox cartao = new VBox(conteudo);
        cartao.getStyleClass().add("inicio-cartao");
        cartao.setAlignment(Pos.CENTER);
        cartao.setMaxWidth(900);
        cartao.setPadding(new Insets(58, 64, 58, 64));

        VBox areaCentral = new VBox(cartao);
        areaCentral.setAlignment(Pos.CENTER);
        areaCentral.setPadding(new Insets(30, 36, 30, 36));
        VBox.setVgrow(areaCentral, Priority.ALWAYS);

        Button gerente = new Button("⚙  ÁREA DO GERENTE");
        gerente.getStyleClass().add("btn-gerente");
        gerente.setFocusTraversable(false);
        gerente.setOnAction(e -> pedirSenha(gerente));
        HBox rodape = new HBox(gerente);
        rodape.setAlignment(Pos.CENTER_RIGHT);
        rodape.setPadding(new Insets(8, 20, 14, 20));

        BorderPane tela = new BorderPane();
        tela.getStyleClass().add("tela-inicio");
        tela.setTop(Ui.topo("BEM-VINDO AO AUTOATENDIMENTO", app::abrirAjuda));
        tela.setCenter(areaCentral);
        tela.setBottom(rodape);
        return tela;
    }

    private void pedirSenha(Button origem) {
        var janela = origem.getScene().getWindow();
        Dialogs.pedirSenha(janela, "Área do gerente", "Digite a senha do gerente", 8).ifPresent(senha -> {
            if (senha.equals(Config.PIN_GERENTE)) {
                app.irParaProdutos();
            } else {
                Dialogs.aviso(janela, "Acesso negado", "Senha incorreta.");
            }
        });
    }
}
