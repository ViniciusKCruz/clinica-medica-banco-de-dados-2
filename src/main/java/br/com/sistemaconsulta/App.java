package br.com.sistemaconsulta;

import br.com.sistemaconsulta.util.JpaUtil;
import br.com.sistemaconsulta.seed.DataSeeder;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        try {
            JpaUtil.getEntityManagerFactory();
            System.out.println("PostgreSQL conectado; tabelas criadas/atualizadas com sucesso.");
            boolean criada = new DataSeeder().popular();
            System.out.println(criada
                    ? "Carga criada: 5 clinicas, 100 especialidades, 200 pacientes e 2000 consultas, com dados de apoio."
                    : "O banco ja possui dados; carga nao executada.");
        } finally {
            JpaUtil.close();
        }
    }
}
