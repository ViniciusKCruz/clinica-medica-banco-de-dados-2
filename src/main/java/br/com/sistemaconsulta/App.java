package br.com.sistemaconsulta;

import br.com.sistemaconsulta.console.ConsoleApp;
import br.com.sistemaconsulta.util.JpaUtil;
import br.com.sistemaconsulta.seed.DataSeeder;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        try {
            JpaUtil.getEntityManagerFactory();
            boolean criada = new DataSeeder().popular();
            System.out.println(criada
                    ? "Seeder executado: 5 clinicas, 100 especialidades, 200 pacientes e 2000 consultas, com dados de apoio."
                    : "O banco ja possui dados; Seeder nao executado.");
            new ConsoleApp(JpaUtil.getEntityManagerFactory(), System.in, System.out).executar();
        } finally {
            JpaUtil.close();
        }
    }
}
