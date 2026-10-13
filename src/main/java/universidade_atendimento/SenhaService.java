package universidade_atendimento;

import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SenhaService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public SenhaService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String codificar(String senha) {
        if (senha == null || senha.isBlank()) {
            return senha;
        }

        // Evita codificar duas vezes uma senha que já esteja em BCrypt.
        if (ehHashBCrypt(senha)) {
            return senha;
        }

        return passwordEncoder.encode(senha);
    }

    @Transactional
    public boolean validarSenha(Usuario usuario, String senhaInformada) {
        if (usuario == null || usuario.getSenha() == null
                || senhaInformada == null) {
            return false;
        }

        String senhaArmazenada = usuario.getSenha();

        if (ehHashBCrypt(senhaArmazenada)) {
            return passwordEncoder.matches(senhaInformada, senhaArmazenada);
        }

        // Compatibilidade temporária: se ainda houver senha antiga em texto puro,
        // valida e converte imediatamente para BCrypt após o primeiro login válido.
        if (senhaArmazenada.equals(senhaInformada)) {
            usuario.setSenha(passwordEncoder.encode(senhaInformada));
            usuarioRepository.save(usuario);
            return true;
        }

        return false;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrarSenhasLegadas() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        int migradas = 0;

        for (Usuario usuario : usuarios) {
            String senha = usuario.getSenha();

            if (senha != null && !senha.isBlank() && !ehHashBCrypt(senha)) {
                usuario.setSenha(passwordEncoder.encode(senha));
                migradas++;
            }
        }

        if (migradas > 0) {
            usuarioRepository.saveAll(usuarios);
            System.out.println("[SEGURANÇA] Senhas legadas convertidas para BCrypt: " + migradas);
        } else {
            System.out.println("[SEGURANÇA] Verificação concluída: nenhuma senha legada para migrar.");
        }
    }

    private boolean ehHashBCrypt(String valor) {
        return valor != null
                && (valor.startsWith("$2a$")
                || valor.startsWith("$2b$")
                || valor.startsWith("$2y$"));
    }
}
