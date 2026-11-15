package br.com.sistemaconsulta.dao.dto;

public final class TotalConsultasPorMedico {
    private final Long medicoId;
    private final String nomeMedico;
    private final Long total;

    public TotalConsultasPorMedico(Long medicoId, String nomeMedico, Long total) {
        this.medicoId = medicoId;
        this.nomeMedico = nomeMedico;
        this.total = total;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public String getNomeMedico() {
        return nomeMedico;
    }

    public Long getTotal() {
        return total;
    }
}
