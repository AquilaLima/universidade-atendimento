package universidade_atendimento;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CadastroController {

    private final UsuarioRepository usuarioRepository;

    // Credencial necessária para cadastro como universidade
    private static final String CREDENCIAL_UNIVERSIDADE = "UNI-2026-ADMIN";

    public CadastroController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String perfil,
            @RequestParam(required = false) String credencial,
            Model model) {

        // Verifica se o e-mail já existe
        if (usuarioRepository.findByEmail(email).isPresent()) {
            model.addAttribute("erro", "Este e-mail já está cadastrado.");
            return "cadastro";
        }

        // Validação da credencial da universidade
        if ("UNIVERSIDADE".equals(perfil)) {

            if (!CREDENCIAL_UNIVERSIDADE.equals(credencial)) {
                model.addAttribute(
                        "erro",
                        "Credencial de universidade inválida."
                );

                return "cadastro";
            }
        }

        // Cria o usuário
        Usuario usuario = new Usuario();

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(senha);
        usuario.setPerfil(perfil);

        usuarioRepository.save(usuario);

        return "redirect:/login";
    }
}