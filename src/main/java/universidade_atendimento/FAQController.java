package universidade_atendimento;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FAQController {

    private final FAQRepository faqRepository;

    public FAQController(
            FAQRepository faqRepository) {

        this.faqRepository = faqRepository;
    }

    // =====================================================
    // FAQ PARA O ALUNO
    // =====================================================

    @GetMapping("/faq")
    public String faq(
            Model model,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        // Usuário não autenticado
        if (usuario == null) {

            return "redirect:/login";
        }

        // FAQ destinada ao aluno
        if (!"ALUNO".equals(usuario.getPerfil())) {

            return "redirect:/universidade";
        }

        // Mostra somente perguntas ativas
        model.addAttribute(
                "perguntas",
                faqRepository
                        .findByAtivaTrueOrderByCategoriaAsc()
        );

        return "faq";
    }

    // =====================================================
    // GERENCIAMENTO DA FAQ - UNIVERSIDADE
    // =====================================================

    @GetMapping("/universidade/faq")
    public String gerenciarFAQ(
            HttpSession session,
            Model model) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        if (usuario == null) {

            return "redirect:/login";
        }

        if (!"UNIVERSIDADE".equals(
                usuario.getPerfil())) {

            return "redirect:/usuario";
        }

        model.addAttribute(
                "perguntas",
                faqRepository
                        .findAllByOrderByCategoriaAsc()
        );

        return "universidade-faq";
    }

    // =====================================================
    // CRIAR NOVA PERGUNTA
    // =====================================================

    @PostMapping("/universidade/faq/criar")
    public String criarFAQ(
            @RequestParam String categoria,
            @RequestParam String pergunta,
            @RequestParam String resposta,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        if (usuario == null) {

            return "redirect:/login";
        }

        if (!"UNIVERSIDADE".equals(
                usuario.getPerfil())) {

            return "redirect:/usuario";
        }

        FAQ faq = new FAQ();

        faq.setCategoria(
                categoria.trim()
        );

        faq.setPergunta(
                pergunta.trim()
        );

        faq.setResposta(
                resposta.trim()
        );

        faq.setAtiva(true);

        faqRepository.save(faq);

        return "redirect:/universidade/faq";
    }

    // =====================================================
    // TELA PARA EDITAR PERGUNTA
    // =====================================================

    @GetMapping("/universidade/faq/editar/{id}")
    public String editarFAQ(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        if (usuario == null) {

            return "redirect:/login";
        }

        if (!"UNIVERSIDADE".equals(
                usuario.getPerfil())) {

            return "redirect:/usuario";
        }

        FAQ faq =
                faqRepository
                        .findById(id)
                        .orElse(null);

        if (faq == null) {

            return "redirect:/universidade/faq";
        }

        model.addAttribute(
                "faq",
                faq
        );

        return "universidade-faq-editar";
    }

    // =====================================================
    // SALVAR ALTERAÇÃO
    // =====================================================

    @PostMapping("/universidade/faq/editar/{id}")
    public String salvarEdicaoFAQ(
            @PathVariable Long id,
            @RequestParam String categoria,
            @RequestParam String pergunta,
            @RequestParam String resposta,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        if (usuario == null) {

            return "redirect:/login";
        }

        if (!"UNIVERSIDADE".equals(
                usuario.getPerfil())) {

            return "redirect:/usuario";
        }

        FAQ faq =
                faqRepository
                        .findById(id)
                        .orElse(null);

        if (faq == null) {

            return "redirect:/universidade/faq";
        }

        faq.setCategoria(
                categoria.trim()
        );

        faq.setPergunta(
                pergunta.trim()
        );

        faq.setResposta(
                resposta.trim()
        );

        faqRepository.save(faq);

        return "redirect:/universidade/faq";
    }

    // =====================================================
    // ATIVAR / DESATIVAR
    // =====================================================

    @PostMapping("/universidade/faq/status/{id}")
    public String alterarStatus(
            @PathVariable Long id,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        if (usuario == null) {

            return "redirect:/login";
        }

        if (!"UNIVERSIDADE".equals(
                usuario.getPerfil())) {

            return "redirect:/usuario";
        }

        FAQ faq =
                faqRepository
                        .findById(id)
                        .orElse(null);

        if (faq != null) {

            faq.setAtiva(
                    !Boolean.TRUE.equals(faq.getAtiva())
            );

            faqRepository.save(faq);
        }

        return "redirect:/universidade/faq";
    }

    // =====================================================
    // EXCLUIR
    // =====================================================

    @PostMapping("/universidade/faq/excluir/{id}")
    public String excluirFAQ(
            @PathVariable Long id,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        if (usuario == null) {

            return "redirect:/login";
        }

        if (!"UNIVERSIDADE".equals(
                usuario.getPerfil())) {

            return "redirect:/usuario";
        }

        if (faqRepository.existsById(id)) {

            faqRepository.deleteById(id);
        }

        return "redirect:/universidade/faq";
    }
}