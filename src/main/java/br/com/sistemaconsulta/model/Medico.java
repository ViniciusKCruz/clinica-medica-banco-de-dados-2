package br.com.sistemaconsulta.model;

import br.com.sistemaconsulta.model.enums.StatusMedico;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * Especialização de {@link Pessoa} correspondente à tabela {@code medico}.
 * Especialidades, consultas e prontuário ficam para as próximas etapas.
 */
@Entity
@Table(name = "medico")
@PrimaryKeyJoinColumn(name = "id_medico")
public class Medico extends Pessoa {

    private static final long serialVersionUID = 1L;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_atual", nullable = false, length = 15)
    private StatusMedico statusAtual;

    @Column(name = "salario", precision = 10, scale = 2)
    private BigDecimal salario;

    public Medico() {
        super();
        this.statusAtual = StatusMedico.ATIVO;
    }

    public Medico(String nome, String cpf, Endereco endereco,
                  String telefone, StatusMedico statusAtual, BigDecimal salario) {
        super(nome, cpf, endereco);
        this.telefone = telefone;
        this.statusAtual = statusAtual != null ? statusAtual : StatusMedico.ATIVO;
        this.salario = salario;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public StatusMedico getStatusAtual() {
        return statusAtual;
    }

    public void setStatusAtual(StatusMedico statusAtual) {
        this.statusAtual = statusAtual;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }
}
