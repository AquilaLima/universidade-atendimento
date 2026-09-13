package universidade_atendimento;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final UsuarioRepository usuarioRepository;

    public AdminController(
            UsuarioRepository usuarioRepository) {

        this.usuarioRepository =
                usuarioRepository;
    }

    // =========================================================
    // PAINEL ADMINISTRATIVO
    // =========================================================

    @GetMapping("/admin")
    public String painelAdmin(
            @RequestParam(
                    required = false,
                    defaultValue = ""
            ) String busca,
            HttpSession session,
            Model model) {

        Usuario admin =
                obterAdminAtivo(session);

        if (admin == null) {
            session.invalidate();
            return "redirect:/login";
        }

        List<Usuario> todosUsuarios =
                usuarioRepository.findAll();

        List<Usuario> usuarios =
                todosUsuarios;

        // =====================================================
        // BUSCA POR NOME OU E-MAIL
        // =====================================================

        if (busca != null
                && !busca.isBlank()) {

            String termo =
                    busca
                            .trim()
                            .toLowerCase(
                                    Locale.ROOT
                            );

            usuarios =
                    todosUsuarios
                            .stream()
                            .filter(usuario -> {

                                String nome =
                                        usuario.getNome() == null
                                        ? ""
                                        : usuario
                                                .getNome()
                                                .toLowerCase(
                                                        Locale.ROOT
                                                );

                                String email =
                                        usuario.getEmail() == null
                                        ? ""
                                        : usuario
                                                .getEmail()
                                                .toLowerCase(
                                                        Locale.ROOT
                                                );

                                return nome.contains(termo)
                                        || email.contains(termo);
                            })
                            .toList();
        }

        model.addAttribute(
                "usuarios",
                usuarios
        );

        model.addAttribute(
                "admin",
                admin
        );

        model.addAttribute(
                "busca",
                busca
        );

        // =====================================================
        // INDICADORES
        // =====================================================

        long totalUsuarios =
                todosUsuarios.size();

        long ativos =
                todosUsuarios
                        .stream()
                        .filter(
                                usuario ->
                                        Boolean.TRUE.equals(
                                                usuario.getAtivo()
                                        )
                        )
                        .count();

        long alunos =
                todosUsuarios
                        .stream()
                        .filter(
                                usuario ->
                                        "ALUNO".equals(
                                                usuario.getPerfil()
                                        )
                        )
                        .count();

        long universidade =
                todosUsuarios
                        .stream()
                        .filter(
                                usuario ->
                                        "UNIVERSIDADE".equals(
                                                usuario.getPerfil()
                                        )
                        )
                        .count();

        model.addAttribute(
                "totalUsuarios",
                totalUsuarios
        );

        model.addAttribute(
                "usuariosAtivos",
                ativos
        );

        model.addAttribute(
                "totalAlunos",
                alunos
        );

        model.addAttribute(
                "totalUniversidade",
                universidade
        );

        return "admin";
    }

    // =========================================================
    // TELA - NOVO USUÁRIO
    // =========================================================

    @GetMapping("/admin/usuarios/novo")
    public String novoUsuario(
            HttpSession session,
            Model model) {

        Usuario admin =
                obterAdminAtivo(session);

        if (admin == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute(
                "admin",
                admin
        );

        return "admin-usuario-novo";
    }

    // =========================================================
    // CADASTRAR USUÁRIO
    // =========================================================

    @PostMapping("/admin/usuarios/novo")
    public String salvarNovoUsuario(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String perfil,
            @RequestParam(
                    required = false
            ) String senha,
            HttpSession session,
            Model model) {

        Usuario admin =
                obterAdminAtivo(session);

        if (admin == null) {
            session.invalidate();
            return "redirect:/login";
        }

        String emailNormalizado =
                email
                        .trim()
                        .toLowerCase();

        // =====================================================
        // PERFIL VÁLIDO
        // =====================================================

        if (!perfil.equals("ALUNO")
                && !perfil.equals("UNIVERSIDADE")
                && !perfil.equals("ADM")) {

            model.addAttribute(
                    "erro",
                    "Perfil inválido."
            );

            model.addAttribute(
                    "admin",
                    admin
            );

            return "admin-usuario-novo";
        }

        // =====================================================
        // E-MAIL JÁ CADASTRADO
        // =====================================================

        Optional<Usuario> existente =
                usuarioRepository
                        .findByEmail(
                                emailNormalizado
                        );

        if (existente.isPresent()) {

            model.addAttribute(
                    "erro",
                    "Já existe um usuário cadastrado com este e-mail."
            );

            model.addAttribute(
                    "admin",
                    admin
            );

            return "admin-usuario-novo";
        }

        // =====================================================
        // ADM PRECISA DE SENHA
        // =====================================================

        if ("ADM".equals(perfil)
                && (senha == null
                || senha.isBlank())) {

            model.addAttribute(
                    "erro",
                    "Informe uma senha para o administrador."
            );

            model.addAttribute(
                    "admin",
                    admin
            );

            return "admin-usuario-novo";
        }

        Usuario usuario =
                new Usuario();

        usuario.setNome(
                nome == null
                ? null
                : nome.trim()
        );

        usuario.setEmail(
                emailNormalizado
        );

        usuario.setPerfil(
                perfil
        );

        usuario.setAtivo(
                true
        );

        // =====================================================
        // ALUNO
        // =====================================================

        if ("ALUNO".equals(perfil)) {

            usuario.setSenha(
                    null
            );

            usuario.setTelefone(
                    null
            );

            usuario.setCadastroFinalizado(
                    false
            );
        }

        // =====================================================
        // UNIVERSIDADE
        // =====================================================

        else if ("UNIVERSIDADE".equals(perfil)) {

            usuario.setSenha(
                    null
            );

            usuario.setCadastroFinalizado(
                    true
            );
        }

        // =====================================================
        // ADMINISTRADOR
        // =====================================================

        else if ("ADM".equals(perfil)) {

            usuario.setSenha(
                    senha
            );

            usuario.setCadastroFinalizado(
                    true
            );
        }

        usuarioRepository.save(
                usuario
        );

        return "redirect:/admin";
    }

    // =========================================================
    // TELA - EDITAR USUÁRIO
    // =========================================================

    @GetMapping("/admin/usuarios/{id}/editar")
    public String editarUsuario(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Usuario admin =
                obterAdminAtivo(session);

        if (admin == null) {
            session.invalidate();
            return "redirect:/login";
        }

        Usuario usuario =
                usuarioRepository
                        .findById(id)
                        .orElse(null);

        if (usuario == null) {
            return "redirect:/admin";
        }

        model.addAttribute(
                "usuario",
                usuario
        );

        model.addAttribute(
                "admin",
                admin
        );

        return "admin-usuario-editar";
    }

    // =========================================================
    // SALVAR ALTERAÇÃO DO USUÁRIO
    // =========================================================

    @PostMapping("/admin/usuarios/{id}/editar")
    public String salvarEdicaoUsuario(
            @PathVariable Long id,
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String perfil,
            @RequestParam(
                    required = false
            ) String senha,
            HttpSession session,
            Model model) {

        Usuario admin =
                obterAdminAtivo(session);

        if (admin == null) {
            session.invalidate();
            return "redirect:/login";
        }

        Usuario usuario =
                usuarioRepository
                        .findById(id)
                        .orElse(null);

        if (usuario == null) {
            return "redirect:/admin";
        }

        // =====================================================
        // NÃO PERMITE ALTERAR O PRÓPRIO PERFIL
        // =====================================================

        if (usuario.getId()
                .equals(admin.getId())
                && !"ADM".equals(perfil)) {

            model.addAttribute(
                    "erro",
                    "Você não pode remover o perfil ADM da sua própria conta."
            );

            model.addAttribute(
                    "usuario",
                    usuario
            );

            model.addAttribute(
                    "admin",
                    admin
            );

            return "admin-usuario-editar";
        }

        if (!perfil.equals("ALUNO")
                && !perfil.equals("UNIVERSIDADE")
                && !perfil.equals("ADM")) {

            model.addAttribute(
                    "erro",
                    "Perfil inválido."
            );

            model.addAttribute(
                    "usuario",
                    usuario
            );

            model.addAttribute(
                    "admin",
                    admin
            );

            return "admin-usuario-editar";
        }

        String emailNormalizado =
                email
                        .trim()
                        .toLowerCase();

        Optional<Usuario> usuarioComEmail =
                usuarioRepository
                        .findByEmail(
                                emailNormalizado
                        );

        // =====================================================
        // EVITAR E-MAIL DUPLICADO
        // =====================================================

        if (usuarioComEmail.isPresent()
                && !usuarioComEmail
                        .get()
                        .getId()
                        .equals(usuario.getId())) {

            model.addAttribute(
                    "erro",
                    "Este e-mail já pertence a outro usuário."
            );

            model.addAttribute(
                    "usuario",
                    usuario
            );

            model.addAttribute(
                    "admin",
                    admin
            );

            return "admin-usuario-editar";
        }

        String perfilAnterior =
                usuario.getPerfil();

        usuario.setNome(
                nome.trim()
        );

        usuario.setEmail(
                emailNormalizado
        );

        usuario.setPerfil(
                perfil
        );

        // =====================================================
        // ALTEROU PARA ALUNO
        // =====================================================

        if ("ALUNO".equals(perfil)
                && !"ALUNO".equals(
                        perfilAnterior)) {

            usuario.setSenha(
                    null
            );

            usuario.setTelefone(
                    null
            );

            usuario.setCadastroFinalizado(
                    false
            );

            usuario.setCodigoAcesso(
                    null
            );

            usuario.setCodigoAcessoExpiraEm(
                    null
            );
        }

        // =====================================================
        // ALTEROU PARA UNIVERSIDADE
        // =====================================================

        if ("UNIVERSIDADE".equals(perfil)) {

            usuario.setSenha(
                    null
            );

            usuario.setCadastroFinalizado(
                    true
            );

            usuario.setCodigoAcesso(
                    null
            );

            usuario.setCodigoAcessoExpiraEm(
                    null
            );
        }

        // =====================================================
        // PERFIL ADM
        // =====================================================

        if ("ADM".equals(perfil)) {

            usuario.setCadastroFinalizado(
                    true
            );

            usuario.setCodigoAcesso(
                    null
            );

            usuario.setCodigoAcessoExpiraEm(
                    null
            );

            if (senha != null
                    && !senha.isBlank()) {

                usuario.setSenha(
                        senha
                );
            }

            if (usuario.getSenha() == null
                    || usuario
                            .getSenha()
                            .isBlank()) {

                model.addAttribute(
                        "erro",
                        "O perfil ADM precisa possuir uma senha."
                );

                model.addAttribute(
                        "usuario",
                        usuario
                );

                model.addAttribute(
                        "admin",
                        admin
                );

                return "admin-usuario-editar";
            }
        }

        usuarioRepository.save(
                usuario
        );

        return "redirect:/admin";
    }

    // =========================================================
    // ATIVAR / DESATIVAR USUÁRIO
    // =========================================================

    @PostMapping("/admin/usuarios/{id}/status")
    public String alterarStatusUsuario(
            @PathVariable Long id,
            HttpSession session) {

        Usuario admin =
                obterAdminAtivo(session);

        if (admin == null) {
            session.invalidate();
            return "redirect:/login";
        }

        Usuario usuario =
                usuarioRepository
                        .findById(id)
                        .orElse(null);

        if (usuario == null) {
            return "redirect:/admin";
        }

        // =====================================================
        // NÃO PODE DESATIVAR A PRÓPRIA CONTA
        // =====================================================

        if (usuario
                .getId()
                .equals(admin.getId())) {

            return "redirect:/admin";
        }

        usuario.setAtivo(
                !Boolean.TRUE.equals(
                        usuario.getAtivo()
                )
        );

        // =====================================================
        // DESATIVA CÓDIGO TEMPORÁRIO
        // =====================================================

        if (!Boolean.TRUE.equals(
                usuario.getAtivo())) {

            usuario.setCodigoAcesso(
                    null
            );

            usuario.setCodigoAcessoExpiraEm(
                    null
            );
        }

        usuarioRepository.save(
                usuario
        );

        return "redirect:/admin";
    }

    // =========================================================
    // VALIDAR ADMINISTRADOR
    // =========================================================

    private Usuario obterAdminAtivo(
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

        if (!"ADM".equals(
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
}