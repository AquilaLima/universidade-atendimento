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
import java.time.LocalDateTime;

@Controller
public class AtendimentoController {

    private final AtendimentoRepository repository;
    private final BlockchainService blockchainService;

    public AtendimentoController(
        AtendimentoRepository repository,
        BlockchainService blockchainService) {

    this.repository = repository;
    this.blockchainService = blockchainService;
}

    // =========================================================
    // TELA INICIAL
    // =========================================================

    @GetMapping("/")
public String inicio(
        Model model,
        HttpSession session) {

    Usuario usuario = (Usuario) session.getAttribute("usuario");

    if (usuario == null) {
        return "redirect:/login";
    }

    if (!"UNIVERSIDADE".equals(usuario.getPerfil())) {
        return "redirect:/usuario";
    }

    List<Atendimento> atendimentos = repository.findAll();

    long total = atendimentos.size();

    long abertos = atendimentos.stream()
            .filter(a -> "ABERTO".equals(a.getStatus()))
            .count();

    long emAtendimento = atendimentos.stream()
            .filter(a -> "EM ATENDIMENTO".equals(a.getStatus()))
            .count();

    long resolvidos = atendimentos.stream()
            .filter(a -> "RESOLVIDO".equals(a.getStatus()))
            .count();

    model.addAttribute("atendimentos", atendimentos);
    model.addAttribute("atendimento", new Atendimento());

    model.addAttribute("total", total);
    model.addAttribute("abertos", abertos);
    model.addAttribute("emAtendimento", emAtendimento);
    model.addAttribute("resolvidos", resolvidos);

    return "index";
}

    // =========================================================
    // ABRIR NOVO ATENDIMENTO
    // =========================================================

    @GetMapping("/atendimentos/novo")
    public String novoAtendimento(
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        // Somente ALUNO pode abrir atendimento
        if (!"ALUNO".equals(usuario.getPerfil())) {
            return "redirect:/universidade";
        }

        Atendimento atendimento = new Atendimento();

        atendimento.setNome(usuario.getNome());
        atendimento.setEmail(usuario.getEmail());

        model.addAttribute("atendimento", atendimento);

        return "novo-atendimento";
    }

    // =========================================================
    // SALVAR ATENDIMENTO
    // =========================================================

    @PostMapping("/atendimentos")
    public String salvar(
            @ModelAttribute Atendimento atendimento,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        // Somente ALUNO pode criar atendimento
        if (!"ALUNO".equals(usuario.getPerfil())) {
            return "redirect:/universidade";
        }

        String protocolo =
                "AT-" + Year.now().getValue() + "-"
                + System.currentTimeMillis();

        atendimento.setProtocolo(protocolo);

        atendimento.setUsuario(usuario);

        atendimento.setNome(usuario.getNome());

        atendimento.setEmail(usuario.getEmail());

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

        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        Atendimento atendimento =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Atendimento não encontrado"));

        // =====================================================
        // ALUNO
        // =====================================================

        if ("ALUNO".equals(usuario.getPerfil())) {

            // O aluno só pode visualizar seus próprios chamados
            if (atendimento.getUsuario() == null
                    || !atendimento.getUsuario()
                            .getId()
                            .equals(usuario.getId())) {

                return "redirect:/usuario";
            }

            model.addAttribute("voltar", "/usuario");

            model.addAttribute("podeAtualizar", false);
        }

        // =====================================================
        // UNIVERSIDADE
        // =====================================================

        else if ("UNIVERSIDADE".equals(usuario.getPerfil())) {

            model.addAttribute("voltar", "/universidade");

            model.addAttribute("podeAtualizar", true);
        }

        else {
            return "redirect:/login";
        }

        model.addAttribute("atendimento", atendimento);

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
        (Usuario) session.getAttribute("usuario");

if (usuario == null) {
    return "redirect:/login";
}

        // Somente UNIVERSIDADE pode alterar status
        if (!"UNIVERSIDADE".equals(usuario.getPerfil())) {
            return "redirect:/usuario";
        }

        Atendimento atendimento =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Atendimento não encontrado"));

        atendimento.setStatus(status);

        repository.save(atendimento);
        
        blockchainService.registrarEvento(
        "STATUS_ALTERADO",
        atendimento.getId(),
        usuario.getId(),
        "Status alterado para: " + status
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
                (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        // Somente ALUNO
        if (!"ALUNO".equals(usuario.getPerfil())) {
            return "redirect:/universidade";
        }

        List<Atendimento> atendimentos =
                repository.findByUsuario(usuario);

        model.addAttribute("atendimentos", atendimentos);

        return "usuario";
    }

    // =========================================================
    // INDICADORES DA UNIVERSIDADE
    // =========================================================

    private void adicionarIndicadores(
            Model model,
            List<Atendimento> atendimentos) {

        long total = atendimentos.size();

        long abertos = atendimentos.stream()
                .filter(a ->
                        "ABERTO".equals(a.getStatus()))
                .count();

        long emAtendimento = atendimentos.stream()
                .filter(a ->
                        "EM ATENDIMENTO".equals(a.getStatus()))
                .count();

        long resolvidos = atendimentos.stream()
                .filter(a ->
                        "RESOLVIDO".equals(a.getStatus()))
                .count();

        model.addAttribute("total", total);

        model.addAttribute("abertos", abertos);

        model.addAttribute(
                "emAtendimento",
                emAtendimento);

        model.addAttribute(
                "resolvidos",
                resolvidos);
    }
@PostMapping("/atendimentos/{id}/resposta")
public String responderAtendimento(
        @PathVariable Long id,
        @RequestParam String resposta,
        HttpSession session) {

    Usuario usuario = (Usuario) session.getAttribute("usuario");

    // Usuário não está logado
    if (usuario == null) {
        return "redirect:/login";
    }

    // Somente universidade pode responder
    if (!"UNIVERSIDADE".equals(usuario.getPerfil())) {
        return "redirect:/usuario";
    }

    Atendimento atendimento = repository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Atendimento não encontrado"));

    // Validação da resposta
    if (resposta == null || resposta.isBlank()) {
        return "redirect:/atendimentos/" + id;
    }

    atendimento.setResposta(resposta);
    atendimento.setDataResposta(LocalDateTime.now());

    // Ao responder, o chamado passa para EM ATENDIMENTO
    if ("ABERTO".equals(atendimento.getStatus())) {
        atendimento.setStatus("EM ATENDIMENTO");
    }

    repository.save(atendimento);
    
    blockchainService.registrarEvento( "RESPOSTA_ADICIONADA", atendimento.getId(),usuario.getId(),"Resposta adicionada ao atendimento " + atendimento.getProtocolo()
    );
                

    return "redirect:/atendimentos/" + id;
}
}