package br.com.caixa.ui;

import br.com.caixa.db.VendaDao;
import br.com.caixa.model.Cpf;
import br.com.caixa.model.ItemVenda;
import br.com.caixa.model.Moeda;
import br.com.caixa.model.Sessao;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TelaFinalizacao {
    private static final int ETAPA = 5;

    private final App app;
    private final Sessao sessao;

    public TelaFinalizacao(App app, Sessao sessao) {
        this.app = app;
        this.sessao = sessao;
    }

    public Parent criar() {
        Label titulo = new Label();
        titulo.getStyleClass().add("texto-grande");
        TextArea cupom = new TextArea();
        cupom.setEditable(false);
        cupom.setFocusTraversable(false);
        cupom.getStyleClass().add("cupom");
        cupom.setMaxWidth(440);
        cupom.setPrefRowCount(14);

        try {
            long id = new VendaDao().salvar(sessao);
            sessao.vendaId(id);
            titulo.setText("Compra finalizada! Obrigado!");
            cupom.setText(montarCupom());

            // volta sozinho para o início depois de 30 s
            PauseTransition espera = new PauseTransition(Duration.seconds(30));
            espera.setOnFinished(e -> {
                if (app.etapaAtual() == ETAPA && sessao.vendaId() == id) {
                    app.irParaInicio();
                }
            });
            espera.play();
        } catch (IllegalStateException e) {
            titulo.setText("Não foi possível registrar a venda");
            titulo.getStyleClass().add("msg-erro");
            cupom.setText(e.getMessage() + "\n\nChame um atendente.");
        }

        VBox centro = new VBox(14, titulo, cupom);
        centro.setAlignment(Pos.CENTER);
        centro.getStyleClass().add("cartao-etapa");
        centro.setMaxWidth(700);
        StackPane areaCentral = new StackPane(centro);
        areaCentral.getStyleClass().add("area-etapa");

        BorderPane tela = new BorderPane();
        tela.setTop(Ui.topo("FINALIZAÇÃO", app::abrirAjuda));
        tela.setCenter(areaCentral);
        tela.setBottom(Ui.barraRodape(Ui.rodape("NOVA COMPRA", "btn-finalizar", app::irParaInicio)));
        return tela;
    }

    private String montarCupom() {
        StringBuilder sb = new StringBuilder();
        sb.append("     SUPERMERCADO EXEMPLO\n");
        sb.append(String.format("  Venda nº %06d  %s%n", sessao.vendaId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        sb.append("-".repeat(40)).append('\n');
        for (ItemVenda i : sessao.itens()) {
            sb.append(String.format("%-20.20s %9s %9s%n",
                    i.produto().nome(), i.quantidadeTexto(), Moeda.semSimbolo(i.totalCentavos())));
        }
        sb.append("-".repeat(40)).append('\n');
        sb.append(String.format("%-30s %9s%n", "TOTAL", Moeda.formatar(sessao.totalCentavos())));
        sb.append(String.format("%-30s %9s%n", "Pago (" + sessao.formaPagamento() + ")",
                Moeda.formatar(sessao.valorPagoCentavos())));
        if (sessao.trocoCentavos() > 0) {
            sb.append(String.format("%-30s %9s%n", "Troco", Moeda.formatar(sessao.trocoCentavos())));
        }
        sb.append('\n');
        sb.append(sessao.cpf() == null ? "CPF: não informado" : "CPF: " + cpfParcial(sessao.cpf()));
        return sb.toString();
    }

    /** Mostra só parte do CPF na tela: ***.456.789-** */
    private String cpfParcial(String cpf) {
        String d = Cpf.digitos(cpf);
        return "***." + d.substring(3, 6) + "." + d.substring(6, 9) + "-**";
    }
}
