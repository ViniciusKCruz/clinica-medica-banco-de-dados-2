package br.com.sistemaconsulta.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "clinica")
public class Clinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clinica")
    private Long id;

    @Embedded
    @AttributeOverride(name = "uf", column = @Column(name = "uf", length = 45))
    private Endereco endereco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medico")
    private Medico medico;

    public Clinica() {
    }

    public Long getId() {
        return id;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente valor) {
        if (this.paciente == valor) {
            return;
        }
        if (this.paciente != null) {
            this.paciente.removerClinicas(this);
        }
        this.paciente = valor;
        if (valor != null) {
            valor.adicionarClinicas(this);
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
            this.medico.removerClinicas(this);
        }
        this.medico = valor;
        if (valor != null) {
            valor.adicionarClinicas(this);
        }
    }

}
