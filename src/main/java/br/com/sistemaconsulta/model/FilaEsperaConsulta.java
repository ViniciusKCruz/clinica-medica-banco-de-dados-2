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
import javax.persistence.Table;
import br.com.sistemaconsulta.model.enums.StatusFila;
import br.com.sistemaconsulta.model.enums.Prioridade;
import java.time.LocalDateTime;

@Entity
@Table(name = "fila_espera_consulta")
public class FilaEsperaConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fila_espera_consulta")
    private Long id;

    @Column(name = "data_solicitacao")
    private LocalDateTime dataSolicitacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_fila", length = 20)
    private StatusFila statusFila;

    @Enumerated(EnumType.STRING)
    @Column(length = 45)
    private Prioridade prioridade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medico")
    private Medico medico;

    public FilaEsperaConsulta() {
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public StatusFila getStatusFila() {
        return statusFila;
    }

    public void setStatusFila(StatusFila statusFila) {
        this.statusFila = statusFila;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente valor) {
        if (this.paciente == valor) {
            return;
        }
        if (this.paciente != null) {
            this.paciente.removerFilasEspera(this);
        }
        this.paciente = valor;
        if (valor != null) {
            valor.adicionarFilasEspera(this);
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
            this.medico.removerFilasEspera(this);
        }
        this.medico = valor;
        if (valor != null) {
            valor.adicionarFilasEspera(this);
        }
    }

}
