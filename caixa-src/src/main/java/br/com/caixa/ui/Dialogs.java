package br.com.caixa.ui;

import br.com.caixa.model.Moeda;
import br.com.caixa.model.Produto;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;

/** Caixas de diálogo padronizadas (com o mesmo visual do resto do sistema). */
final class Dialogs {
    private static final ButtonType CONFIRMAR = new ButtonType("CONFIRMAR", ButtonBar.ButtonData.OK_DONE);
    private static final ButtonType CANCELAR = new ButtonType("CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE);

    private Dialogs() {
    }

    static String css() {
        return Dialogs.class.getResource("/style.css").toExternalForm();
    }

    static void aviso(Window owner, String titulo, String mensagem) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensagem, ButtonType.OK);
        a.initOwner(owner);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.getDialogPane().getStylesheets().add(css());
        a.showAndWait();
    }

    static boolean confirmar(Window owner, String titulo, String mensagem) {
        ButtonType sim = new ButtonType("SIM", ButtonBar.ButtonData.YES);
        ButtonType nao = new ButtonType("NÃO", ButtonBar.ButtonData.NO);
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensagem, sim, nao);
        a.initOwner(owner);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.getDialogPane().getStylesheets().add(css());
        return a.showAndWait().filter(b -> b == sim).isPresent();
    }

    /** Pede um número usando o teclado na tela. Retorna o texto digitado (ex.: "0,350"). */
    static Optional<String> pedirNumero(Window owner, String titulo, String instrucao,
                                        boolean decimal, int maxLen) {
        return pedir(owner, titulo, instrucao, decimal, maxLen, false);
    }

    /** Igual ao pedirNumero, mas esconde o que é digitado (bolinhas). */
    static Optional<String> pedirSenha(Window owner, String titulo, String instrucao, int maxLen) {
        return pedir(owner, titulo, instrucao, false, maxLen, true);
    }

    private static Optional<String> pedir(Window owner, String titulo, String instrucao,
                                          boolean decimal, int maxLen, boolean senha) {
        Dialog<String> d = new Dialog<>();
        d.initOwner(owner);
        d.setTitle(titulo);
        d.setHeaderText(instrucao);
        DialogPane p = d.getDialogPane();
        p.getStylesheets().add(css());
        p.getButtonTypes().addAll(CONFIRMAR, CANCELAR);

        TextField campo = senha ? new PasswordField() : new TextField();
        // Permite digitar tanto pelo teclado físico quanto pelo teclado virtual.
        campo.setEditable(true);
        campo.setFocusTraversable(true);
        campo.getStyleClass().addAll("campo", "campo-grande");

        VBox box = new VBox(12, campo, new TecladoNumerico(campo, maxLen, decimal));
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(6));
        p.setContent(box);

        d.setResultConverter(bt -> bt == CONFIRMAR ? campo.getText() : null);
        javafx.application.Platform.runLater(campo::requestFocus);
        return d.showAndWait().filter(s -> !s.isBlank());
    }

    /** Grade com os produtos de hortifruti para o cliente tocar. */
    static Optional<Produto> escolherProduto(Window owner, String titulo, List<Produto> produtos) {
        Dialog<Produto> d = new Dialog<>();
        d.initOwner(owner);
        d.setTitle(titulo);
        d.setHeaderText("Toque no item que você quer comprar");
        DialogPane p = d.getDialogPane();
        p.getStylesheets().add(css());
        p.getButtonTypes().add(CANCELAR);

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.setPadding(new Insets(6));
        int col = 0;
        int lin = 0;
        for (Produto produto : produtos) {
            String sufixo = produto.porKg() ? " /kg" : " /un";
            Button b = new Button(produto.nome() + "\n" + Moeda.formatar(produto.precoCentavos()) + sufixo);
            b.getStyleClass().add("btn-rodape");
            b.setPrefSize(190, 80);
            b.setOnAction(e -> {
                d.setResult(produto);
                d.close();
            });
            grade.add(b, col, lin);
            if (++col == 3) {
                col = 0;
                lin++;
            }
        }
        p.setContent(grade);
        return d.showAndWait();
    }
}
