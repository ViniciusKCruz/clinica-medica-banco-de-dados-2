package br.com.sistemaconsulta.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.sistemaconsulta.model.enums.StatusPaciente;

/**
 * Especialização de {@link Pessoa} correspondente à tabela {@code paciente}.
 */
@Entity
@Table(name = "paciente")
@PrimaryKeyJoinColumn(name = "id_paciente")
@NamedQuery(name = "Paciente.porConvenio",
        query = "SELECT p FROM Paciente p JOIN p.convenio c WHERE c.id = :convenioId ORDER BY p.nome, p.id")
public class Paciente extends Pessoa {

    @OneToMany(mappedBy = "paciente")
    private List<Consulta> consultas = new ArrayList<>();

    @OneToMany(mappedBy = "paciente")
    private List<Prontuario> prontuarios = new ArrayList<>();

    @OneToMany(mappedBy = "paciente")
    private List<FilaEsperaConsulta> filasEspera = new ArrayList<>();

    @OneToMany(mappedBy = "paciente")
    private List<Clinica> clinicas = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_convenio")
    private Convenio convenio;

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

    @Override
    protected void validarDadosEspecificos() {
        if (statusPaciente == null) {
            throw new IllegalArgumentException("Status do paciente é obrigatório.");
        }
    }

    public void setStatusPaciente(StatusPaciente statusPaciente) {
        this.statusPaciente = statusPaciente;
    }

    public List<Consulta> getConsultas() {
        return Collections.unmodifiableList(consultas);
    }

    void adicionarConsultas(Consulta valor) {
        if (!consultas.contains(valor)) {
            consultas.add(valor);
        }
    }

    void removerConsultas(Consulta valor) {
        consultas.remove(valor);
    }

    public List<Prontuario> getProntuarios() {
        return Collections.unmodifiableList(prontuarios);
    }

    void adicionarProntuarios(Prontuario valor) {
        if (!prontuarios.contains(valor)) {
            prontuarios.add(valor);
        }
    }

    void removerProntuarios(Prontuario valor) {
        prontuarios.remove(valor);
    }

    public List<FilaEsperaConsulta> getFilasEspera() {
        return Collections.unmodifiableList(filasEspera);
    }

    void adicionarFilasEspera(FilaEsperaConsulta valor) {
        if (!filasEspera.contains(valor)) {
            filasEspera.add(valor);
        }
    }

    void removerFilasEspera(FilaEsperaConsulta valor) {
        filasEspera.remove(valor);
    }

    public List<Clinica> getClinicas() {
        return Collections.unmodifiableList(clinicas);
    }

    void adicionarClinicas(Clinica valor) {
        if (!clinicas.contains(valor)) {
            clinicas.add(valor);
        }
    }

    void removerClinicas(Clinica valor) {
        clinicas.remove(valor);
    }

    public Convenio getConvenio() {
        return convenio;
    }

    public void setConvenio(Convenio valor) {
        if (this.convenio == valor) {
            return;
        }
        if (this.convenio != null) {
            this.convenio.removerPacientes(this);
        }
        this.convenio = valor;
        if (valor != null) {
            valor.adicionarPacientes(this);
        }
    }

}
