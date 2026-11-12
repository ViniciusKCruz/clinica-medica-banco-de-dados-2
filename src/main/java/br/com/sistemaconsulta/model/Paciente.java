package br.com.sistemaconsulta.model;

import br.com.sistemaconsulta.model.enums.StatusPaciente;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Especialização de {@link Pessoa} correspondente à tabela {@code paciente}.
 * Relacionamento com Convenio fica para a Pessoa 2 / próximas etapas.
 */
@Entity
@Table(name = "paciente")
@PrimaryKeyJoinColumn(name = "id_paciente")
public class Paciente extends Pessoa {

    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_paciente", nullable = false, length = 45)
    private StatusPaciente statusPaciente;

    public Paciente() {
        super();
        this.statusPaciente = StatusPaciente.ATIVO;
    }

    public Paciente(String nome, String cpf, Endereco endereco, StatusPaciente statusPaciente) {
        super(nome, cpf, endereco);
        this.statusPaciente = statusPaciente != null ? statusPaciente : StatusPaciente.ATIVO;
    }

    public StatusPaciente getStatusPaciente() {
        return statusPaciente;
    }

    public void setStatusPaciente(StatusPaciente statusPaciente) {
        this.statusPaciente = statusPaciente;
    }
}
