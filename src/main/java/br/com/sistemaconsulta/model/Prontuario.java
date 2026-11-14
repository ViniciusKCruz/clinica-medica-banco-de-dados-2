package br.com.sistemaconsulta.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "prontuario")
public class Prontuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prontuario")
    private Long id;

    @Column(name = "queixa_principal", length = 200)
    private String queixaPrincipal;

    @Column(length = 200)
    private String diagnostico;

    @Column(name = "prescricao_medica", length = 400)
    private String prescricaoMedica;

    @Column(name = "data_registro")
    private LocalDateTime dataRegistro;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_consulta", nullable = false, unique = true)
    private Consulta consulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medico")
    private Medico medico;

    public Prontuario() {
    }

    public Long getId() {
        return id;
    }

    public String getQueixaPrincipal() {
        return queixaPrincipal;
    }

    public void setQueixaPrincipal(String queixaPrincipal) {
        this.queixaPrincipal = queixaPrincipal;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getPrescricaoMedica() {
        return prescricaoMedica;
    }

    public void setPrescricaoMedica(String prescricaoMedica) {
        this.prescricaoMedica = prescricaoMedica;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta valor) {
        if (this.consulta == valor) {
            return;
        }
        if (valor != null && valor.getProntuario() != null && valor.getProntuario() != this) {
            throw new IllegalArgumentException("A consulta já possui um prontuário.");
        }
        if (this.consulta != null) {
            this.consulta.vincularProntuario(null);
        }
        this.consulta = valor;
        if (valor != null) {
            valor.vincularProntuario(this);
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
            this.paciente.removerProntuarios(this);
        }
        this.paciente = valor;
        if (valor != null) {
            valor.adicionarProntuarios(this);
        }
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico valor) {
        if (this.medico == valor) {
            return;
        }
        if (this.medico != null) {
            this.medico.removerProntuarios(this);
        }
        this.medico = valor;
        if (valor != null) {
            valor.adicionarProntuarios(this);
        }
    }

}
