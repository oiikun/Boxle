package br.senac.boxle.entities;

import jakarta.persistence.*;

import java.io.Serializable;

// Entidade Cliente, que é a empresa que aluga os boxes. Vira a tabela Clientes
@Entity(name = "Clientes")
public class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    private String cnpj;

    private String ie;

    // Endereço da empresa, preenchido na tela pela consulta do CEP
    private String cep;

    private String logradouro;

    private String numero;

    private String bairro;

    private String cidade;

    private String uf;

    // Pessoa de contato na empresa (só o nome, os documentos que valem são os da empresa)
    private String responsavel;

    // Dia do mês em que a empresa paga (1 a 28). Vale para os contratos dela
    private Integer diaVencimento;

    private String telefone;

    private String email;

    public Cliente() {
    }

    public Cliente(String nome, String cnpj, String ie, String responsavel, Integer diaVencimento,
                   String telefone, String email) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.ie = ie;
        this.responsavel = responsavel;
        this.diaVencimento = diaVencimento;
        this.telefone = telefone;
        this.email = email;
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

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getIe() {
        return ie;
    }

    public void setIe(String ie) {
        this.ie = ie;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public Integer getDiaVencimento() {
        return diaVencimento;
    }

    public void setDiaVencimento(Integer diaVencimento) {
        this.diaVencimento = diaVencimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
