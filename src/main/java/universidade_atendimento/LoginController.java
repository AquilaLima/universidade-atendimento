package universidade_atendimento;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final EmailService emailService;

    public LoginController(
            UsuarioRepository usuarioRepository,
            AtendimentoRepository atendimentoRepository,
            EmailService emailService) {

        this.usuarioRepository = usuarioRepository;
        this.atendimentoRepository = atendimentoRepository;
        this.emailService = emailService;
    }

    // =====================================================
    // PÁGINA INICIAL
    // =====================================================

    @GetMapping("/")
    public String inicio(HttpSession session) {

        Long usuarioId =
                (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuario =
                usuarioRepository.findById(usuarioId)
                        .orElse(null);

        if (usuario == null
                || !Boolean.TRUE.equals(usuario.getAtivo())) {

            session.invalidate();

            return "redirect:/login";
        }

        if ("ADM".equals(usuario.getPerfil())) {
            return "redirect:/admin";
        }

        if ("UNIVERSIDADE".equals(usuario.getPerfil())) {
            return "redirect:/universidade";
        }

        if ("ALUNO".equals(usuario.getPerfil())) {
            return "redirect:/usuario";
        }

        session.invalidate();

        return "redirect:/login";
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
    // =====================================================

    @PostMapping("/login/aluno")
    public String loginAluno(
            @RequestParam String email,
            Model model,
            HttpSession session) {

        String emailNormalizado =
                email.trim().toLowerCase();

        Optional<Usuario> resultado =
                usuarioRepository.findByEmail(
                        emailNormalizado
                );

        if (resultado.isEmpty()) {

            model.addAttribute(
                    "erro",
                    "E-mail institucional não encontrado."
            );

            return "login";
        }

        Usuario usuario =
                resultado.get();

        if (!Boolean.TRUE.equals(
                usuario.getAtivo())) {

            model.addAttribute(
                    "erro",
                    "Este usuário está desativado. Procure a instituição."
            );

            return "login";
        }

        if (!"ALUNO".equals(
                usuario.getPerfil())) {

            model.addAttribute(
                    "erro",
                    "Este e-mail não pertence a um aluno."
            );

            return "login";
        }

        // =================================================
        // PRIMEIRO ACESSO
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
        // =================================================

        session.setAttribute(
                "loginAlunoId",
                usuario.getId()
        );

        return "redirect:/login/aluno/senha";
    }

    // =====================================================
    // TELA DE SENHA DO ALUNO
    // =====================================================

    @GetMapping("/login/aluno/senha")
    public String telaSenhaAluno(
            HttpSession session) {

        Long usuarioId =
                (Long) session.getAttribute(
                        "loginAlunoId"
                );

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
                (Long) session.getAttribute(
                        "loginAlunoId"
                );

        if (usuarioId == null) {

            return "redirect:/login";
        }

        Usuario usuario =
                usuarioRepository.findById(usuarioId)
                        .orElse(null);

        if (usuario == null) {

            session.removeAttribute(
                    "loginAlunoId"
            );

            return "redirect:/login";
        }

        if (!Boolean.TRUE.equals(
                usuario.getAtivo())) {

            session.invalidate();

            return "redirect:/login";
        }

        if (!"ALUNO".equals(
                usuario.getPerfil())) {

            session.invalidate();

            return "redirect:/login";
        }

        if (usuario.getSenha() == null
                || !usuario.getSenha().equals(senha)) {

            model.addAttribute(
                    "erro",
                    "Senha inválida."
            );

            return "login-aluno-senha";
        }

        session.removeAttribute(
                "loginAlunoId"
        );

        criarSessao(
                usuario,
                session
        );

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

        if (!Boolean.TRUE.equals(
                usuario.getAtivo())) {

            session.invalidate();

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

        if (!Boolean.TRUE.equals(
                usuario.getAtivo())) {

            session.invalidate();

            return "redirect:/login";
        }

        if (!"ALUNO".equals(
                usuario.getPerfil())) {

            return "redirect:/login";
        }

        usuario.setNome(
                nome.trim()
        );

        usuario.setTelefone(
                telefone.trim()
        );

        usuario.setSenha(
                senha
        );

        usuario.setCadastroFinalizado(
                true
        );

        usuarioRepository.save(
                usuario
        );

        session.removeAttribute(
                "preCadastroUsuarioId"
        );

        criarSessao(
                usuario,
                session
        );

        return "redirect:/usuario";
    }

    // =====================================================
// LOGIN DA UNIVERSIDADE
// ENVIA CÓDIGO PARA O E-MAIL
// =====================================================

@PostMapping("/login/universidade")
public String loginUniversidade(
        @RequestParam String email,
        Model model,
        HttpSession session) {

    long inicioTotal =
            System.currentTimeMillis();

    System.out.println(
            "[LOGIN UNIVERSIDADE] 1 - Início: "
            + LocalDateTime.now()
    );

    String emailNormalizado =
            email
                    .trim()
                    .toLowerCase();

    // =================================================
    // CONSULTA USUÁRIO
    // =================================================

    long inicioBanco =
            System.currentTimeMillis();

    Optional<Usuario> resultado =
            usuarioRepository.findByEmail(
                    emailNormalizado
            );

    long fimBanco =
            System.currentTimeMillis();

    System.out.println(
            "[LOGIN UNIVERSIDADE] 2 - Consulta usuário: "
            + (fimBanco - inicioBanco)
            + " ms"
    );

    if (resultado.isEmpty()) {

        System.out.println(
                "[LOGIN UNIVERSIDADE] E-mail não encontrado."
        );

        model.addAttribute(
                "erro",
                "E-mail institucional não autorizado."
        );

        return "login";
    }

    Usuario usuario =
            resultado.get();

    // =================================================
    // VALIDAR PERFIL
    // =================================================

    if (!"UNIVERSIDADE".equals(
            usuario.getPerfil())) {

        model.addAttribute(
                "erro",
                "Este e-mail não pertence a um funcionário da Universidade."
        );

        return "login";
    }

    // =================================================
    // VALIDAR USUÁRIO ATIVO
    // =================================================

    if (!Boolean.TRUE.equals(
            usuario.getAtivo())) {

        model.addAttribute(
                "erro",
                "Este acesso está desativado. Procure o administrador."
        );

        return "login";
    }

    // =================================================
    // GERAR CÓDIGO
    // =================================================

    String codigo =
            String.format(
                    "%06d",
                    new Random()
                            .nextInt(1000000)
            );

    usuario.setCodigoAcesso(
            codigo
    );

    usuario.setCodigoAcessoExpiraEm(
            LocalDateTime.now()
                    .plusMinutes(10)
    );

    // =================================================
    // SALVAR CÓDIGO NO BANCO
    // =================================================

    long inicioSalvar =
            System.currentTimeMillis();

    usuarioRepository.save(
            usuario
    );

    long fimSalvar =
            System.currentTimeMillis();

    System.out.println(
            "[LOGIN UNIVERSIDADE] 3 - Salvar código no banco: "
            + (fimSalvar - inicioSalvar)
            + " ms"
    );

    // =================================================
    // ENVIAR E-MAIL
    // =================================================

    try {

        System.out.println(
                "[LOGIN UNIVERSIDADE] 4 - Iniciando envio do e-mail: "
                + LocalDateTime.now()
        );

        long inicioEmail =
                System.currentTimeMillis();

        emailService.enviarCodigoAcesso(
                usuario.getEmail(),
                codigo
        );

        long fimEmail =
                System.currentTimeMillis();

        System.out.println(
                "[LOGIN UNIVERSIDADE] 5 - Envio do e-mail concluído em: "
                + (fimEmail - inicioEmail)
                + " ms"
        );

    } catch (Exception e) {

        System.out.println(
                "[LOGIN UNIVERSIDADE] ERRO AO ENVIAR E-MAIL"
        );

        e.printStackTrace();

        usuario.setCodigoAcesso(
                null
        );

        usuario.setCodigoAcessoExpiraEm(
                null
        );

        usuarioRepository.save(
                usuario
        );

        model.addAttribute(
                "erro",
                "Não foi possível enviar o código de acesso."
        );

        return "login";
    }

    // =================================================
    // PREPARAR SESSÃO TEMPORÁRIA
    // =================================================

    session.setAttribute(
            "loginUniversidadeId",
            usuario.getId()
    );

    long fimTotal =
            System.currentTimeMillis();

    System.out.println(
            "[LOGIN UNIVERSIDADE] 6 - Tempo total: "
            + (fimTotal - inicioTotal)
            + " ms"
    );

    System.out.println(
            "[LOGIN UNIVERSIDADE] 7 - Redirecionando para tela do código."
    );

    return "redirect:/login/universidade/codigo";
}

    // =====================================================
    // TELA DO CÓDIGO DA UNIVERSIDADE
    // =====================================================

    @GetMapping("/login/universidade/codigo")
    public String telaCodigoUniversidade(
            HttpSession session) {

        Long usuarioId =
                (Long) session.getAttribute(
                        "loginUniversidadeId"
                );

        if (usuarioId == null) {

            return "redirect:/login";
        }

        Usuario usuario =
                usuarioRepository.findById(usuarioId)
                        .orElse(null);

        if (usuario == null
                || !"UNIVERSIDADE".equals(usuario.getPerfil())
                || !Boolean.TRUE.equals(usuario.getAtivo())) {

            session.removeAttribute(
                    "loginUniversidadeId"
            );

            return "redirect:/login";
        }

        return "login-universidade-codigo";
    }

    // =====================================================
    // VALIDAR CÓDIGO DA UNIVERSIDADE
    // =====================================================

    @PostMapping("/login/universidade/codigo")
    public String validarCodigoUniversidade(
            @RequestParam String codigo,
            HttpSession session,
            Model model) {

        Long usuarioId =
                (Long) session.getAttribute(
                        "loginUniversidadeId"
                );

        if (usuarioId == null) {

            return "redirect:/login";
        }

        Usuario usuario =
                usuarioRepository.findById(usuarioId)
                        .orElse(null);

        if (usuario == null) {

            session.removeAttribute(
                    "loginUniversidadeId"
            );

            return "redirect:/login";
        }

        if (!Boolean.TRUE.equals(
                usuario.getAtivo())) {

            session.invalidate();

            return "redirect:/login";
        }

        if (!"UNIVERSIDADE".equals(
                usuario.getPerfil())) {

            session.invalidate();

            return "redirect:/login";
        }

        if (usuario.getCodigoAcesso() == null
                || usuario.getCodigoAcessoExpiraEm() == null) {

            model.addAttribute(
                    "erro",
                    "Código de acesso inválido. Solicite um novo código."
            );

            return "login-universidade-codigo";
        }

        // Verifica se expirou
        if (LocalDateTime.now().isAfter(
                usuario.getCodigoAcessoExpiraEm())) {

            usuario.setCodigoAcesso(
                    null
            );

            usuario.setCodigoAcessoExpiraEm(
                    null
            );

            usuarioRepository.save(
                    usuario
            );

            model.addAttribute(
                    "erro",
                    "O código expirou. Solicite um novo código."
            );

            return "login-universidade-codigo";
        }

        // Verifica o código digitado
        if (!usuario.getCodigoAcesso()
                .equals(codigo.trim())) {

            model.addAttribute(
                    "erro",
                    "Código inválido."
            );

            return "login-universidade-codigo";
        }

        // Código só pode ser usado uma vez
        usuario.setCodigoAcesso(
                null
        );

        usuario.setCodigoAcessoExpiraEm(
                null
        );

        usuarioRepository.save(
                usuario
        );

        session.removeAttribute(
                "loginUniversidadeId"
        );

        criarSessao(
                usuario,
                session
        );

        return "redirect:/universidade";
    }

    // =====================================================
    // LOGIN DO ADMINISTRADOR
    // =====================================================

    @PostMapping("/login/admin")
    public String loginAdmin(
            @RequestParam String email,
            @RequestParam String senha,
            Model model,
            HttpSession session) {

        String emailNormalizado =
                email.trim().toLowerCase();

        Optional<Usuario> resultado =
                usuarioRepository.findByEmail(
                        emailNormalizado
                );

        if (resultado.isEmpty()) {

            model.addAttribute(
                    "erro",
                    "Administrador não encontrado."
            );

            return "login";
        }

        Usuario usuario =
                resultado.get();

        if (!"ADM".equals(
                usuario.getPerfil())) {

            model.addAttribute(
                    "erro",
                    "Este usuário não possui perfil de administrador."
            );

            return "login";
        }

        if (!Boolean.TRUE.equals(
                usuario.getAtivo())) {

            model.addAttribute(
                    "erro",
                    "Este usuário está desativado."
            );

            return "login";
        }

        if (usuario.getSenha() == null
                || !usuario.getSenha().equals(senha)) {

            model.addAttribute(
                    "erro",
                    "Senha inválida."
            );

            return "login";
        }

        criarSessao(
                usuario,
                session
        );

        return "redirect:/admin";
    }

    // =====================================================
    // CRIA A SESSÃO
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

        Long usuarioId =
                (Long) session.getAttribute(
                        "usuarioId"
                );

        if (usuarioId == null) {

            return "redirect:/login";
        }

        Usuario usuario =
                usuarioRepository.findById(usuarioId)
                        .orElse(null);

        if (usuario == null
                || !Boolean.TRUE.equals(usuario.getAtivo())) {

            session.invalidate();

            return "redirect:/login";
        }

        if (!"ALUNO".equals(
                usuario.getPerfil())) {

            return "redirect:/login";
        }

        // Atualiza o usuário salvo na sessão
        session.setAttribute(
                "usuario",
                usuario
        );

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

        Long usuarioId =
                (Long) session.getAttribute(
                        "usuarioId"
                );

        if (usuarioId == null) {

            return "redirect:/login";
        }

        Usuario usuario =
                usuarioRepository.findById(usuarioId)
                        .orElse(null);

        if (usuario == null
                || !Boolean.TRUE.equals(usuario.getAtivo())) {

            session.invalidate();

            return "redirect:/login";
        }

        if (!"UNIVERSIDADE".equals(
                usuario.getPerfil())) {

            return "redirect:/login";
        }

        // Atualiza usuário salvo na sessão
        session.setAttribute(
                "usuario",
                usuario
        );

        var atendimentos =
                atendimentoRepository.findAll();

        long total =
                atendimentos.size();

        long abertos =
                atendimentos.stream()
                        .filter(a ->
                                "ABERTO".equalsIgnoreCase(
                                        a.getStatus()
                                )
                        )
                        .count();

        long emAtendimento =
                atendimentos.stream()
                        .filter(a ->
                                "EM ATENDIMENTO".equalsIgnoreCase(
                                        a.getStatus()
                                )
                        )
                        .count();

        long resolvidos =
                atendimentos.stream()
                        .filter(a ->
                                "RESOLVIDO".equalsIgnoreCase(
                                        a.getStatus()
                                )
                        )
                        .count();

        model.addAttribute(
                "atendimentos",
                atendimentos
        );

        model.addAttribute(
                "usuario",
                usuario
        );

        model.addAttribute(
                "total",
                total
        );

        model.addAttribute(
                "abertos",
                abertos
        );

        model.addAttribute(
                "emAtendimento",
                emAtendimento
        );

        model.addAttribute(
                "resolvidos",
                resolvidos
        );

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