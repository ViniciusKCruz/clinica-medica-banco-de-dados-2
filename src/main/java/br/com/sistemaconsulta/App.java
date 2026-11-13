package br.com.sistemaconsulta;

import br.com.sistemaconsulta.util.JpaUtil;

/** Verifica a conexao e cria/atualiza as tabelas antes de encerrar. */
public final class App {
    private App() {
    }

    public static void main(String[] args) {
        try {
            JpaUtil.getEntityManagerFactory();
            System.out.println("PostgreSQL conectado; tabelas criadas/atualizadas com sucesso.");
        } finally {
            JpaUtil.close();
        }
    }
}
