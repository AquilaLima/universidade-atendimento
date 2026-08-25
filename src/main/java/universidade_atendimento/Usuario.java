package universidade_atendimento;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Inicialmente pode ficar vazio para alunos pré-cadastrados
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    // Será preenchida quando o aluno finalizar o cadastro
    private String telefone;

    // Será preenchida quando o aluno finalizar o cadastro
    private String senha;

    @Column(nullable = false)
    private String perfil;

    @Column(nullable = false)
    private Boolean cadastroFinalizado = false;

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
}