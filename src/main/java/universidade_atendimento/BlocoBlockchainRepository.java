package universidade_atendimento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlocoBlockchainRepository
        extends JpaRepository<BlocoBlockchain, Long> {

    Optional<BlocoBlockchain> findTopByOrderByIndiceDesc();
}