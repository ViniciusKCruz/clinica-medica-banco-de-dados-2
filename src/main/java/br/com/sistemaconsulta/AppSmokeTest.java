package br.com.sistemaconsulta;

import br.com.sistemaconsulta.model.Endereco;
import br.com.sistemaconsulta.model.Medico;
import br.com.sistemaconsulta.model.Paciente;
import br.com.sistemaconsulta.model.enums.Prioridade;
import br.com.sistemaconsulta.model.enums.StatusConsulta;
import br.com.sistemaconsulta.model.enums.StatusFila;
import br.com.sistemaconsulta.model.enums.StatusMedico;
import br.com.sistemaconsulta.model.enums.StatusPaciente;
import br.com.sistemaconsulta.util.JpaUtil;

import javax.persistence.EntityManager;
import java.math.BigDecimal;

/**
 * Smoke test da Etapa 1: sobe o EntityManager (H2), persiste Paciente e Medico
 * e confirma herança, embeddable e enums do domínio clínico.
 *
 * Eclipse: Run As → Java Application
 * Terminal: {@code mvn -q exec:java}
 */
public class AppSmokeTest {

    public static void main(String[] args) {
        EntityManager em = JpaUtil.getEntityManager("sistemaConsultaH2PU");
        try {
            em.getTransaction().begin();

            Paciente paciente = new Paciente(
                    "Ana Souza",
                    "11122233344",
                    new Endereco("Rua das Flores 100", "Centro", "Salvador", "BA"),
                    StatusPaciente.ATIVO);

            Medico medico = new Medico(
                    "Dr. Bruno Lima",
                    "55566677788",
                    new Endereco("Av. Sete 50", "Barra", "Salvador", "BA"),
                    "71988880000",
                    StatusMedico.ATIVO,
                    new BigDecimal("8500.00"));

            em.persist(paciente);
            em.persist(medico);
            em.getTransaction().commit();

            System.out.println("=== Etapa 1 OK (Consulta Médica) ===");
            System.out.println("Paciente: " + paciente + " | status=" + paciente.getStatusPaciente());
            System.out.println("Endereco embutido: " + paciente.getEndereco());
            System.out.println("Medico: " + medico + " | status=" + medico.getStatusAtual());
            System.out.println("Enums prontos: "
                    + StatusConsulta.AGENDADA + ", "
                    + StatusFila.AGUARDANDO + ", "
                    + Prioridade.NORMAL);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            System.err.println("Falha na Etapa 1. Verifique persistence.xml e dependências Maven.");
        } finally {
            em.close();
            JpaUtil.close();
        }
    }
}
