package br.com.sistemaconsulta.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Endereço embutido (sem tabela própria).
 * Campos alinhados ao DER: endereco, bairro, cidade, uf.
 */
@Embeddable
public class Endereco implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "endereco", length = 45)
    private String endereco;

    @Column(name = "bairro", length = 45)
    private String bairro;

    @Column(name = "cidade", length = 45)
    private String cidade;

    @Column(name = "uf", length = 2)
    private String uf;

    public Endereco() {
    }

    public Endereco(String endereco, String bairro, String cidade, String uf) {
        this.endereco = endereco;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Endereco)) {
            return false;
        }
        Endereco that = (Endereco) o;
        return Objects.equals(endereco, that.endereco)
                && Objects.equals(bairro, that.bairro)
                && Objects.equals(cidade, that.cidade)
                && Objects.equals(uf, that.uf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endereco, bairro, cidade, uf);
    }

    @Override
    public String toString() {
        return endereco + " - " + bairro + ", " + cidade + "/" + uf;
    }
}
