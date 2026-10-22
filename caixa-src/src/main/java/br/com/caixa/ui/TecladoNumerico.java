package br.com.caixa.ui;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/** Teclado na tela (para telas de toque) que escreve num TextField. */
public class TecladoNumerico extends GridPane {

    public TecladoNumerico(TextField campo, int maxLen, boolean decimal) {
        setHgap(8);
        setVgap(8);
        String[] teclas = {
                "1", "2", "3",
                "4", "5", "6",
                "7", "8", "9",
                decimal ? "," : "C", "0", "⌫"
        };
        for (int i = 0; i < teclas.length; i++) {
            String t = teclas[i];
            Button b = new Button(t);
            b.getStyleClass().add("tecla");
            b.setFocusTraversable(false);
            b.setOnAction(e -> pressionar(campo, t, maxLen, decimal));
            add(b, i % 3, i / 3);
        }
    }

    private void pressionar(TextField campo, String tecla, int maxLen, boolean decimal) {
        String txt = campo.getText();
        switch (tecla) {
            case "⌫" -> {
                if (!txt.isEmpty()) {
                    campo.setText(txt.substring(0, txt.length() - 1));
                }
            }
            case "C" -> campo.setText("");
            case "," -> {
                if (decimal && !txt.contains(",")) {
                    campo.setText((txt.isEmpty() ? "0" : txt) + ",");
                }
            }
            default -> {
                int tamanho = decimal ? txt.length() : txt.replaceAll("\\D", "").length();
                if (tamanho < maxLen) {
                    campo.setText(txt + tecla);
                }
            }
        }
    }
}
