package universidade_atendimento;

import jakarta.servlet.http.HttpSession;

import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AtendimentoController {

    private final AtendimentoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final BlockchainService blockchainService;
    private final MensagemAtendimentoRepository mensagemRepository;

    public AtendimentoController(
            AtendimentoRepository repository,
            UsuarioRepository usuarioRepository,
            BlockchainService blockchainService,
            MensagemAtendimentoRepository mensagemRepository) {

        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.blockchainService = blockchainService;
        this.mensagemRepository = mensagemRepository;
    }

    // =========================================================
    // ABRIR NOVO ATENDIMENTO
    // =========================================================

    @GetMapping("/atendimentos/novo")
    public String novoAtendimento(
            HttpSession session,
            Model model) {

        Usuario usuario = obterAlunoAtivo(session);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        Atendimento atendimento = new Atendimento();

        atendimento.setNome(usuario.getNome());
        atendimento.setEmail(usuario.getEmail());
        atendimento.setTelefone(usuario.getTelefone());

        model.addAttribute(
                "atendimento",
                atendimento
        );

        model.addAttribute(
                "usuario",
                usuario
        );

        return "novo-atendimento";
    }

    // =========================================================
    // SALVAR ATENDIMENTO
    // =========================================================

    @PostMapping("/atendimentos")
    public String salvar(
            @ModelAttribute Atendimento atendimento,
            HttpSession session) {

        Usuario usuario = obterAlunoAtivo(session);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        String protocolo =
                "AT-"
                + Year.now().getValue()
                + "-"
                + System.currentTimeMillis();

        atendimento.setProtocolo(protocolo);

        atendimento.setUsuario(usuario);

        atendimento.setNome(usuario.getNome());
        atendimento.setEmail(usuario.getEmail());
        atendimento.setTelefone(usuario.getTelefone());

        repository.save(atendimento);

        blockchainService.registrarEvento(
                "ATENDIMENTO_CRIADO",
                atendimento.getId(),
                usuario.getId(),
                "Atendimento criado pelo aluno. Protocolo: "
                + atendimento.getProtocolo()
        );

        return "redirect:/usuario";
    }

    // =========================================================
    // DETALHES DO ATENDIMENTO
    // =========================================================

    @GetMapping("/atendimentos/{id}")
    public String detalhes(
            @PathVariable Long id,
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

        if (usuario == null
                || !Boolean.TRUE.equals(usuario.getAtivo())) {

            session.invalidate();
            return "redirect:/login";
        }

        session.setAttribute(
                "usuario",
                usuario
        );

        Atendimento atendimento =
                repository
                        .findById(id)
                        .orElse(null);

        if (atendimento == null) {
            return "redirect:/login";
        }

        // =====================================================
        // PERFIL ALUNO
        // =====================================================

        if ("ALUNO".equals(usuario.getPerfil())) {

            if (atendimento.getUsuario() == null
                    || !atendimento
                            .getUsuario()
                            .getId()
                            .equals(usuario.getId())) {

                return "redirect:/usuario";
            }

            model.addAttribute(
                    "voltar",
                    "/usuario"
            );

            model.addAttribute(
                    "podeAtualizar",
                    false
            );
        }

        // =====================================================
        // PERFIL UNIVERSIDADE
        // =====================================================

        else if ("UNIVERSIDADE".equals(
                usuario.getPerfil())) {

            model.addAttribute(
                    "voltar",
                    "/universidade"
            );

            model.addAttribute(
                    "podeAtualizar",
                    true
            );
        }

        // =====================================================
        // PERFIL INVÁLIDO
        // =====================================================

        else {
            return "redirect:/login";
        }

        // =====================================================
        // MENSAGENS
        // =====================================================

        List<MensagemAtendimento> mensagens =
                mensagemRepository
                        .findByAtendimentoOrderByDataHoraAsc(
                                atendimento
                        );

        model.addAttribute(
                "atendimento",
                atendimento
        );

        model.addAttribute(
                "usuario",
                usuario
        );

        model.addAttribute(
                "mensagens",
                mensagens
        );

        return "detalhes";
    }

    // =========================================================
    // ATUALIZAR STATUS
    // =========================================================

    @PostMapping("/atendimentos/{id}/status")
    public String atualizarStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpSession session) {

        Usuario usuario =
                obterUniversidadeAtiva(session);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        Atendimento atendimento =
                repository
                        .findById(id)
                        .orElse(null);

        if (atendimento == null) {
            return "redirect:/universidade";
        }

        List<String> statusPermitidos =
                List.of(
                        "ABERTO",
                        "EM ATENDIMENTO",
                        "RESOLVIDO",
                        "FECHADO",
                        "CANCELADO"
                );

        if (!statusPermitidos.contains(status)) {
            return "redirect:/atendimentos/" + id;
        }

        String statusAnterior =
                atendimento.getStatus();

        atendimento.setStatus(status);

        repository.save(atendimento);

        blockchainService.registrarEvento(
                "STATUS_ALTERADO",
                atendimento.getId(),
                usuario.getId(),
                "Status alterado de "
                + statusAnterior
                + " para "
                + status
        );

        return "redirect:/atendimentos/" + id;
    }

    // =========================================================
    // MEUS ATENDIMENTOS
    // =========================================================

    @GetMapping("/meus-atendimentos")
    public String meusAtendimentos(
            HttpSession session,
            Model model) {

        Usuario usuario =
                obterAlunoAtivo(session);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        List<Atendimento> atendimentos =
                repository.findByUsuario(usuario);

        model.addAttribute(
                "atendimentos",
                atendimentos
        );

        model.addAttribute(
                "usuario",
                usuario
        );

        return "usuario";
    }

    // =========================================================
// ENVIAR MENSAGEM
// =========================================================

@PostMapping("/atendimentos/{id}/mensagem")
public String enviarMensagem(
        @PathVariable Long id,
        @RequestParam String mensagem,
        HttpSession session) {

    Long usuarioId =
            (Long) session.getAttribute(
                    "usuarioId"
            );

    if (usuarioId == null) {
        return "redirect:/login";
    }

    Usuario usuario =
            usuarioRepository
                    .findById(usuarioId)
                    .orElse(null);

    if (usuario == null
            || !Boolean.TRUE.equals(
                    usuario.getAtivo())) {

        session.invalidate();

        return "redirect:/login";
    }

    // =====================================================
    // SOMENTE ALUNO OU UNIVERSIDADE PODE ENVIAR MENSAGEM
    // =====================================================

    if (!"ALUNO".equals(usuario.getPerfil())
            && !"UNIVERSIDADE".equals(
                    usuario.getPerfil())) {

        return "redirect:/login";
    }

    Atendimento atendimento =
            repository
                    .findById(id)
                    .orElse(null);

    if (atendimento == null) {

        if ("ALUNO".equals(
                usuario.getPerfil())) {

            return "redirect:/usuario";
        }

        return "redirect:/universidade";
    }

    // =====================================================
    // ALUNO SÓ PODE ACESSAR O PRÓPRIO ATENDIMENTO
    // =====================================================

    if ("ALUNO".equals(
            usuario.getPerfil())) {

        if (atendimento.getUsuario() == null
                || !atendimento
                        .getUsuario()
                        .getId()
                        .equals(usuario.getId())) {

            return "redirect:/usuario";
        }
    }

    // =====================================================
    // NÃO ACEITA MENSAGEM APÓS FECHAMENTO OU CANCELAMENTO
    // =====================================================

    String statusAtual =
            atendimento.getStatus();

    if ("FECHADO".equalsIgnoreCase(statusAtual)
            || "CANCELADO".equalsIgnoreCase(statusAtual)) {

        return "redirect:/atendimentos/" + id;
    }

    // =====================================================
    // VALIDAR MENSAGEM
    // =====================================================

    if (mensagem == null
            || mensagem.isBlank()) {

        return "redirect:/atendimentos/" + id;
    }

    // =====================================================
    // SALVAR MENSAGEM
    // =====================================================

    MensagemAtendimento novaMensagem =
            new MensagemAtendimento();

    novaMensagem.setAtendimento(
            atendimento
    );

    novaMensagem.setUsuario(
            usuario
    );

    novaMensagem.setMensagem(
            mensagem.trim()
    );

    mensagemRepository.save(
            novaMensagem
    );

    // =====================================================
    // ALUNO RESPONDE CHAMADO RESOLVIDO
    // VOLTA AUTOMATICAMENTE PARA EM ATENDIMENTO
    // =====================================================

    if ("ALUNO".equals(usuario.getPerfil())
            && "RESOLVIDO".equalsIgnoreCase(
                    atendimento.getStatus())) {

        atendimento.setStatus(
                "EM ATENDIMENTO"
        );

        repository.save(
                atendimento
        );

        blockchainService.registrarEvento(
                "ATENDIMENTO_REABERTO",
                atendimento.getId(),
                usuario.getId(),
                "Atendimento reaberto automaticamente "
                + "após nova mensagem do aluno. "
                + "Status alterado de RESOLVIDO "
                + "para EM ATENDIMENTO."
        );
    }

    // =====================================================
    // PRIMEIRA RESPOSTA DA UNIVERSIDADE
    // ABERTO -> EM ATENDIMENTO
    // =====================================================

    if ("UNIVERSIDADE".equals(
            usuario.getPerfil())
            && "ABERTO".equalsIgnoreCase(
                    atendimento.getStatus())) {

        atendimento.setStatus(
                "EM ATENDIMENTO"
        );

        repository.save(
                atendimento
        );

        blockchainService.registrarEvento(
                "STATUS_ALTERADO",
                atendimento.getId(),
                usuario.getId(),
                "Status alterado automaticamente "
                + "de ABERTO para EM ATENDIMENTO"
        );
    }

    // =====================================================
    // AUDITORIA DA MENSAGEM
    // =====================================================

    blockchainService.registrarEvento(
            "MENSAGEM_ADICIONADA",
            atendimento.getId(),
            usuario.getId(),
            "Mensagem adicionada ao atendimento "
            + atendimento.getProtocolo()
    );

    return "redirect:/atendimentos/" + id;
}

    // =========================================================
    // REABRIR ATENDIMENTO
    // =========================================================

    @PostMapping("/atendimentos/{id}/reabrir")
    public String reabrirAtendimento(
            @PathVariable Long id,
            HttpSession session) {

        Usuario usuario =
                obterUniversidadeAtiva(session);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        Atendimento atendimento =
                repository
                        .findById(id)
                        .orElse(null);

        if (atendimento == null) {
            return "redirect:/universidade";
        }

        // SOMENTE RESOLVIDO PODE SER REABERTO
        if (!"RESOLVIDO".equalsIgnoreCase(
                atendimento.getStatus())) {

            return "redirect:/atendimentos/" + id;
        }

        atendimento.setStatus(
                "EM ATENDIMENTO"
        );

        repository.save(atendimento);

        blockchainService.registrarEvento(
                "ATENDIMENTO_REABERTO",
                atendimento.getId(),
                usuario.getId(),
                "Atendimento reaberto. "
                + "Status alterado de RESOLVIDO "
                + "para EM ATENDIMENTO."
        );

        return "redirect:/atendimentos/" + id;
    }

    // =========================================================
    // VALIDAR ALUNO ATIVO
    // =========================================================

    private Usuario obterAlunoAtivo(
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
    // VALIDAR UNIVERSIDADE ATIVA
    // =========================================================

    private Usuario obterUniversidadeAtiva(
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

        if (!"UNIVERSIDADE".equals(
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
    // INDICADORES DA UNIVERSIDADE
    // =========================================================

    private void adicionarIndicadores(
            Model model,
            List<Atendimento> atendimentos) {

        long total =
                atendimentos.size();

        long abertos =
                atendimentos.stream()
                        .filter(
                                a -> "ABERTO"
                                        .equalsIgnoreCase(
                                                a.getStatus()
                                        )
                        )
                        .count();

        long emAtendimento =
                atendimentos.stream()
                        .filter(
                                a -> "EM ATENDIMENTO"
                                        .equalsIgnoreCase(
                                                a.getStatus()
                                        )
                        )
                        .count();

        long resolvidos =
                atendimentos.stream()
                        .filter(
                                a -> "RESOLVIDO"
                                        .equalsIgnoreCase(
                                                a.getStatus()
                                        )
                        )
                        .count();

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
    }
}