package universidade_atendimento;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final AtendimentoRepository atendimentoRepository;

    public LoginController(
            UsuarioRepository usuarioRepository,
            AtendimentoRepository atendimentoRepository) {

        this.usuarioRepository = usuarioRepository;
        this.atendimentoRepository = atendimentoRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String autenticar(
            @RequestParam String email,
            @RequestParam String senha,
            Model model,
            HttpSession session) {

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElse(null);

        if (usuario == null ||
                !usuario.getSenha().equals(senha)) {

            model.addAttribute(
                    "erro",
                    "E-mail ou senha inválidos"
            );

            return "login";
        }

        // Guarda o usuário completo na sessão
        session.setAttribute("usuario", usuario);

        // Guarda também as informações individuais
        session.setAttribute(
                "usuarioId",
                usuario.getId()
        );

        session.setAttribute(
                "perfil",
                usuario.getPerfil()
        );

        // ==========================
        // PERFIL UNIVERSIDADE
        // ==========================

        if ("UNIVERSIDADE".equals(usuario.getPerfil())) {

            return "redirect:/universidade";
        }

        // ==========================
        // PERFIL ALUNO
        // ==========================

        if ("ALUNO".equals(usuario.getPerfil())) {

            return "redirect:/usuario";
        }

        model.addAttribute(
                "erro",
                "Perfil de usuário inválido."
        );

        return "login";
    }

    // ==============================
    // ÁREA DO ALUNO
    // ==============================

    @GetMapping("/usuario")
    public String usuario(
            HttpSession session,
            Model model) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!"ALUNO".equals(usuario.getPerfil())) {
            return "redirect:/universidade";
        }

        model.addAttribute("usuario", usuario);

        return "usuario";
    }

    // ==============================
    // ÁREA DA UNIVERSIDADE
    // ==============================

    @GetMapping("/universidade")
    public String universidade(
            HttpSession session,
            Model model) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!"UNIVERSIDADE".equals(usuario.getPerfil())) {
            return "redirect:/usuario";
        }

        model.addAttribute(
                "atendimentos",
                atendimentoRepository.findAll()
        );

        model.addAttribute(
                "usuario",
                usuario
        );

        return "universidade";
    }
    
    @GetMapping("/logout")
public String logout(HttpSession session) {

    session.invalidate();

    return "redirect:/login";
}
}