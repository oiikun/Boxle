package br.senac.boxle.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

// Entidade Contrato. Liga um box a uma empresa, com a data de início, o dia de vencimento e o valor por mês.
// A 1ª parcela vence na data de início (paga na assinatura). As outras vencem todo mês no dia de vencimento
@Entity(name = "Contratos")
public class Contrato implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Chaves estrangeiras para o box e para a empresa do contrato
    @ManyToOne
    @JoinColumn(name = "box_id")
    private Box box;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private LocalDate dataInicio;

    private Double valorMensal;

    // Dia do mês em que a parcela vence (1 a 28, para existir em todos os meses)
    private Integer diaVencimento;

    // Quantas parcelas já foram pagas, contando a da assinatura
    private Integer parcelasPagas;

    // ATIVO ou ENCERRADO. O contrato encerrado fica guardado como histórico
    private String status;

    private LocalDate dataEncerramento;

    // Último dia para a empresa retirar as coisas do box depois de encerrar. No dia seguinte o box fica livre
    private LocalDate prazoDesocupacao;

    public Contrato() {
    }

    public Contrato(Box box, Cliente cliente, LocalDate dataInicio) {
        this.box = box;
        this.cliente = cliente;
        this.dataInicio = dataInicio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Box getBox() {
        return box;
    }

    public void setBox(Box box) {
        this.box = box;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Double getValorMensal() {
        return valorMensal;
    }

    public void setValorMensal(Double valorMensal) {
        this.valorMensal = valorMensal;
    }

    public Integer getDiaVencimento() {
        return diaVencimento;
    }

    public void setDiaVencimento(Integer diaVencimento) {
        this.diaVencimento = diaVencimento;
    }

    public Integer getParcelasPagas() {
        return parcelasPagas;
    }

    public void setParcelasPagas(Integer parcelasPagas) {
        this.parcelasPagas = parcelasPagas;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDataEncerramento() {
        return dataEncerramento;
    }

    public void setDataEncerramento(LocalDate dataEncerramento) {
        this.dataEncerramento = dataEncerramento;
    }

    public LocalDate getPrazoDesocupacao() {
        return prazoDesocupacao;
    }

    public void setPrazoDesocupacao(LocalDate prazoDesocupacao) {
        this.prazoDesocupacao = prazoDesocupacao;
    }

    // Encerrado, mas a empresa ainda está dentro do prazo para tirar as coisas do box
    public boolean isDesocupando() {
        return "ENCERRADO".equals(status) && prazoDesocupacao != null
                && !prazoDesocupacao.isBefore(LocalDate.now());
    }

    // ----- Cálculo das parcelas. Não viram coluna no banco, mas vão no JSON para a tela -----

    // Data em que vence a parcela de número n (a 1ª é a própria data de início)
    public LocalDate vencimentoDaParcela(int n) {
        if (n <= 1) {
            return dataInicio;
        }
        LocalDate segunda = dataInicio.withDayOfMonth(diaVencimento);
        if (!segunda.isAfter(dataInicio)) {
            segunda = segunda.plusMonths(1);
        }
        return segunda.plusMonths(n - 2);
    }

    // Quantas parcelas já venceram até a data (vencer no próprio dia conta)
    public int parcelasVencidasAte(LocalDate data) {
        if (dataInicio == null || diaVencimento == null || data.isBefore(dataInicio)) {
            return 0;
        }
        int n = 1;
        while (!vencimentoDaParcela(n + 1).isAfter(data)) {
            n++;
        }
        return n;
    }

    // Vencimento da próxima parcela ainda não paga
    public LocalDate getProximoVencimento() {
        if (dataInicio == null || diaVencimento == null || parcelasPagas == null || !"ATIVO".equals(status)) {
            return null;
        }
        return vencimentoDaParcela(parcelasPagas + 1);
    }

    // Parcelas que já venceram e ainda não foram pagas
    public int getParcelasEmAtraso() {
        if (parcelasPagas == null || !"ATIVO".equals(status)) {
            return 0;
        }
        return Math.max(0, parcelasVencidasAte(LocalDate.now()) - parcelasPagas);
    }

    public double getValorEmAtraso() {
        if (valorMensal == null) {
            return 0;
        }
        return Math.round(getParcelasEmAtraso() * valorMensal * 100) / 100.0;
    }
}
