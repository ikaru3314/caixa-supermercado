package br.com.caixa.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Assistente de ajuda do caixa (botão AJUDA).
 *
 * <p>Se a variável de ambiente {@code ANTHROPIC_API_KEY} estiver definida, as perguntas vão
 * para a API do Claude. Sem a chave (ou sem internet), cai automaticamente num modo local
 * com respostas prontas, então o caixa nunca fica sem ajuda.
 *
 * <p>Variáveis opcionais: {@code CAIXA_IA_MODELO} (padrão: claude-haiku-5-5).
 */
public class AssistenteIA {
    private static final String ENDPOINT = "https://api.anthropic.com/v1/messages";
    private static final int MAX_MENSAGENS_HISTORICO = 10;

    private static final String SISTEMA = """
            Você é o assistente de ajuda de um caixa de autoatendimento de supermercado.
            Responda sempre em português do Brasil, com no máximo 3 frases curtas, em tom simples e amigável.
            O cliente passa por estas etapas: Início, Informar CPF, Efetuar compra, Forma de pagamento, Pagamento e Finalização.
            Na etapa de compra: passe o código de barras no leitor; se não ler, use o botão DIGITAR CÓDIGO DE BARRAS;
            frutas, legumes e verduras ficam no botão FRUTAS, LEGUMES E VERDURAS (informa-se o peso em kg);
            para remover um item, toque nele na lista e use "remover item selecionado";
            para pagar, toque em FINALIZAR COMPRA. Formas de pagamento: dinheiro, débito, crédito e Pix.
            Use o contexto do caixa informado na mensagem para dar a resposta certa para a etapa atual.
            Nunca invente preços, promoções ou políticas da loja. Se não souber ou se o problema for com o equipamento,
            diga para chamar um funcionário. Nunca peça senha, número de cartão ou outros dados sensíveis.
            """;

    private final String apiKey = System.getenv("ANTHROPIC_API_KEY");
    private final String modelo = System.getenv().getOrDefault("CAIXA_IA_MODELO", "claude-haiku-5-5");
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final List<String[]> historico = new ArrayList<>(); // {papel, texto}

    /** true quando a chave da API está configurada (respostas vêm do Claude). */
    public boolean usaNuvem() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** Começa uma conversa nova (chamado a cada novo cliente). */
    public synchronized void reiniciar() {
        historico.clear();
    }

    /** Pode demorar (rede): chame fora da thread da interface. */
    public synchronized String perguntar(String pergunta, String contextoDoCaixa) {
        if (usaNuvem()) {
            try {
                return perguntarNuvem(pergunta, contextoDoCaixa);
            } catch (IOException | RuntimeException e) {
                // sem rede / chave inválida / erro da API: usa o modo local
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return AjudaLocal.responder(pergunta);
    }

    private String perguntarNuvem(String pergunta, String contexto) throws IOException, InterruptedException {
        String mensagemUsuario = "[Contexto do caixa: " + contexto + "]\nPergunta do cliente: " + pergunta;

        JsonArray mensagens = new JsonArray();
        for (String[] m : historico) {
            mensagens.add(mensagem(m[0], m[1]));
        }
        mensagens.add(mensagem("user", mensagemUsuario));

        JsonObject corpo = new JsonObject();
        corpo.addProperty("model", modelo);
        corpo.addProperty("max_tokens", 400);
        corpo.addProperty("system", SISTEMA);
        corpo.add("messages", mensagens);

        HttpRequest req = HttpRequest.newBuilder(URI.create(ENDPOINT))
                .timeout(Duration.ofSeconds(20))
                .header("content-type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(corpo.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() / 100 != 2) {
            throw new IOException("API respondeu HTTP " + resp.statusCode());
        }

        String texto = JsonParser.parseString(resp.body()).getAsJsonObject()
                .getAsJsonArray("content").get(0).getAsJsonObject()
                .get("text").getAsString().trim();

        historico.add(new String[]{"user", mensagemUsuario});
        historico.add(new String[]{"assistant", texto});
        while (historico.size() > MAX_MENSAGENS_HISTORICO) {
            historico.remove(0);
            historico.remove(0); // mantém os pares pergunta/resposta
        }
        return texto;
    }

    private static JsonObject mensagem(String papel, String texto) {
        JsonObject o = new JsonObject();
        o.addProperty("role", papel);
        o.addProperty("content", texto);
        return o;
    }
}
