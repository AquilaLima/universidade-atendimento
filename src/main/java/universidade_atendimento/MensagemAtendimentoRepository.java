package universidade_atendimento;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensagemAtendimentoRepository
        extends JpaRepository<MensagemAtendimento, Long> {

    List<MensagemAtendimento>
            findByAtendimentoOrderByDataHoraAsc(
                    Atendimento atendimento
            );
}