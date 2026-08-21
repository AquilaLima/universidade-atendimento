package universidade_atendimento;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "atendimentos")
public class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(unique = true, nullable = false)
    private String protocolo;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String email;

    private String telefone;

    @Column(nullable = false)
    private String assunto;

    @Column(length = 2000)
    private String descricao;

    private String categoria;

    private String prioridade;

    private String status;

    private LocalDateTime dataAbertura;

    private LocalDateTime dataAtualizacao;

    public Atendimento() {
    }
    
    @Column(length = 5000)
private String resposta;

private LocalDateTime dataResposta;

public String getResposta() {
    return resposta;
}

public void setResposta(String resposta) {
    this.resposta = resposta;
}

public LocalDateTime getDataResposta() {
    return dataResposta;
}

public void setDataResposta(LocalDateTime dataResposta) {
    this.dataResposta = dataResposta;
}

    @PrePersist
    protected void aoCriar() {

        if (status == null || status.isBlank()) {
            status = "ABERTO";
        }

        if (prioridade == null || prioridade.isBlank()) {
            prioridade = "NORMAL";
        }

        dataAbertura = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void aoAtualizar() {
        dataAtualizacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
}