package br.com.caixa.ui;

import br.com.caixa.db.ProdutoDao;
import br.com.caixa.model.Moeda;
import br.com.caixa.model.Produto;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.stage.Window;

import java.util.function.Function;

/** Área do gerente: cadastro de produtos ligado ao banco (adicionar, editar e excluir). */
public class TelaProdutos {
    private final App app;
    private final ProdutoDao dao = new ProdutoDao();

    private final ObservableList<Produto> dados = FXCollections.observableArrayList();
    private final TableView<Produto> tabela = new TableView<>();
    private final TextField busca = new TextField();
    private final Label contagem = new Label();
    private final Label msg = new Label();

    public TelaProdutos(App app) {
        this.app = app;
    }

    public Parent criar() {
        busca.setPromptText("Buscar por nome ou código...");
        busca.getStyleClass().add("campo");
        HBox.setHgrow(busca, Priority.ALWAYS);
        busca.textProperty().addListener((obs, antigo, novo) -> recarregar());

        contagem.getStyleClass().add("produtos-contagem");
        HBox linhaBusca = new HBox(14, busca, contagem);
        linhaBusca.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        linhaBusca.getStyleClass().add("produtos-busca-linha");

        configurarTabela();
        VBox.setVgrow(tabela, Priority.ALWAYS);
        tabela.getStyleClass().add("produtos-tabela");

        msg.getStyleClass().add("msg-ok");
        msg.setWrapText(true);

        Label subtitulo = new Label("Visão geral do catálogo");
        subtitulo.getStyleClass().add("produtos-subtitulo");
        Label descricao = new Label("Consulte o estoque e mantenha os dados dos produtos atualizados.");
        descricao.getStyleClass().add("produtos-descricao");
        VBox introducao = new VBox(4, subtitulo, descricao);

        Label totalValor = new Label("—");
        Label totalTitulo = new Label("PRODUTOS CADASTRADOS");
        Label estoqueTitulo = new Label("ITENS COM ESTOQUE BAIXO");
        Label categoriasTitulo = new Label("CATEGORIAS NO CATÁLOGO");
        Label estoqueValor = new Label("—");
        Label categoriasValor = new Label("—");
        totalValor.getStyleClass().add("produto-metrica-valor");
        estoqueValor.getStyleClass().add("produto-metrica-valor");
        categoriasValor.getStyleClass().add("produto-metrica-valor");
        totalTitulo.getStyleClass().add("produto-metrica-titulo");
        estoqueTitulo.getStyleClass().add("produto-metrica-titulo");
        categoriasTitulo.getStyleClass().add("produto-metrica-titulo");
        VBox cardTotal = new VBox(8, totalTitulo, totalValor);
        VBox cardEstoque = new VBox(8, estoqueTitulo, estoqueValor);
        VBox cardCategorias = new VBox(8, categoriasTitulo, categoriasValor);
        cardTotal.getStyleClass().addAll("produto-metrica", "produto-metrica-azul");
        cardEstoque.getStyleClass().addAll("produto-metrica", "produto-metrica-verde");
        cardCategorias.getStyleClass().addAll("produto-metrica", "produto-metrica-roxo");
        GridPane metricas = new GridPane();
        metricas.setHgap(12);
        ColumnConstraints mc1 = new ColumnConstraints(); mc1.setPercentWidth(33.333);
        ColumnConstraints mc2 = new ColumnConstraints(); mc2.setPercentWidth(33.333);
        ColumnConstraints mc3 = new ColumnConstraints(); mc3.setPercentWidth(33.334);
        metricas.getColumnConstraints().addAll(mc1, mc2, mc3);
        metricas.add(cardTotal, 0, 0); metricas.add(cardEstoque, 1, 0); metricas.add(cardCategorias, 2, 0);
        metricas.setMaxWidth(Double.MAX_VALUE);

        VBox tabelaCard = new VBox(10, linhaBusca, tabela, msg);
        tabelaCard.getStyleClass().add("produtos-tabela-card");
        VBox.setVgrow(tabela, Priority.ALWAYS);
        VBox centro = new VBox(16, introducao, metricas, tabelaCard);
        centro.getStyleClass().add("conteudo-gerente");
        centro.setPadding(new Insets(16, 24, 16, 24));
        VBox.setVgrow(tabelaCard, Priority.ALWAYS);

        BorderPane tela = new BorderPane();
        tela.setTop(Ui.topo("GERENCIAR PRODUTOS", app::abrirAjuda));
        tela.setCenter(centro);
        tela.getStyleClass().add("tela-produtos");
        tela.setBottom(Ui.barraRodape(
                Ui.rodape("VOLTAR", "btn-voltar", app::irParaInicio),
                Ui.rodape("ADICIONAR\nPRODUTO", "btn-finalizar", this::adicionar),
                Ui.rodape("EDITAR\nPRODUTO", null, this::editar),
                Ui.rodape("EXCLUIR\nPRODUTO", "btn-voltar", this::excluir)));

        recarregar();
        return tela;
    }

    private void configurarTabela() {
        tabela.setItems(dados);
        tabela.setPlaceholder(new Label("Nenhum produto encontrado"));
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.getColumns().add(coluna("CÓDIGO", Produto::codigo, "CENTER-LEFT", 140));
        tabela.getColumns().add(coluna("NOME", Produto::nome, "CENTER-LEFT", 280));
        tabela.getColumns().add(coluna("PREÇO", p -> Moeda.formatar(p.precoCentavos()), "CENTER-RIGHT", 100));
        tabela.getColumns().add(coluna("UNID.", Produto::unidade, "CENTER", 70));
        tabela.getColumns().add(coluna("CATEGORIA", Produto::categoria, "CENTER-LEFT", 120));
        tabela.getColumns().add(coluna("ESTOQUE", Produto::estoqueTexto, "CENTER-RIGHT", 110));
        // duplo clique = editar
        tabela.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                editar();
            }
        });
    }

    private TableColumn<Produto, String> coluna(String titulo, Function<Produto, String> valor,
                                                String alinhamento, double largura) {
        TableColumn<Produto, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(cd -> new ReadOnlyStringWrapper(valor.apply(cd.getValue())));
        c.setSortable(false);
        c.setPrefWidth(largura);
        c.setStyle("-fx-alignment: " + alinhamento + ";");
        return c;
    }

    // ---------------------------------------------------------------- ações (ligadas ao banco)

    private void recarregar() {
        try {
            dados.setAll(dao.listarTodos(busca.getText()));
            contagem.setText(dados.size() + (dados.size() == 1 ? " produto" : " produtos"));
            atualizarMetricas();
        } catch (IllegalStateException e) {
            erro(e.getMessage());
        }
    }


    private void atualizarMetricas() {
        // Os cartões são atualizados a partir dos produtos atualmente carregados.
        VBox centro = (VBox) tabela.getParent().getParent();
        if (centro.getChildren().size() < 2 || !(centro.getChildren().get(1) instanceof GridPane grid)) return;
        int total = dados.size();
        long baixo = dados.stream().filter(p -> p.estoque() <= (p.porKg() ? 5.0 : 10.0)).count();
        long categorias = dados.stream().map(Produto::categoria).filter(c -> c != null && !c.isBlank()).distinct().count();
        ((Label) ((VBox) grid.getChildren().get(0)).getChildren().get(1)).setText(String.valueOf(total));
        ((Label) ((VBox) grid.getChildren().get(1)).getChildren().get(1)).setText(String.valueOf(baixo));
        ((Label) ((VBox) grid.getChildren().get(2)).getChildren().get(1)).setText(String.valueOf(categorias));
    }

    private void adicionar() {
        FormProduto.abrir(janela(), null, dao::existe).ifPresent(p -> {
            try {
                dao.inserir(p);
                recarregar();
                ok("Produto adicionado: " + p.nome());
            } catch (IllegalStateException e) {
                erro(e.getMessage());
            }
        });
    }

    private void editar() {
        Produto selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            erro("Selecione um produto na lista.");
            return;
        }
        FormProduto.abrir(janela(), selecionado, codigo -> false).ifPresent(p -> {
            try {
                dao.atualizar(p);
                recarregar();
                ok("Produto alterado: " + p.nome());
            } catch (IllegalStateException e) {
                erro(e.getMessage());
            }
        });
    }

    private void excluir() {
        Produto selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            erro("Selecione um produto na lista.");
            return;
        }
        boolean sim = Dialogs.confirmar(janela(), "Excluir produto",
                "Excluir \"" + selecionado.nome() + "\" (código " + selecionado.codigo() + ")?\n"
                        + "Essa ação não pode ser desfeita. O histórico de vendas é mantido.");
        if (!sim) {
            return;
        }
        try {
            dao.excluir(selecionado.codigo());
            recarregar();
            ok("Produto excluído: " + selecionado.nome());
        } catch (IllegalStateException e) {
            erro(e.getMessage());
        }
    }

    private void ok(String texto) {
        msg.getStyleClass().setAll("msg-ok");
        msg.setText(texto);
    }

    private void erro(String texto) {
        msg.getStyleClass().setAll("msg-erro");
        msg.setText(texto);
    }

    private Window janela() {
        return tabela.getScene().getWindow();
    }
}
