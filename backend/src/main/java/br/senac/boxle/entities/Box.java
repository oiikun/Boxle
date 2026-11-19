package br.senac.boxle.entities;

import jakarta.persistence.*;

import java.io.Serializable;

// Entidade Box (o Model do MVC). O JPA transforma essa classe na tabela Boxes do banco
@Entity(name = "Boxes")
public class Box implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String codigo;

    private Double tamanho;

    private Double precoMensal;

    // LIVRE ou OCUPADO. Quem muda o status é o contrato, não o cadastro do box
    private String status;

    public Box() {
    }

    public Box(String codigo, Double tamanho, Double precoMensal, String status) {
        this.codigo = codigo;
        this.tamanho = tamanho;
        this.precoMensal = precoMensal;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Double getTamanho() {
        return tamanho;
    }

    public void setTamanho(Double tamanho) {
        this.tamanho = tamanho;
    }

    public Double getPrecoMensal() {
        return precoMensal;
    }

    public void setPrecoMensal(Double precoMensal) {
        this.precoMensal = precoMensal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
