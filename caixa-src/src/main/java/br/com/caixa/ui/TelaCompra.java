package br.com.caixa.ui;

import br.com.caixa.db.ProdutoDao;
import br.com.caixa.model.ItemVenda;
import br.com.caixa.model.Moeda;
import br.com.caixa.model.Produto;
import br.com.caixa.model.Sessao;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Window;

import java.util.Optional;
import java.util.function.Function;

/**
 * Tela de venda no estilo PDV (ponto de venda).
 *
 * Teclas de atalho: F1 ajuda | F2 digitar código | F3 frutas/legumes | F4 CPF |
 * F5 remover item | F10 finalizar | Esc voltar.
 */
public class TelaCompra {
    private final App app;
    private final Sessao sessao;
    private final ProdutoDao produtos = new ProdutoDao();

    private final Label status = new Label("CAIXA ABERTO");
    private final Label lblCpf = new Label();
    private final TextField campoCodigo = new TextField();
    private final Label lblValorUnitario = valor("pdv-valor");
    private final Label lblTotalItem = valor("pdv-valor");
    private final Label lblSubtotal = valor("pdv-valor-grande");
    private final Label lblRecebido = valor("pdv-valor");
    private final Label lblTroco = valor("pdv-valor");
    private final TableView<ItemVenda> tabela = new TableView<>();
    private Button btnFinalizar;

    public TelaCompra(App app, Sessao sessao) {
        this.app = app;
        this.sessao = sessao;
    }

    public Parent criar() {
        sessao.valorPagoCentavos(0); // a compra ainda não foi paga

        BorderPane tela = new BorderPane();
        tela.setTop(montarTopo());

        HBox centro = new HBox(12, colunaEsquerda(), colunaDireita());
        centro.setPadding(new Insets(0, 14, 14, 14));
        tela.setCenter(centro);

        tela.addEventFilter(KeyEvent.KEY_PRESSED, this::tratarTecla);

        mostrarItem(null);
        atualizarTotais();
        Platform.runLater(campoCodigo::requestFocus);
        return tela;
    }

    // ================================================================ topo

    private HBox montarTopo() {
        status.getStyleClass().add("pdv-status");
        status.setMaxWidth(Double.MAX_VALUE);
        status.setMinWidth(0);
        HBox.setHgrow(status, Priority.ALWAYS);

        lblCpf.getStyleClass().add("pdv-cpf");
        lblCpf.setMinWidth(120);
        lblCpf.setPrefHeight(56);

        Button ajuda = new Button("AJUDA\nF1");
        ajuda.getStyleClass().add("btn-ajuda");
        ajuda.setFocusTraversable(false);
        ajuda.setOnAction(e -> app.abrirAjuda());

        HBox topo = new HBox(10, status, lblCpf, ajuda);
        topo.setAlignment(Pos.CENTER);
        topo.setPadding(new Insets(12, 14, 10, 14));
        return topo;
    }

    // ================================================================ coluna esquerda

    private VBox colunaEsquerda() {
        SVGPath carrinho = new SVGPath();
        carrinho.setContent("M0,0 L8,0 L14,26 L40,26 L46,8 L10,8 "
                + "M12,36 a4,4 0 1,0 8,0 a4,4 0 1,0 -8,0 "
                + "M34,36 a4,4 0 1,0 8,0 a4,4 0 1,0 -8,0");
        carrinho.getStyleClass().add("carrinho");
        carrinho.setScaleX(2.2);
        carrinho.setScaleY(2.2);
        StackPane quadroCarrinho = new StackPane(carrinho);
        quadroCarrinho.getStyleClass().add("pdv-quadro-carrinho");
        quadroCarrinho.setMinHeight(110);
        quadroCarrinho.setPrefHeight(110);

        campoCodigo.getStyleClass().add("campo");
        campoCodigo.setPromptText("passe o produto");
        campoCodigo.setOnAction(e -> processarCodigo(campoCodigo.getText()));

        VBox esquerda = new VBox(8,
                quadroCarrinho,
                bloco("CÓDIGO DE BARRAS", campoCodigo),
                bloco("VALOR UNITÁRIO", lblValorUnitario),
                bloco("TOTAL DO ITEM", lblTotalItem),
                atalhos());
        esquerda.setMinWidth(270);
        esquerda.setPrefWidth(290);
        esquerda.setMaxWidth(310);
        return esquerda;
    }

    /** Botões de atalho (clicáveis, mostram a tecla correspondente). */
    private GridPane atalhos() {
        btnFinalizar = atalho("F10", "Finalizar", "btn-finalizar", app::irParaFormaPagamento);
        btnFinalizar.disableProperty().bind(Bindings.isEmpty(sessao.itens()));

        GridPane g = new GridPane();
        g.setHgap(6);
        g.setVgap(6);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        g.getColumnConstraints().addAll(c1, c2);

        g.add(atalho("F2", "Digitar código", null, this::digitarCodigo), 0, 0);
        g.add(atalho("F3", "Frutas/Legumes", null, this::abrirHortifruti), 1, 0);
        g.add(atalho("F4", "Informar CPF", null, app::irParaCpf), 0, 1);
        g.add(atalho("F5", "Remover item", null, this::removerSelecionado), 1, 1);
        g.add(btnFinalizar, 0, 2);
        g.add(atalho("Esc", "Voltar", "btn-voltar", this::voltar), 1, 2);
        return g;
    }

    private Button atalho(String tecla, String texto, String classeExtra, Runnable acao) {
        Button b = new Button(tecla + "  " + texto);
        b.getStyleClass().addAll("btn-rodape", "btn-atalho");
        if (classeExtra != null) {
            b.getStyleClass().add(classeExtra);
        }
        b.setMaxWidth(Double.MAX_VALUE);
        b.setFocusTraversable(false);
        b.setOnAction(e -> {
            acao.run();
        });
        return b;
    }

    // ================================================================ coluna direita

    private VBox colunaDireita() {
        Label tituloLista = new Label("LISTA DE PRODUTOS");
        tituloLista.getStyleClass().add("pdv-lista-titulo");
        tituloLista.setMaxWidth(Double.MAX_VALUE);

        configurarTabela();
        VBox.setVgrow(tabela, Priority.ALWAYS);

        VBox lista = new VBox(tituloLista, tabela);
        lista.getStyleClass().add("pdv-bloco");
        VBox.setVgrow(lista, Priority.ALWAYS);

        VBox recebido = bloco("TOTAL RECEBIDO", lblRecebido);
        VBox troco = bloco("TROCO", lblTroco);
        recebido.setMaxWidth(Double.MAX_VALUE);
        troco.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(recebido, Priority.ALWAYS);
        HBox.setHgrow(troco, Priority.ALWAYS);
        HBox baixo = new HBox(10, recebido, troco);

        VBox direita = new VBox(8, lista, bloco("SUBTOTAL", lblSubtotal), baixo);
        HBox.setHgrow(direita, Priority.ALWAYS);
        return direita;
    }

    private void configurarTabela() {
        tabela.setItems(sessao.itens());
        tabela.setPlaceholder(new Label(""));
        tabela.setFocusTraversable(false); // o foco fica sempre no campo do leitor
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.getColumns().add(coluna("Nº ITEM", i -> String.valueOf(sessao.itens().indexOf(i) + 1), "CENTER", 70));
        tabela.getColumns().add(coluna("CÓDIGO", i -> i.produto().codigo(), "CENTER-LEFT", 130));
        tabela.getColumns().add(coluna("DESCRIÇÃO", i -> i.produto().nome(), "CENTER-LEFT", 300));
        tabela.getColumns().add(coluna("QTD", ItemVenda::quantidadeTexto, "CENTER-RIGHT", 90));
        tabela.getColumns().add(coluna("VLR. UNIT.", i -> Moeda.semSimbolo(i.produto().precoCentavos()), "CENTER-RIGHT", 90));
        tabela.getColumns().add(coluna("TOTAL", i -> Moeda.semSimbolo(i.totalCentavos()), "CENTER-RIGHT", 90));
        // ao selecionar uma linha, os painéis da esquerda mostram aquele item
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, item) -> mostrarItem(item));
    }

    private TableColumn<ItemVenda, String> coluna(String titulo, Function<ItemVenda, String> valor,
                                                  String alinhamento, double largura) {
        TableColumn<ItemVenda, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(cd -> new ReadOnlyStringWrapper(valor.apply(cd.getValue())));
        c.setSortable(false);
        c.setPrefWidth(largura);
        c.setStyle("-fx-alignment: " + alinhamento + ";");
        return c;
    }

    // ================================================================ componentes

    private static Label valor(String classe) {
        Label l = new Label();
        l.getStyleClass().add(classe);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    /** Painel com barra de título escura e corpo branco (como no PDV de referência). */
    private VBox bloco(String titulo, Node conteudo) {
        Label t = new Label(titulo);
        t.getStyleClass().add("pdv-bloco-titulo");
        t.setMaxWidth(Double.MAX_VALUE);
        StackPane corpo = new StackPane(conteudo);
        corpo.getStyleClass().add("pdv-bloco-corpo");
        VBox v = new VBox(t, corpo);
        v.getStyleClass().add("pdv-bloco");
        return v;
    }

    // ================================================================ teclas de atalho

    private void tratarTecla(KeyEvent e) {
        Runnable acao = switch (e.getCode()) {
            case F1 -> app::abrirAjuda;
            case F2 -> this::digitarCodigo;
            case F3 -> this::abrirHortifruti;
            case F4 -> app::irParaCpf;
            case F5 -> this::removerSelecionado;
            case F10 -> () -> {
                if (!sessao.itens().isEmpty()) {
                    app.irParaFormaPagamento();
                }
            };
            case ESCAPE -> this::voltar;
            default -> null;
        };
        if (acao != null) {
            e.consume();
            Platform.runLater(acao);
        }
    }

    // ================================================================ ações

    private void processarCodigo(String texto) {
        String codigo = texto == null ? "" : texto.trim();
        if (codigo.isEmpty()) {
            return;
        }
        try {
            Optional<Produto> produto = produtos.buscarPorCodigo(codigo);
            if (produto.isPresent()) {
                adicionarProduto(produto.get());
            } else {
                setStatus("PRODUTO NÃO ENCONTRADO: " + codigo, true);
            }
        } catch (IllegalStateException e) {
            setStatus("ERRO NO BANCO DE DADOS", true);
        }
        campoCodigo.clear();
        campoCodigo.requestFocus();
    }

    private void adicionarProduto(Produto produto) {
        double quantidade = 1;
        if (produto.porKg()) {
            // Num caixa real o peso viria da balança integrada; aqui o cliente digita.
            Optional<Double> peso = Dialogs
                    .pedirNumero(janela(), "Pesagem",
                            "Digite o peso de \"" + produto.nome() + "\" em kg (ex.: 0,350)", true, 6)
                    .flatMap(Moeda::parseQuantidade);
            if (peso.isEmpty() || peso.get() <= 0) {
                return;
            }
            quantidade = peso.get();
        }
        // confere o estoque cadastrado no banco (considerando o que já está no carrinho)
        if (sessao.quantidadeDe(produto.codigo()) + quantidade > produto.estoque() + 1e-9) {
            setStatus("SEM ESTOQUE: " + produto.nome().toUpperCase()
                    + " (disponível: " + produto.estoqueTexto() + ")", true);
            return;
        }
        ItemVenda item = sessao.adicionar(produto, quantidade);
        sessao.valorPagoCentavos(0);
        tabela.refresh();
        tabela.getSelectionModel().select(item);
        tabela.scrollTo(item);
        mostrarItem(item);
        atualizarTotais();
        setStatus(produto.nome().toUpperCase(), false);
    }

    private void abrirHortifruti() {
        try {
            Dialogs.escolherProduto(janela(), "Frutas, legumes e verduras", produtos.listarPorCategoria("FLV"))
                    .ifPresent(this::adicionarProduto);
        } catch (IllegalStateException e) {
            setStatus("ERRO NO BANCO DE DADOS", true);
        }
        campoCodigo.requestFocus();
    }

    private void digitarCodigo() {
        Dialogs.pedirNumero(janela(), "Digitar código de barras",
                "Digite o código de barras do produto", false, 13).ifPresent(this::processarCodigo);
        campoCodigo.requestFocus();
    }

    private void removerSelecionado() {
        ItemVenda item = tabela.getSelectionModel().getSelectedItem();
        if (item == null) {
            setStatus("SELECIONE UM ITEM DA LISTA PARA REMOVER", true);
            return;
        }
        sessao.itens().remove(item);
        sessao.valorPagoCentavos(0);
        tabela.refresh();
        atualizarTotais();
        setStatus("ITEM REMOVIDO", false);
        campoCodigo.requestFocus();
    }

    private void voltar() {
        if (sessao.itens().isEmpty()) {
            app.irParaCpf();
        } else if (Dialogs.confirmar(janela(), "Cancelar compra",
                "Deseja cancelar a compra? Todos os itens serão removidos.")) {
            app.irParaInicio();
        }
    }

    // ================================================================ atualização da tela

    /** Mostra o valor unitário e o total do item (ou zeros quando não há item). */
    private void mostrarItem(ItemVenda item) {
        lblValorUnitario.setText(Moeda.formatar(item == null ? 0 : item.produto().precoCentavos()));
        lblTotalItem.setText(Moeda.formatar(item == null ? 0 : item.totalCentavos()));
    }

    private void atualizarTotais() {
        lblSubtotal.setText(Moeda.semSimbolo(sessao.totalCentavos()));
        lblRecebido.setText(Moeda.formatar(sessao.valorPagoCentavos()));
        lblTroco.setText(Moeda.formatar(sessao.trocoCentavos()));
        lblCpf.setText(sessao.cpf() == null ? "SEM CPF" : "CPF NA NOTA");
    }

    /** Mensagem grande no topo (substitui "CAIXA ABERTO"). */
    private void setStatus(String texto, boolean erro) {
        status.setText(texto);
        status.getStyleClass().remove("pdv-status-erro");
        if (erro) {
            status.getStyleClass().add("pdv-status-erro");
        }
    }

    private Window janela() {
        return campoCodigo.getScene().getWindow();
    }
}
