package universidade_atendimento;

import jakarta.servlet.http.HttpSession;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ChatbotController {

    private final FAQRepository faqRepository;
    private final UsuarioRepository usuarioRepository;

    public ChatbotController(
            FAQRepository faqRepository,
            UsuarioRepository usuarioRepository) {

        this.faqRepository = faqRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // =========================================================
    // TELA DO ASSISTENTE
    // =========================================================

    @GetMapping("/assistente")
    public String assistente(
            HttpSession session,
            Model model) {

        Usuario usuario =
                obterAlunoAtivo(session);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute(
                "usuario",
                usuario
        );

        return "assistente";
    }

    // =========================================================
    // PERGUNTAR AO ASSISTENTE
    // =========================================================

    @PostMapping("/assistente/perguntar")
    public String perguntar(
            @RequestParam String pergunta,
            HttpSession session,
            Model model) {

        Usuario usuario =
                obterAlunoAtivo(session);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute(
                "usuario",
                usuario
        );

        model.addAttribute(
                "perguntaUsuario",
                pergunta
        );

        if (pergunta == null
                || pergunta.isBlank()) {

            model.addAttribute(
                    "resposta",
                    "Digite uma pergunta para que eu possa ajudar."
            );

            return "assistente";
        }

        List<FAQ> faqs =
                faqRepository
                        .findByAtivaTrueOrderByCategoriaAsc();

        FAQ melhorFAQ =
                encontrarMelhorFAQ(
                        pergunta,
                        faqs
                );

        if (melhorFAQ != null) {

            model.addAttribute(
                    "resposta",
                    melhorFAQ.getResposta()
            );

            model.addAttribute(
                    "categoria",
                    melhorFAQ.getCategoria()
            );

            model.addAttribute(
                    "encontrouResposta",
                    true
            );

        } else {

            model.addAttribute(
                    "resposta",
                    "Não encontrei uma resposta cadastrada para essa dúvida. "
                    + "Você pode abrir um chamado para que a universidade "
                    + "analise sua solicitação."
            );

            model.addAttribute(
                    "encontrouResposta",
                    false
            );
        }

        return "assistente";
    }

    // =========================================================
    // LOCALIZAR FAQ MAIS PARECIDA
    // =========================================================

    private FAQ encontrarMelhorFAQ(
            String perguntaUsuario,
            List<FAQ> faqs) {

        String perguntaNormalizada =
                normalizar(perguntaUsuario);

        FAQ melhorFAQ = null;

        int melhorPontuacao = 0;

        for (FAQ faq : faqs) {

            String perguntaFAQ =
                    normalizar(
                            faq.getPergunta()
                    );

            // Correspondência direta
            if (perguntaNormalizada.contains(perguntaFAQ)
                    || perguntaFAQ.contains(perguntaNormalizada)) {

                return faq;
            }

            int pontuacao =
                    calcularPontuacao(
                            perguntaNormalizada,
                            perguntaFAQ
                    );

            if (pontuacao > melhorPontuacao) {

                melhorPontuacao =
                        pontuacao;

                melhorFAQ =
                        faq;
            }
        }

        /*
         * Exige pelo menos duas palavras relevantes
         * coincidentes.
         */
        if (melhorPontuacao >= 2) {
            return melhorFAQ;
        }

        return null;
    }

    // =========================================================
    // CALCULAR SEMELHANÇA
    // =========================================================

    private int calcularPontuacao(
            String perguntaUsuario,
            String perguntaFAQ) {

        Set<String> palavrasUsuario =
                extrairPalavras(
                        perguntaUsuario
                );

        Set<String> palavrasFAQ =
                extrairPalavras(
                        perguntaFAQ
                );

        int pontuacao = 0;

        for (String palavra : palavrasUsuario) {

            if (palavrasFAQ.contains(palavra)) {
                pontuacao++;
            }
        }

        return pontuacao;
    }

    // =========================================================
    // EXTRAIR PALAVRAS RELEVANTES
    // =========================================================

    private Set<String> extrairPalavras(
            String texto) {

        Set<String> ignorar =
                Set.of(
                        "a",
                        "o",
                        "as",
                        "os",
                        "de",
                        "da",
                        "do",
                        "das",
                        "dos",
                        "e",
                        "em",
                        "para",
                        "por",
                        "um",
                        "uma",
                        "como",
                        "que",
                        "eu",
                        "meu",
                        "minha"
                );

        Set<String> palavras =
                new HashSet<>();

        Arrays.stream(
                texto.split("\\s+")
        )
        .filter(
                palavra ->
                        palavra.length() >= 3
        )
        .filter(
                palavra ->
                        !ignorar.contains(palavra)
        )
        .forEach(
                palavras::add
        );

        return palavras;
    }

    // =========================================================
    // NORMALIZAR TEXTO
    // =========================================================

    private String normalizar(
            String texto) {

        if (texto == null) {
            return "";
        }

        String normalizado =
                Normalizer.normalize(
                        texto,
                        Normalizer.Form.NFD
                );

        normalizado =
                normalizado.replaceAll(
                        "\\p{M}",
                        ""
                );

        normalizado =
                normalizado.toLowerCase();

        normalizado =
                normalizado.replaceAll(
                        "[^a-z0-9\\s]",
                        " "
                );

        normalizado =
                normalizado.replaceAll(
                        "\\s+",
                        " "
                );

        return normalizado.trim();
    }

    // =========================================================
    // VALIDAR ALUNO
    // =========================================================

    private Usuario obterAlunoAtivo(
            HttpSession session) {

        Long usuarioId =
                (Long) session.getAttribute(
                        "usuarioId"
                );

        if (usuarioId == null) {
            return null;
        }

        Usuario usuario =
                usuarioRepository
                        .findById(usuarioId)
                        .orElse(null);

        if (usuario == null) {
            return null;
        }

        if (!"ALUNO".equals(
                usuario.getPerfil())) {

            return null;
        }

        if (!Boolean.TRUE.equals(
                usuario.getAtivo())) {

            return null;
        }

        session.setAttribute(
                "usuario",
                usuario
        );

        return usuario;
    }
    
    // =========================================================
// ASSISTENTE - RESPOSTA PARA CHAT FLUTUANTE
// =========================================================

@PostMapping("/assistente/perguntar-json")
@ResponseBody
public Map<String, Object> perguntarJson(
        @RequestParam String pergunta,
        HttpSession session) {

    Map<String, Object> resultado =
            new HashMap<>();

    Usuario usuario =
            obterAlunoAtivo(session);

    if (usuario == null) {

        resultado.put(
                "sucesso",
                false
        );

        resultado.put(
                "resposta",
                "Sua sessão expirou. Faça login novamente."
        );

        return resultado;
    }

    if (pergunta == null
            || pergunta.isBlank()) {

        resultado.put(
                "sucesso",
                false
        );

        resultado.put(
                "resposta",
                "Digite uma pergunta para que eu possa ajudar."
        );

        return resultado;
    }

    List<FAQ> faqs =
            faqRepository
                    .findByAtivaTrueOrderByCategoriaAsc();

    FAQ melhorFAQ =
            encontrarMelhorFAQ(
                    pergunta,
                    faqs
            );

    if (melhorFAQ != null) {

        resultado.put(
                "sucesso",
                true
        );

        resultado.put(
                "encontrou",
                true
        );

        resultado.put(
                "resposta",
                melhorFAQ.getResposta()
        );

        resultado.put(
                "categoria",
                melhorFAQ.getCategoria()
        );

    } else {

        resultado.put(
                "sucesso",
                true
        );

        resultado.put(
                "encontrou",
                false
        );

        resultado.put(
                "resposta",
                "Não encontrei uma resposta cadastrada para essa dúvida. "
                + "Se precisar de uma análise mais detalhada, "
                + "você pode abrir um chamado."
        );
    }

    return resultado;
}
}