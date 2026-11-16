package br.com.sistemaconsulta.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "medico_especialidade")
public class MedicoEspecialidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medico_especialidade")
    private Long id;

    @Column(length = 45)
    private String nome;

    @Column(length = 150)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medico")
    private Medico medico;

    public MedicoEspecialidade() {
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico valor) {
        if (this.medico == valor) {
            return;
        }
        if (this.medico != null) {
            this.medico.removerEspecialidades(this);
        }
        this.medico = valor;
        if (valor != null) {
            valor.adicionarEspecialidades(this);
        }
    }

}
