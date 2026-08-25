package universidade_atendimento;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FAQRepository extends JpaRepository<FAQ, Long> {

    List<FAQ> findByAtivaTrueOrderByCategoriaAsc();

    List<FAQ> findAllByOrderByCategoriaAsc();
}