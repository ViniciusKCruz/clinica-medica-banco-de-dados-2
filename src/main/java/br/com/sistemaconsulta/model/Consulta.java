package br.com.sistemaconsulta.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import br.com.sistemaconsulta.model.enums.StatusConsulta;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "consulta")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Long id;

    @Column(length = 45)
    private String nome;

    @Column(name = "horario")
    private LocalDateTime horario;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_consulta", length = 45)
    private StatusConsulta statusConsulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medico")
    private Medico medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente")
    private Paciente paciente;

    @OneToMany(mappedBy = "consulta")
    private List<Prontuario> prontuarios = new ArrayList<>();

    public Consulta() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDateTime getHorario() {
        return horario;
    }

    public void setHorario(LocalDateTime horario) {
        this.horario = horario;
    }

    public StatusConsulta getStatusConsulta() {
        return statusConsulta;
    }

    public void setStatusConsulta(StatusConsulta statusConsulta) {
        this.statusConsulta = statusConsulta;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico valor) {
        if (this.medico == valor) {
            return;
        }
        if (this.medico != null) {
            this.medico.removerConsultas(this);
        }
        this.medico = valor;
        if (valor != null) {
            valor.adicionarConsultas(this);
        }
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente valor) {
        if (this.paciente == valor) {
            return;
        }
        if (this.paciente != null) {
            this.paciente.removerConsultas(this);
        }
        this.paciente = valor;
        if (valor != null) {
            valor.adicionarConsultas(this);
        }
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

}
