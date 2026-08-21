package universidade_atendimento;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    List<Atendimento> findByUsuario(Usuario usuario);
}