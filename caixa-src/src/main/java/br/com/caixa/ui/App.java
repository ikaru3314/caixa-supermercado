package br.com.caixa.ui;

import br.com.caixa.ai.AssistenteIA;
import br.com.caixa.db.Database;
import br.com.caixa.model.Moeda;
import br.com.caixa.model.Sessao;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/** Janela principal + navegação entre as etapas do caixa. */
public class App extends Application {
    private final Sessao sessao = new Sessao();
    private final StepsBar etapas = new StepsBar();
    private final BorderPane raiz = new BorderPane();
    private final AssistenteIA assistente = new AssistenteIA();

    private Stage stage;
    private int etapaAtual;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Database.inicializar();

        raiz.setLeft(etapas);
        Scene scene = new Scene(raiz, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });

        stage.setTitle("Caixa de Autoatendimento");
        stage.setMinWidth(1100);
        stage.setMinHeight(660);
        stage.setScene(scene);
        stage.show();

        irParaInicio();
    }

    // ---------------------------------------------------------------- navegação

    public void irParaInicio() {
        sessao.limpar();
        assistente.reiniciar();
        mostrar(0, new TelaInicio(this).criar());
    }

    public void irParaProdutos() {
        mostrar(0, new TelaProdutos(this).criar());
    }

    public void irParaCpf() {
        mostrar(1, new TelaCpf(this, sessao).criar());
    }

    public void irParaCompra() {
        mostrar(2, new TelaCompra(this, sessao).criar());
    }

    public void irParaFormaPagamento() {
        mostrar(3, new TelaFormaPagamento(this, sessao).criar());
    }

    public void irParaPagamento() {
        mostrar(4, new TelaPagamento(this, sessao).criar());
    }

    public void irParaFinalizacao() {
        mostrar(5, new TelaFinalizacao(this, sessao).criar());
    }

    private void mostrar(int etapa, Parent tela) {
        etapaAtual = etapa;
        etapas.definir(etapa);
        tela.setOpacity(0);
        tela.setTranslateY(8);
        raiz.setCenter(tela);
        FadeTransition fade = new FadeTransition(Duration.millis(220), tela);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(220), tela);
        slide.setFromY(8);
        slide.setToY(0);
        new ParallelTransition(fade, slide).play();
    }

    public int etapaAtual() {
        return etapaAtual;
    }

    // ---------------------------------------------------------------- ajuda (IA)

    public void abrirAjuda() {
        new AjudaDialog(stage, assistente, this::contextoParaIA).show();
    }

    /** Resumo do que está acontecendo no caixa, enviado junto com a pergunta do cliente. */
    private String contextoParaIA() {
        return "Etapa atual: " + StepsBar.ETAPAS[etapaAtual]
                + ". Itens no carrinho: " + sessao.itens().size()
                + ". Total: " + Moeda.formatar(sessao.totalCentavos())
                + ". CPF na nota: " + (sessao.cpf() == null ? "não" : "sim")
                + (sessao.formaPagamento() == null ? "" : ". Forma de pagamento: " + sessao.formaPagamento())
                + ".";
    }
}
