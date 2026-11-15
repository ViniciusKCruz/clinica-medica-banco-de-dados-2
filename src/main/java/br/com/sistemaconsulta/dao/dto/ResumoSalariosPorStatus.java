package br.com.sistemaconsulta.dao.dto;

import br.com.sistemaconsulta.model.enums.StatusMedico;
import java.math.BigDecimal;

public final class ResumoSalariosPorStatus {
    private final StatusMedico status;
    private final Long quantidade;
    private final BigDecimal totalSalarios;
    private final Double mediaSalarios;

    public ResumoSalariosPorStatus(StatusMedico status, Long quantidade,
                                  BigDecimal totalSalarios, Double mediaSalarios) {
        this.status = status;
        this.quantidade = quantidade;
        this.totalSalarios = totalSalarios;
        this.mediaSalarios = mediaSalarios;
    }

    public StatusMedico getStatus() {
        return status;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public BigDecimal getTotalSalarios() {
        return totalSalarios;
    }

    public Double getMediaSalarios() {
        return mediaSalarios;
    }
}
