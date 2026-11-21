package br.com.sistemaconsulta.dao.dto;

import java.util.Collections;
import java.util.List;

public final class Pagina<T> {
    private final List<T> registros;
    private final long total;
    private final int tamanho;

    public Pagina(List<T> registros, long total, int tamanho) {
        this.registros = Collections.unmodifiableList(registros);
        this.total = total;
        this.tamanho = tamanho;
    }

    public List<T> getRegistros() {
        return registros;
    }

    public long getTotal() {
        return total;
    }

    public long getTotalPaginas() {
        return total == 0 ? 0 : (total - 1) / tamanho + 1;
    }
}
