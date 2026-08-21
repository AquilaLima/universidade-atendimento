package universidade_atendimento;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
public class BlockchainService {

    private final BlocoBlockchainRepository repository;

    public BlockchainService(
            BlocoBlockchainRepository repository) {

        this.repository = repository;
    }

    public BlocoBlockchain registrarEvento(
            String tipoEvento,
            Long atendimentoId,
            Long usuarioId,
            String dados) {

        BlocoBlockchain ultimoBloco =
                repository
                        .findTopByOrderByIndiceDesc()
                        .orElse(null);

        long indice;

        String hashAnterior;

        if (ultimoBloco == null) {

            indice = 1;

            hashAnterior = "0";

        } else {

            indice = ultimoBloco.getIndice() + 1;

            hashAnterior = ultimoBloco.getHashAtual();
        }

        LocalDateTime dataHora =
                LocalDateTime.now();

        String conteudo =
                indice
                + "|"
                + dataHora
                + "|"
                + tipoEvento
                + "|"
                + atendimentoId
                + "|"
                + usuarioId
                + "|"
                + dados
                + "|"
                + hashAnterior;

        String hashAtual =
                gerarHash(conteudo);

        BlocoBlockchain bloco =
                new BlocoBlockchain();

        bloco.setIndice(indice);

        bloco.setDataHora(dataHora);

        bloco.setTipoEvento(tipoEvento);

        bloco.setAtendimentoId(atendimentoId);

        bloco.setUsuarioId(usuarioId);

        bloco.setDados(dados);

        bloco.setHashAnterior(hashAnterior);

        bloco.setHashAtual(hashAtual);

        return repository.save(bloco);
    }

    private String gerarHash(String texto) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            texto.getBytes(
                                    StandardCharsets.UTF_8));

            StringBuilder hexadecimal =
                    new StringBuilder();

            for (byte b : hash) {

                String hex =
                        Integer.toHexString(
                                0xff & b);

                if (hex.length() == 1) {
                    hexadecimal.append('0');
                }

                hexadecimal.append(hex);
            }

            return hexadecimal.toString();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erro ao gerar hash SHA-256",
                    e);
        }
    }
}