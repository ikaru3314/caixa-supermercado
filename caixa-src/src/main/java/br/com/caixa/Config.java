package br.com.caixa;

/** Configurações simples do sistema. Mude aqui. */
public final class Config {
    /** Senha para entrar na "Área do gerente" (cadastro de produtos). TROQUE antes de usar de verdade. */
    public static final String PIN_GERENTE = "1234";

    // ------------------------------------------------------------------ banco de dados

    /**
     * false = usa SQLite (arquivo caixa.db, não precisa instalar nada).
     * true  = usa MySQL (rode antes o script banco/supermercado_mysql.sql, ou deixe o
     *         programa criar as tabelas sozinho).
     */
    public static final boolean USAR_MYSQL = false;

    /** Endereço do MySQL. "supermercado" é o nome do banco. */
    public static final String MYSQL_URL = "jdbc:mysql://localhost:3306/supermercado"
            + "?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8"
            + "&useSSL=false&allowPublicKeyRetrieval=true";
    public static final String MYSQL_USUARIO = "root";
    private static final String MYSQL_SENHA = "";   // coloque a senha do seu MySQL aqui

    /** A senha pode vir da variável de ambiente CAIXA_MYSQL_SENHA (melhor que deixá-la no código). */
    public static String mysqlSenha() {
        String env = System.getenv("CAIXA_MYSQL_SENHA");
        return env != null ? env : MYSQL_SENHA;
    }

    private Config() {
    }
}
