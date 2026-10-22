package br.com.caixa.model;

public final class Cpf {
    /**
     * true  = confere os dígitos verificadores (só aceita CPF real/válido).
     * false = aceita qualquer número com 11 dígitos (bom para testes).
     * Mude aqui para ligar/desligar a validação.
     */
    public static final boolean VALIDAR_DIGITOS = false;

    private Cpf() {
    }

    /** Usado pela tela: respeita a opção VALIDAR_DIGITOS. */
    public static boolean aceito(String texto) {
        if (digitos(texto).length() != 11) {
            return false;
        }
        return !VALIDAR_DIGITOS || valido(texto);
    }

    public static String digitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    /** Aplica a máscara 000.000.000-00 conforme o usuário digita. */
    public static String mascara(String texto) {
        String d = digitos(texto);
        if (d.length() > 11) {
            d = d.substring(0, 11);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            if (i == 3 || i == 6) {
                sb.append('.');
            } else if (i == 9) {
                sb.append('-');
            }
            sb.append(d.charAt(i));
        }
        return sb.toString();
    }

    public static boolean valido(String texto) {
        String d = digitos(texto);
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(d, 9) == d.charAt(9) - '0'
                && digitoVerificador(d, 10) == d.charAt(10) - '0';
    }

    private static int digitoVerificador(String d, int n) {
        int soma = 0;
        for (int i = 0; i < n; i++) {
            soma += (d.charAt(i) - '0') * (n + 1 - i);
        }
        int r = (soma * 10) % 11;
        return r == 10 ? 0 : r;
    }
}
