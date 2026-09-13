package universidade_atendimento;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Pode ficar vazio inicialmente para alunos pré-cadastrados
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    private String telefone;

    private String senha;

    @Column(nullable = false)
    private String perfil;

    @Column(nullable = false)
    private Boolean cadastroFinalizado = false;

    @Column(nullable = false)
    private Boolean ativo = true;

    // Código temporário de acesso da Universidade
    @Column(name = "codigo_acesso")
    private String codigoAcesso;

    // Data e hora de expiração do código temporário
    @Column(name = "codigo_acesso_expira_em")
    private LocalDateTime codigoAcessoExpiraEm;

    public Usuario() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public Boolean getCadastroFinalizado() {
        return cadastroFinalizado;
    }

    public void setCadastroFinalizado(Boolean cadastroFinalizado) {
        this.cadastroFinalizado = cadastroFinalizado;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getCodigoAcesso() {
        return codigoAcesso;
    }

    public void setCodigoAcesso(String codigoAcesso) {
        this.codigoAcesso = codigoAcesso;
    }

    public LocalDateTime getCodigoAcessoExpiraEm() {
        return codigoAcessoExpiraEm;
    }

    public void setCodigoAcessoExpiraEm(LocalDateTime codigoAcessoExpiraEm) {
        this.codigoAcessoExpiraEm = codigoAcessoExpiraEm;
    }
}