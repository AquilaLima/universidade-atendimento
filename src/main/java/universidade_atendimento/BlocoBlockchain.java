package universidade_atendimento;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_blocos")
public class BlocoBlockchain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long indice;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private String tipoEvento;

    private Long atendimentoId;

    private Long usuarioId;

    @Column(length = 5000)
    private String dados;

    @Column(nullable = false, length = 64)
    private String hashAnterior;

    @Column(nullable = false, unique = true, length = 64)
    private String hashAtual;

    public BlocoBlockchain() {
    }

    public Long getId() {
        return id;
    }

    public Long getIndice() {
        return indice;
    }

    public void setIndice(Long indice) {
        this.indice = indice;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public Long getAtendimentoId() {
        return atendimentoId;
    }

    public void setAtendimentoId(Long atendimentoId) {
        this.atendimentoId = atendimentoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getDados() {
        return dados;
    }

    public void setDados(String dados) {
        this.dados = dados;
    }

    public String getHashAnterior() {
        return hashAnterior;
    }

    public void setHashAnterior(String hashAnterior) {
        this.hashAnterior = hashAnterior;
    }

    public String getHashAtual() {
        return hashAtual;
    }

    public void setHashAtual(String hashAtual) {
        this.hashAtual = hashAtual;
    }
}