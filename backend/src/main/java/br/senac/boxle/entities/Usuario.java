package br.senac.boxle.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.io.Serializable;

// Entidade Usuario, quem entra no sistema (administrador ou atendente)
@Entity(name = "Usuarios")
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    // A senha só é recebida pela API, nunca é devolvida nas respostas
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    private String perfil;

    // CPF do funcionário, para saber quem é a pessoa por trás do login. Guardado só com os números
    @Column(unique = true)
    private String cpf;

    // Chave da sessão, devolvida só no login. Não vira coluna no banco
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String token;

    public Usuario() {
    }

    public Usuario(String nome, String email, String senha, String perfil, String cpf) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.cpf = cpf;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
