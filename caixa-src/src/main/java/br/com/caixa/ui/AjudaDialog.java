package br.com.caixa.ui;

import br.com.caixa.ai.AssistenteIA;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Supplier;

/** Janela do assistente de IA aberta pelo botão AJUDA. */
public class AjudaDialog extends Stage {
    private final AssistenteIA ia;
    private final Supplier<String> contexto;
    private final VBox mensagens = new VBox(10);
    private final ScrollPane rolagem = new ScrollPane(mensagens);
    private final TextField entrada = new TextField();
    private final Button enviar = new Button("ENVIAR");

    public AjudaDialog(Window dono, AssistenteIA ia, Supplier<String> contexto) {
        this.ia = ia;
        this.contexto = contexto;
        initOwner(dono);
        initModality(Modality.WINDOW_MODAL);
        setTitle("Assistente de ajuda");

        mensagens.setPadding(new Insets(12));
        rolagem.setFitToWidth(true);
        rolagem.getStyleClass().add("chat-fundo");
        mensagens.getStyleClass().add("chat-fundo");
        mensagens.heightProperty().addListener((o, a, b) -> rolagem.setVvalue(1.0));

        Label selo = new Label(ia.usaNuvem()
                ? "Respostas geradas por IA (Claude)"
                : "Modo offline: respostas prontas (defina ANTHROPIC_API_KEY para ativar a IA)");
        selo.getStyleClass().add("selo-ia");

        FlowPane sugestoes = new FlowPane(8, 8);
        for (String s : new String[]{"Como informar o CPF?", "Como pesar frutas?", "O leitor não lê o código",
                "Como pagar?", "Chamar atendente"}) {
            Button chip = new Button(s);
            chip.getStyleClass().add("chip");
            chip.setFocusTraversable(false);
            chip.setOnAction(e -> enviarPergunta(s));
            sugestoes.getChildren().add(chip);
        }
        VBox topo = new VBox(8, selo, sugestoes);
        topo.setPadding(new Insets(10, 12, 6, 12));

        entrada.setPromptText("Digite sua dúvida...");
        entrada.setOnAction(e -> enviarPergunta(entrada.getText()));
        HBox.setHgrow(entrada, Priority.ALWAYS);
        enviar.getStyleClass().add("btn-enviar");
        enviar.setOnAction(e -> enviarPergunta(entrada.getText()));
        Button fechar = new Button("FECHAR");
        fechar.getStyleClass().add("btn-fechar");
        fechar.setOnAction(e -> close());
        HBox rodape = new HBox(8, entrada, enviar, fechar);
        rodape.setAlignment(Pos.CENTER);
        rodape.setPadding(new Insets(8, 12, 12, 12));

        BorderPane raiz = new BorderPane();
        raiz.setTop(topo);
        raiz.setCenter(rolagem);
        raiz.setBottom(rodape);

        Scene cena = new Scene(raiz, 540, 640);
        cena.getStylesheets().add(Dialogs.css());
        setScene(cena);

        bolha("Olá! Sou o assistente do caixa. Como posso ajudar?", false);
    }

    private void enviarPergunta(String texto) {
        String pergunta = texto == null ? "" : texto.trim();
        if (pergunta.isEmpty() || enviar.isDisabled()) {
            return;
        }
        entrada.clear();
        bolha(pergunta, true);
        Label resposta = bolha("Digitando...", false);
        bloquear(true);

        String ctx = contexto.get(); // lido na thread da interface
        Task<String> tarefa = new Task<>() {
            @Override
            protected String call() {
                return ia.perguntar(pergunta, ctx);
            }
        };
        tarefa.setOnSucceeded(e -> {
            resposta.setText(tarefa.getValue());
            bloquear(false);
        });
        tarefa.setOnFailed(e -> {
            resposta.setText("Desculpe, não consegui responder agora. Chame um funcionário, por favor.");
            bloquear(false);
        });
        Thread t = new Thread(tarefa, "assistente-ia");
        t.setDaemon(true);
        t.start();
    }

    private void bloquear(boolean ocupado) {
        enviar.setDisable(ocupado);
        entrada.setDisable(ocupado);
        if (!ocupado) {
            entrada.requestFocus();
        }
    }

    private Label bolha(String texto, boolean doUsuario) {
        Label l = new Label(texto);
        l.setWrapText(true);
        l.setMaxWidth(380);
        l.getStyleClass().add(doUsuario ? "bolha-usuario" : "bolha-ia");
        HBox linha = new HBox(l);
        linha.setAlignment(doUsuario ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        mensagens.getChildren().add(linha);
        return l;
    }
}
