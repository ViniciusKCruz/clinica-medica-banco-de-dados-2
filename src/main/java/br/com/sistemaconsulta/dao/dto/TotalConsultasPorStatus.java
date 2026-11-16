package br.com.sistemaconsulta.dao.dto;

import br.com.sistemaconsulta.model.enums.StatusConsulta;

public final class TotalConsultasPorStatus {
    private final StatusConsulta status;
    private final Long total;

    public TotalConsultasPorStatus(StatusConsulta status, Long total) {
        this.status = status;
        this.total = total;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public Long getTotal() {
        return total;
    }
}
