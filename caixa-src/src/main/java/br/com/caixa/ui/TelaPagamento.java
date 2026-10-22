package br.com.caixa.ui;

import br.com.caixa.model.Moeda;
import br.com.caixa.model.Sessao;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;

import java.util.Optional;

/**
 * Tela de pagamento. Nesta versão o pagamento é SIMULADO (protótipo): não há integração
 * com maquininha, PIX real ou cédula. O ponto de integração é o método {@code confirmarPagamento}.
 */
public class TelaPagamento {
    private final App app;
    private final Sessao sessao;

    private final Label status = new Label();
    private final Button confirmar;

    public TelaPagamento(App app, Sessao sessao) {
        this.app = app;
        this.sessao = sessao;
        this.confirmar = Ui.rodape("CONFIRMAR PAGAMENTO", "btn-finalizar", app::irParaFinalizacao);
        this.confirmar.setDisable(true);
    }

    public Parent criar() {
        String forma = sessao.formaPagamento();

        Label total = new Label("Total a pagar: " + Moeda.formatar(sessao.totalCentavos()));
        total.getStyleClass().add("texto-grande");
        Label tipo = new Label("Forma de pagamento: " + forma);
        tipo.getStyleClass().add("texto-medio");

        Node painelForma = switch (forma) {
            case "Dinheiro" -> painelDinheiro();
            case "Pix" -> painelPix();
            default -> painelCartao();
        };

        status.getStyleClass().add("msg-ok");

        VBox centro = new VBox(18, total, tipo, painelForma, status);
        centro.setAlignment(Pos.CENTER);
        centro.getStyleClass().add("cartao-etapa");
        centro.setMaxWidth(800);
        StackPane areaCentral = new StackPane(centro);
        areaCentral.getStyleClass().add("area-etapa");

        BorderPane tela = new BorderPane();
        tela.setTop(Ui.topo("PAGAMENTO", app::abrirAjuda));
        tela.setCenter(areaCentral);
        tela.setBottom(Ui.barraRodape(
                Ui.rodape("VOLTAR", "btn-voltar", app::irParaFormaPagamento),
                confirmar));
        return tela;
    }

    private Node painelDinheiro() {
        Label instrucao = new Label("Informe o valor que você vai entregar");
        instrucao.getStyleClass().add("texto-medio");

        Button informar = new Button("INFORMAR VALOR RECEBIDO");
        informar.getStyleClass().add("btn-opcao");
        informar.setOnAction(e -> {
            Optional<Long> valor = Dialogs.pedirNumero(informar.getScene().getWindow(),
                            "Pagamento em dinheiro", "Valor entregue (ex.: 50,00)", true, 9)
                    .flatMap(Moeda::parseCentavos);
            valor.ifPresent(this::registrarDinheiro);
        });

        Button exato = new Button("VALOR EXATO");
        exato.getStyleClass().add("btn-opcao");
        exato.setOnAction(e -> registrarDinheiro(sessao.totalCentavos()));

        HBox botoes = new HBox(16, informar, exato);
        botoes.setAlignment(Pos.CENTER);
        return new VBox(12, instrucao, botoes);
    }

    private void registrarDinheiro(long valor) {
        if (valor < sessao.totalCentavos()) {
            status.getStyleClass().setAll("msg-erro");
            status.setText("O valor é menor que o total da compra.");
            confirmar.setDisable(true);
            return;
        }
        sessao.valorPagoCentavos(valor);
        status.getStyleClass().setAll("msg-ok");
        status.setText("Valor recebido: " + Moeda.formatar(valor)
                + "   |   Troco: " + Moeda.formatar(sessao.trocoCentavos()));
        confirmar.setDisable(false);
    }

    private Node painelCartao() {
        Label instrucao = new Label("Insira ou aproxime o cartão na maquininha");
        instrucao.getStyleClass().add("texto-medio");
        Button simular = new Button("SIMULAR APROVAÇÃO");
        simular.getStyleClass().add("btn-opcao");
        simular.setOnAction(e -> aprovarSemTroco("Pagamento aprovado!"));
        VBox box = new VBox(12, instrucao, simular);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private Node painelPix() {
        Label qr = new Label("QR CODE PIX\n(simulado)");
        qr.getStyleClass().add("qr-texto");
        StackPane quadro = new StackPane(qr);
        quadro.getStyleClass().add("qr-falso");
        quadro.setMaxSize(170, 170);
        quadro.setMinSize(170, 170);

        Button simular = new Button("SIMULAR PAGAMENTO RECEBIDO");
        simular.getStyleClass().add("btn-opcao");
        simular.setPrefWidth(400);
        simular.setOnAction(e -> aprovarSemTroco("Pix recebido!"));

        VBox box = new VBox(12, quadro, simular);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void aprovarSemTroco(String mensagem) {
        sessao.valorPagoCentavos(sessao.totalCentavos());
        status.getStyleClass().setAll("msg-ok");
        status.setText(mensagem);
        confirmar.setDisable(false);
    }
}
