package br.com.caixa;

import br.com.caixa.ui.App;
import javafx.application.Application;

/**
 * Classe de entrada. Ela existe separada de {@link App} para o JavaFX
 * funcionar ao rodar direto pelo IntelliJ sem configurar module-path.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
