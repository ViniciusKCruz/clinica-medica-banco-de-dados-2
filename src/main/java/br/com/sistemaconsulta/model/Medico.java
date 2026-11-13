package br.com.sistemaconsulta.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.sistemaconsulta.model.enums.StatusMedico;

import java.math.BigDecimal;

/**
 * Especialização de {@link Pessoa} correspondente à tabela {@code medico}.
 */
@Entity
@Table(name = "medico")
@PrimaryKeyJoinColumn(name = "id_medico")
public class Medico extends Pessoa {

    @OneToMany(mappedBy = "medico")
    private List<MedicoEspecialidade> especialidades = new ArrayList<>();

    @OneToMany(mappedBy = "medico")
    private List<Consulta> consultas = new ArrayList<>();

    @OneToMany(mappedBy = "medico")
    private List<Prontuario> prontuarios = new ArrayList<>();

    @OneToMany(mappedBy = "medico")
    private List<FilaEsperaConsulta> filasEspera = new ArrayList<>();

    @OneToMany(mappedBy = "medico")
    private List<Clinica> clinicas = new ArrayList<>();

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
    public List<MedicoEspecialidade> getEspecialidades() {
        return Collections.unmodifiableList(especialidades);
    }

    void adicionarEspecialidades(MedicoEspecialidade valor) {
        if (!especialidades.contains(valor)) {
            especialidades.add(valor);
        }
    }

    void removerEspecialidades(MedicoEspecialidade valor) {
        especialidades.remove(valor);
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

}
