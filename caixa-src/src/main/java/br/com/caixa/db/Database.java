package br.com.caixa.db;

import br.com.caixa.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Conexão com o banco e criação das tabelas.
 * Usa SQLite (arquivo caixa.db) ou MySQL, conforme {@link Config#USAR_MYSQL}.
 */
public final class Database {
    private static final String URL_SQLITE = "jdbc:sqlite:caixa.db";

    private Database() {
    }

    public static boolean usaMysql() {
        return Config.USAR_MYSQL;
    }

    public static Connection conectar() throws SQLException {
        if (usaMysql()) {
            return DriverManager.getConnection(Config.MYSQL_URL, Config.MYSQL_USUARIO, Config.mysqlSenha());
        }
        return DriverManager.getConnection(URL_SQLITE);
    }

    public static void inicializar() {
        try (Connection c = conectar(); Statement s = c.createStatement()) {
            if (usaMysql()) {
                criarTabelasMysql(s);
            } else {
                criarTabelasSqlite(s);
                migrar(c);
            }
            popularProdutosDeExemplo(c);
        } catch (SQLException e) {
            if (usaMysql()) {
                throw new IllegalStateException("Não foi possível usar o MySQL. Confira se o servidor está ligado "
                        + "e o usuário/senha em Config.java. Detalhe: " + e.getMessage(), e);
            }
            throw new IllegalStateException("Não foi possível iniciar o banco: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------- MySQL

    private static void criarTabelasMysql(Statement s) throws SQLException {
        s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS produto (
                    codigo          VARCHAR(20)   NOT NULL,
                    nome            VARCHAR(100)  NOT NULL,
                    preco_centavos  BIGINT        NOT NULL,
                    por_kg          TINYINT(1)    NOT NULL DEFAULT 0,
                    categoria       VARCHAR(30)   NOT NULL DEFAULT 'MERCEARIA',
                    estoque         DECIMAL(12,3) NOT NULL DEFAULT 0,
                    PRIMARY KEY (codigo),
                    INDEX idx_produto_nome (nome),
                    INDEX idx_produto_categoria (categoria)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""");
        s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS venda (
                    id                   BIGINT       NOT NULL AUTO_INCREMENT,
                    data_hora            DATETIME     NOT NULL,
                    cpf                  VARCHAR(11)  NULL,
                    forma_pagamento      VARCHAR(30)  NOT NULL,
                    total_centavos       BIGINT       NOT NULL,
                    valor_pago_centavos  BIGINT       NOT NULL,
                    PRIMARY KEY (id),
                    INDEX idx_venda_data (data_hora)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""");
        s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS item_venda (
                    id                   BIGINT        NOT NULL AUTO_INCREMENT,
                    venda_id             BIGINT        NOT NULL,
                    codigo               VARCHAR(20)   NOT NULL,
                    nome                 VARCHAR(100)  NOT NULL,
                    quantidade           DECIMAL(12,3) NOT NULL,
                    preco_unit_centavos  BIGINT        NOT NULL,
                    total_centavos       BIGINT        NOT NULL,
                    PRIMARY KEY (id),
                    INDEX idx_item_venda (venda_id),
                    CONSTRAINT fk_item_venda FOREIGN KEY (venda_id) REFERENCES venda (id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""");
    }

    // ---------------------------------------------------------------- SQLite

    private static void criarTabelasSqlite(Statement s) throws SQLException {
        s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS produto (
                    codigo TEXT PRIMARY KEY,
                    nome TEXT NOT NULL,
                    preco_centavos INTEGER NOT NULL,
                    por_kg INTEGER NOT NULL DEFAULT 0,
                    categoria TEXT NOT NULL DEFAULT 'MERCEARIA',
                    estoque REAL NOT NULL DEFAULT 0
                )""");
        s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS venda (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    data_hora TEXT NOT NULL,
                    cpf TEXT,
                    forma_pagamento TEXT NOT NULL,
                    total_centavos INTEGER NOT NULL,
                    valor_pago_centavos INTEGER NOT NULL
                )""");
        s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS item_venda (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    venda_id INTEGER NOT NULL REFERENCES venda(id),
                    codigo TEXT NOT NULL,
                    nome TEXT NOT NULL,
                    quantidade REAL NOT NULL,
                    preco_unit_centavos INTEGER NOT NULL,
                    total_centavos INTEGER NOT NULL
                )""");
    }

    /** Bancos SQLite criados em versões antigas ganham a coluna "estoque" (100 de cada, para começar). */
    private static void migrar(Connection c) throws SQLException {
        boolean temEstoque = false;
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("PRAGMA table_info(produto)")) {
            while (rs.next()) {
                if ("estoque".equalsIgnoreCase(rs.getString("name"))) {
                    temEstoque = true;
                }
            }
        }
        if (!temEstoque) {
            try (Statement s = c.createStatement()) {
                s.executeUpdate("ALTER TABLE produto ADD COLUMN estoque REAL NOT NULL DEFAULT 100");
            }
        }
    }

    // ---------------------------------------------------------------- dados de exemplo

    /** Dados de exemplo (códigos de barras fictícios) para testar o sistema. */
    private static void popularProdutosDeExemplo(Connection c) throws SQLException {
        Object[][] produtos = {
                {"7891000100103", "Leite Integral 1L", 549, 0, "MERCEARIA"},
                {"7891910000197", "Açúcar Refinado 1kg", 499, 0, "MERCEARIA"},
                {"7896005800027", "Arroz Branco 5kg", 2690, 0, "MERCEARIA"},
                {"7896045104000", "Feijão Carioca 1kg", 879, 0, "MERCEARIA"},
                {"7891000053508", "Chocolate em Barra 90g", 650, 0, "MERCEARIA"},
                {"7894900011517", "Refrigerante Cola 2L", 999, 0, "MERCEARIA"},
                {"7891149103102", "Biscoito Recheado 130g", 325, 0, "MERCEARIA"},
                {"7896036090558", "Café Torrado 500g", 1890, 0, "MERCEARIA"},
                {"7891150023501", "Sabão em Pó 1kg", 1450, 0, "MERCEARIA"},
                {"7898080640437", "Pão de Forma 500g", 799, 0, "MERCEARIA"},
                {"2000001", "Banana Prata", 699, 1, "FLV"},
                {"2000002", "Maçã Fuji", 990, 1, "FLV"},
                {"2000003", "Tomate", 749, 1, "FLV"},
                {"2000004", "Cebola", 499, 1, "FLV"},
                {"2000005", "Batata", 599, 1, "FLV"},
                {"2000006", "Cenoura", 449, 1, "FLV"},
                {"2000007", "Laranja", 399, 1, "FLV"},
                {"2000008", "Mamão Papaia", 890, 1, "FLV"},
                {"2000009", "Limão", 550, 1, "FLV"},
                {"2000010", "Alface (unidade)", 350, 0, "FLV"},
        };
        String ignorar = usaMysql() ? "INSERT IGNORE" : "INSERT OR IGNORE";
        String sql = ignorar + " INTO produto(codigo, nome, preco_centavos, por_kg, categoria, estoque) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Object[] p : produtos) {
                ps.setString(1, (String) p[0]);
                ps.setString(2, (String) p[1]);
                ps.setLong(3, ((Integer) p[2]).longValue());
                ps.setInt(4, (Integer) p[3]);
                ps.setString(5, (String) p[4]);
                ps.setDouble(6, ((Integer) p[3]) == 1 ? 50 : 100); // estoque inicial: 50 kg ou 100 un
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
