package br.com.sistemaconsulta.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "convenio")
public class Convenio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_convenio")
    private Long id;

    @Column(name = "nome_convenio", length = 45)
    private String nomeConvenio;

    @Column(length = 14)
    private String cnpj;

    @Column(name = "telefone_contato", length = 20)
    private String telefoneContato;

    @OneToMany(mappedBy = "convenio")
    private List<Paciente> pacientes = new ArrayList<>();

    public Convenio() {
    }

    public Long getId() {
        return id;
    }

    public String getNomeConvenio() {
        return nomeConvenio;
    }

    public void setNomeConvenio(String nomeConvenio) {
        this.nomeConvenio = nomeConvenio;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getTelefoneContato() {
        return telefoneContato;
    }

    public void setTelefoneContato(String telefoneContato) {
        this.telefoneContato = telefoneContato;
    }

    public List<Paciente> getPacientes() {
        return Collections.unmodifiableList(pacientes);
    }

    void adicionarPacientes(Paciente valor) {
        if (!pacientes.contains(valor)) {
            pacientes.add(valor);
        }
    }

    void removerPacientes(Paciente valor) {
        pacientes.remove(valor);
    }

}
