package universidade_atendimento;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final AtendimentoRepository atendimentoRepository;

    /*
     * Credencial da universidade.
     *
     * Não ficará escrita diretamente no código.
     * Será obtida de uma variável de ambiente.
     */
    @Value("${app.universidade.credencial}")
    private String credencialUniversidade;

    public LoginController(
            UsuarioRepository usuarioRepository,
            AtendimentoRepository atendimentoRepository) {

        this.usuarioRepository = usuarioRepository;
        this.atendimentoRepository = atendimentoRepository;
    }

    // =====================================================
    // TELA DE LOGIN
    // =====================================================

    @GetMapping("/login")
    public String login() {

        return "login";
    }

    // =====================================================
    // PRIMEIRO PASSO DO ALUNO
    // INFORMA SOMENTE O E-MAIL INSTITUCIONAL
    // =====================================================

    @PostMapping("/login/aluno")
    public String loginAluno(
            @RequestParam String email,
            Model model,
            HttpSession session) {

        Optional<Usuario> resultado =
                usuarioRepository.findByEmail(email.trim().toLowerCase());

        if (resultado.isEmpty()) {

            model.addAttribute(
                    "erro",
                    "E-mail institucional não encontrado."
            );

            return "login";
        }

        Usuario usuario = resultado.get();

        if (!"ALUNO".equals(usuario.getPerfil())) {

            model.addAttribute(
                    "erro",
                    "Este e-mail não pertence a um aluno."
            );

            return "login";
        }

        // =================================================
        // ALUNO AINDA NÃO FINALIZOU O CADASTRO
        // =================================================

        if (!Boolean.TRUE.equals(
                usuario.getCadastroFinalizado())) {

            session.setAttribute(
                    "preCadastroUsuarioId",
                    usuario.getId()
            );

            return "redirect:/finalizar-cadastro";
        }

        // =================================================
        // CADASTRO JÁ FINALIZADO
        // AGORA PRECISAMOS VALIDAR A SENHA
        // =================================================

        session.setAttribute(
                "loginAlunoId",
                usuario.getId()
        );

        return "redirect:/login/aluno/senha";
    }

    // =====================================================
    // TELA PARA INFORMAR A SENHA DO ALUNO
    // =====================================================

    @GetMapping("/login/aluno/senha")
    public String telaSenhaAluno(
            HttpSession session) {

        Long usuarioId =
                (Long) session.getAttribute("loginAlunoId");

        if (usuarioId == null) {

            return "redirect:/login";
        }

        return "login-aluno-senha";
    }

    // =====================================================
    // AUTENTICAÇÃO DO ALUNO
    // =====================================================

    @PostMapping("/login/aluno/senha")
    public String autenticarAluno(
            @RequestParam String senha,
            HttpSession session,
            Model model) {

        Long usuarioId =
                (Long) session.getAttribute("loginAlunoId");

        if (usuarioId == null) {

            return "redirect:/login";
        }

        Usuario usuario =
                usuarioRepository.findById(usuarioId)
                        .orElse(null);

        if (usuario == null) {

            session.removeAttribute("loginAlunoId");

            return "redirect:/login";
        }

        if (usuario.getSenha() == null ||
                !usuario.getSenha().equals(senha)) {

            model.addAttribute(
                    "erro",
                    "Senha inválida."
            );

            return "login-aluno-senha";
        }

        session.removeAttribute("loginAlunoId");

        criarSessao(usuario, session);

        return "redirect:/usuario";
    }

    // =====================================================
    // FINALIZAR CADASTRO DO ALUNO
    // =====================================================

    @GetMapping("/finalizar-cadastro")
    public String finalizarCadastro(
            HttpSession session,
            Model model) {

        Long usuarioId =
                (Long) session.getAttribute(
                        "preCadastroUsuarioId"
                );

        if (usuarioId == null) {

            return "redirect:/login";
        }

        Usuario usuario =
                usuarioRepository.findById(usuarioId)
                        .orElse(null);

        if (usuario == null) {

            session.removeAttribute(
                    "preCadastroUsuarioId"
            );

            return "redirect:/login";
        }

        model.addAttribute(
                "usuario",
                usuario
        );

        return "finalizar-cadastro";
    }

    // =====================================================
    // SALVAR FINALIZAÇÃO DO CADASTRO
    // =====================================================

    @PostMapping("/finalizar-cadastro")
    public String salvarCadastroAluno(
            @RequestParam String nome,
            @RequestParam String telefone,
            @RequestParam String senha,
            HttpSession session,
            Model model) {

        Long usuarioId =
                (Long) session.getAttribute(
                        "preCadastroUsuarioId"
                );

        if (usuarioId == null) {

            return "redirect:/login";
        }

        Usuario usuario =
                usuarioRepository.findById(usuarioId)
                        .orElse(null);

        if (usuario == null) {

            return "redirect:/login";
        }

        if (!"ALUNO".equals(usuario.getPerfil())) {

            return "redirect:/login";
        }

        usuario.setNome(nome.trim());
        usuario.setTelefone(telefone.trim());
        usuario.setSenha(senha);

        usuario.setCadastroFinalizado(true);

        usuarioRepository.save(usuario);

        session.removeAttribute(
                "preCadastroUsuarioId"
        );

        criarSessao(usuario, session);

        return "redirect:/usuario";
    }

    // =====================================================
    // LOGIN DA UNIVERSIDADE
    // =====================================================

    @PostMapping("/login/universidade")
    public String loginUniversidade(
            @RequestParam String email,
            @RequestParam String credencial,
            Model model,
            HttpSession session) {

        // =============================================
        // VERIFICA A CREDENCIAL
        // =============================================

        if (!credencialUniversidade.equals(credencial)) {

            model.addAttribute(
                    "erro",
                    "Credencial institucional inválida."
            );

            return "login";
        }

        String emailNormalizado =
                email.trim().toLowerCase();

        Optional<Usuario> resultado =
                usuarioRepository.findByEmail(
                        emailNormalizado
                );

        Usuario usuario;

        // =============================================
        // UNIVERSIDADE AINDA NÃO EXISTE
        // CRIA AUTOMATICAMENTE
        // =============================================

        if (resultado.isEmpty()) {

            usuario = new Usuario();

            usuario.setEmail(emailNormalizado);

            usuario.setNome(
                    "Universidade"
            );

            usuario.setPerfil(
                    "UNIVERSIDADE"
            );

            usuario.setCadastroFinalizado(
                    true
            );

            usuarioRepository.save(usuario);

        } else {

            usuario = resultado.get();

            // =========================================
            // EVITA TRANSFORMAR ALUNO EM UNIVERSIDADE
            // =========================================

            if (!"UNIVERSIDADE".equals(
                    usuario.getPerfil())) {

                model.addAttribute(
                        "erro",
                        "Este e-mail pertence a um aluno."
                );

                return "login";
            }
        }

        criarSessao(usuario, session);

        return "redirect:/universidade";
    }

    // =====================================================
    // CRIA A SESSÃO DO USUÁRIO
    // =====================================================

    private void criarSessao(
            Usuario usuario,
            HttpSession session) {

        session.setAttribute(
                "usuario",
                usuario
        );

        session.setAttribute(
                "usuarioId",
                usuario.getId()
        );

        session.setAttribute(
                "perfil",
                usuario.getPerfil()
        );
    }

    // =====================================================
    // ÁREA DO ALUNO
    // =====================================================

    @GetMapping("/usuario")
    public String usuario(
            HttpSession session,
            Model model) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuario"
                );

        if (usuario == null) {

            return "redirect:/login";
        }

        if (!"ALUNO".equals(
                usuario.getPerfil())) {

            return "redirect:/universidade";
        }

        model.addAttribute(
                "usuario",
                usuario
        );

        return "usuario";
    }

    // =====================================================
    // ÁREA DA UNIVERSIDADE
    // =====================================================

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

    var atendimentos = atendimentoRepository.findAll();

    long total = atendimentos.size();

    long abertos = atendimentos.stream()
            .filter(a -> "ABERTO".equalsIgnoreCase(a.getStatus()))
            .count();

    long emAtendimento = atendimentos.stream()
            .filter(a -> "EM ATENDIMENTO".equalsIgnoreCase(a.getStatus()))
            .count();

    long resolvidos = atendimentos.stream()
            .filter(a -> "RESOLVIDO".equalsIgnoreCase(a.getStatus()))
            .count();

    model.addAttribute("atendimentos", atendimentos);
    model.addAttribute("usuario", usuario);

    model.addAttribute("total", total);
    model.addAttribute("abertos", abertos);
    model.addAttribute("emAtendimento", emAtendimento);
    model.addAttribute("resolvidos", resolvidos);

    return "universidade";
}

    // =====================================================
    // LOGOUT
    // =====================================================

    @GetMapping("/logout")
    public String logout(
            HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }
}