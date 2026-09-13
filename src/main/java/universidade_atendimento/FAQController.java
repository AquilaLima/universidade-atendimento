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
    private final UsuarioRepository usuarioRepository;

    public FAQController(
            FAQRepository faqRepository,
            UsuarioRepository usuarioRepository) {

        this.faqRepository = faqRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // =====================================================
    // FAQ PARA O ALUNO
    // =====================================================

    @GetMapping("/faq")
    public String faq(
            Model model,
            HttpSession session) {

        Long usuarioId =
                (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuario =
                usuarioRepository
                        .findById(usuarioId)
                        .orElse(null);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        if (!"ALUNO".equals(usuario.getPerfil())) {
            return "redirect:/login";
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            session.invalidate();
            return "redirect:/login";
        }

        session.setAttribute("usuario", usuario);

        model.addAttribute(
                "perguntas",
                faqRepository
                        .findByAtivaTrueOrderByCategoriaAsc()
        );

        return "faq";
    }

    // =====================================================
    // GERENCIAMENTO DA FAQ - ADMINISTRADOR
    // =====================================================

    @GetMapping("/admin/faq")
    public String gerenciarFAQ(
            HttpSession session,
            Model model) {

        Usuario usuario =
                obterAdminAtivo(session);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute(
                "perguntas",
                faqRepository
                        .findAllByOrderByCategoriaAsc()
        );

        model.addAttribute(
                "usuario",
                usuario
        );

        return "admin-faq";
    }

    // =====================================================
    // CRIAR NOVA PERGUNTA
    // =====================================================

    @PostMapping("/admin/faq/criar")
    public String criarFAQ(
            @RequestParam String categoria,
            @RequestParam String pergunta,
            @RequestParam String resposta,
            HttpSession session) {

        Usuario usuario =
                obterAdminAtivo(session);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
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

        return "redirect:/admin/faq";
    }

    // =====================================================
    // TELA PARA EDITAR PERGUNTA
    // =====================================================

    @GetMapping("/admin/faq/editar/{id}")
    public String editarFAQ(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Usuario usuario =
                obterAdminAtivo(session);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        FAQ faq =
                faqRepository
                        .findById(id)
                        .orElse(null);

        if (faq == null) {
            return "redirect:/admin/faq";
        }

        model.addAttribute(
                "faq",
                faq
        );

        model.addAttribute(
                "usuario",
                usuario
        );

        return "admin-faq-editar";
    }

    // =====================================================
    // SALVAR ALTERAÇÃO
    // =====================================================

    @PostMapping("/admin/faq/editar/{id}")
    public String salvarEdicaoFAQ(
            @PathVariable Long id,
            @RequestParam String categoria,
            @RequestParam String pergunta,
            @RequestParam String resposta,
            HttpSession session) {

        Usuario usuario =
                obterAdminAtivo(session);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        FAQ faq =
                faqRepository
                        .findById(id)
                        .orElse(null);

        if (faq == null) {
            return "redirect:/admin/faq";
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

        return "redirect:/admin/faq";
    }

    // =====================================================
    // ATIVAR / DESATIVAR FAQ
    // =====================================================

    @PostMapping("/admin/faq/status/{id}")
    public String alterarStatus(
            @PathVariable Long id,
            HttpSession session) {

        Usuario usuario =
                obterAdminAtivo(session);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        FAQ faq =
                faqRepository
                        .findById(id)
                        .orElse(null);

        if (faq != null) {

            faq.setAtiva(
                    !Boolean.TRUE.equals(
                            faq.getAtiva()
                    )
            );

            faqRepository.save(faq);
        }

        return "redirect:/admin/faq";
    }

    // =====================================================
    // EXCLUIR FAQ
    // =====================================================

    @PostMapping("/admin/faq/excluir/{id}")
    public String excluirFAQ(
            @PathVariable Long id,
            HttpSession session) {

        Usuario usuario =
                obterAdminAtivo(session);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        if (faqRepository.existsById(id)) {
            faqRepository.deleteById(id);
        }

        return "redirect:/admin/faq";
    }

    // =====================================================
    // VALIDAÇÃO DO ADMINISTRADOR
    // =====================================================

    private Usuario obterAdminAtivo(
            HttpSession session) {

        Long usuarioId =
                (Long) session.getAttribute("usuarioId");

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

        if (!"ADM".equals(usuario.getPerfil())) {
            return null;
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            return null;
        }

        session.setAttribute(
                "usuario",
                usuario
        );

        return usuario;
    }
}