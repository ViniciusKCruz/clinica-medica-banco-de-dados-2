package br.com.sistemaconsulta.console;

import br.com.sistemaconsulta.dao.GenericDAO;
import br.com.sistemaconsulta.dao.ConvenioDAO;
import br.com.sistemaconsulta.dao.ConsultaDAO;
import br.com.sistemaconsulta.dao.MedicoDAO;
import br.com.sistemaconsulta.dao.PacienteDAO;
import br.com.sistemaconsulta.dao.dto.ResumoSalariosPorStatus;
import br.com.sistemaconsulta.dao.dto.Pagina;
import br.com.sistemaconsulta.model.Consulta;
import br.com.sistemaconsulta.model.Prontuario;
import br.com.sistemaconsulta.model.Endereco;
import br.com.sistemaconsulta.model.Convenio;
import br.com.sistemaconsulta.model.Medico;
import br.com.sistemaconsulta.model.Paciente;
import br.com.sistemaconsulta.model.Pessoa;
import br.com.sistemaconsulta.model.enums.StatusMedico;
import br.com.sistemaconsulta.model.enums.StatusPaciente;
import br.com.sistemaconsulta.model.enums.StatusConsulta;

import javax.persistence.EntityManagerFactory;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.IntFunction;

public final class ConsoleApp {
    private static final int TAMANHO_PAGINA = 10;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private final EntradaConsole entrada;
    private final PrintStream saida;
    private final PacienteDAO pacientes;
    private final MedicoDAO medicos;
    private final ConvenioDAO convenios;
    private final ConsultaDAO consultas;

    public ConsoleApp(EntityManagerFactory factory, InputStream entrada, PrintStream saida) {
        this.entrada = new EntradaConsole(entrada, saida);
        this.saida = saida;
        this.pacientes = new PacienteDAO(factory);
        this.medicos = new MedicoDAO(factory);
        this.convenios = new ConvenioDAO(factory);
        this.consultas = new ConsultaDAO(factory);
    }

    public void executar() {
        try {
            while (true) {
                saida.println("\n=== SISTEMA DE CONSULTA MEDICA ===");
                saida.println("1 - Pacientes\n2 - Medicos\n3 - Consultas e relatorios\n0 - Sair");
                switch (entrada.opcao(3)) {
                    case 1: menuPacientes(); break;
                    case 2: menuMedicos(); break;
                    case 3: menuConsultas(); break;
                    case 0: return;
                    default: break;
                }
            }
        } catch (EntradaConsole.FimDaEntrada ignored) {
            saida.println("\nEntrada encerrada. Operacao em andamento cancelada.");
        } finally {
            saida.println("Sistema encerrado.");
        }
    }

    private void menuPacientes() {
        while (true) {
            menuCrud("PACIENTES");
            saida.println("6 - Buscar por nome\n7 - Buscar por convenio\n0 - Voltar");
            int opcao = entrada.opcao(7);
            if (opcao == 0) {
                return;
            }
            executarOperacao(() -> {
                if (opcao == 6) {
                    String trecho = entrada.texto("Parte do nome", null, true, 45);
                    listarPaginado(pagina -> pacientes.buscarPorNomeCriteria(trecho, pagina, TAMANHO_PAGINA), this::mostrar);
                } else if (opcao == 7) {
                    long convenioId = entrada.id("ID do convenio");
                    listarPaginado(pagina -> pacientes.buscarPorConvenio(convenioId, pagina, TAMANHO_PAGINA), this::mostrar);
                } else {
                    executarCrud(opcao, pacientes, Paciente::new, this::preencherPaciente);
                }
            });
        }
    }

    private void menuMedicos() {
        while (true) {
            menuCrud("MEDICOS");
            saida.println("6 - Resumo de salarios por status\n7 - Salarios acima da media\n0 - Voltar");
            int opcao = entrada.opcao(7);
            if (opcao == 0) {
                return;
            }
            executarOperacao(() -> {
                if (opcao == 6) {
                    List<ResumoSalariosPorStatus> resumo = medicos.resumirSalariosPorStatus();
                    if (resumo.isEmpty()) {
                        saida.println("Nenhum registro encontrado.");
                    }
                    for (ResumoSalariosPorStatus item : resumo) {
                        saida.printf("%s | Quantidade: %d | Total: %s | Media: %s%n",
                                item.getStatus(), item.getQuantidade(), exibir(item.getTotalSalarios()),
                                exibir(item.getMediaSalarios()));
                    }
                } else if (opcao == 7) {
                    listarPaginado(pagina -> medicos.buscarComSalarioAcimaDaMedia(pagina, TAMANHO_PAGINA), this::mostrar);
                } else {
                    executarCrud(opcao, medicos, Medico::new, this::preencherMedico);
                }
            });
        }
    }

    private void menuCrud(String titulo) {
        saida.println("\n=== " + titulo + " ===");
        saida.println("1 - Cadastrar\n2 - Listar\n3 - Buscar por ID\n4 - Atualizar\n5 - Excluir");
    }

    private void menuConsultas() {
        while (true) {
            saida.println("\n=== CONSULTAS E RELATORIOS ===");
            saida.println("1 - Listar consultas\n2 - Buscar por status (JPQL)\n3 - Buscar por periodo"
                    + "\n4 - Buscar por medico\n5 - Detalhar consulta por ID\n6 - Total de consultas por status"
                    + "\n7 - Total por medico com minimo de consultas\n8 - Buscar por status (Criteria)"
                    + "\n9 - Combinar filtros (Criteria)\n0 - Voltar");
            int opcao = entrada.opcao(9);
            if (opcao == 0) {
                return;
            }
            executarOperacao(() -> executarConsulta(opcao));
        }
    }

    private void executarConsulta(int opcao) {
        switch (opcao) {
            case 1:
                listarPaginado(consultas, this::mostrarConsulta);
                break;
            case 2: {
                StatusConsulta status = entrada.enumeracao("Status da consulta", StatusConsulta.values(), null);
                listarPaginado(pagina -> consultas.buscarPorStatus(status, pagina, TAMANHO_PAGINA), this::mostrarConsulta);
                break;
            }
            case 3: {
                saida.println("O periodo inclui o inicio e exclui o fim.");
                LocalDateTime inicio = entrada.dataHora("Inicio", false);
                LocalDateTime fim = entrada.dataHora("Fim", false);
                listarPaginado(pagina -> consultas.buscarPorPeriodo(inicio, fim, pagina, TAMANHO_PAGINA), this::mostrarConsulta);
                break;
            }
            case 4: {
                long medicoId = entrada.id("ID do medico");
                listarPaginado(pagina -> consultas.buscarPorMedico(medicoId, pagina, TAMANHO_PAGINA), this::mostrarConsulta);
                break;
            }
            case 5: {
                Consulta consulta = consultas.buscarDetalhadaPorId(entrada.id("ID da consulta"));
                if (consulta == null) {
                    saida.println("Registro nao encontrado.");
                } else {
                    mostrarConsulta(consulta);
                    Prontuario prontuario = consulta.getProntuario();
                    if (prontuario == null) {
                        saida.println("Sem prontuario.");
                    } else {
                        saida.printf("Prontuario: %d | Registro: %s%n", prontuario.getId(), formatarData(prontuario.getDataRegistro()));
                        saida.println("Queixa principal: " + exibir(prontuario.getQueixaPrincipal()));
                        saida.println("Diagnostico: " + exibir(prontuario.getDiagnostico()));
                        saida.println("Prescricao: " + exibir(prontuario.getPrescricaoMedica()));
                    }
                }
                break;
            }
            case 6:
                listarResumo(consultas.contarPorStatus(), item -> saida.printf("Status: %s | Consultas: %d%n",
                        item.getStatus(), item.getTotal()));
                break;
            case 7: {
                long minimo = entrada.quantidade();
                listarPaginado(pagina -> consultas.contarPorMedico(minimo, pagina, TAMANHO_PAGINA),
                        item -> saida.printf("ID: %d | Medico: %s | Consultas: %d%n",
                                item.getMedicoId(), item.getNomeMedico(), item.getTotal()));
                break;
            }
            case 8: {
                StatusConsulta status = entrada.enumeracao("Status da consulta", StatusConsulta.values(), null);
                listarPaginado(pagina -> consultas.buscarPorStatusCriteria(status, pagina, TAMANHO_PAGINA), this::mostrarConsulta);
                break;
            }
            case 9: {
                saida.println("Preencha os filtros desejados; Enter ignora cada filtro.");
                Long medicoId = entrada.idOpcional("ID do medico");
                Long pacienteId = entrada.idOpcional("ID do paciente");
                StatusConsulta status = entrada.enumeracaoOpcional(StatusConsulta.values());
                saida.println("O periodo inclui o inicio e exclui o fim.");
                LocalDateTime inicio = entrada.dataHora("Inicio", true);
                LocalDateTime fim = entrada.dataHora("Fim", true);
                listarPaginado(pagina -> consultas.buscarComFiltros(medicoId, pacienteId, status, inicio, fim,
                        pagina, TAMANHO_PAGINA), this::mostrarConsulta);
                break;
            }
            default: break;
        }
    }

    private <T extends Pessoa> void executarCrud(int opcao, GenericDAO<T, Long> dao,
                                                Supplier<T> novo, Consumer<T> preencher) {
        if (opcao == 1) {
            T registro = novo.get();
            preencher.accept(registro);
            if (registro instanceof Paciente) {
                pacientes.salvarComConvenio((Paciente) registro, escolherConvenio((Paciente) registro));
            } else {
                dao.salvar(registro);
            }
            saida.println("Cadastro realizado. ID: " + registro.getId());
        } else if (opcao == 2) {
            listarPaginado(dao, this::mostrar);
        } else {
            long id = entrada.id("ID");
            T registro = dao.buscarPorId(id);
            if (registro == null) {
                saida.println("Registro nao encontrado.");
                return;
            }
            mostrar(registro);
            if (opcao == 4) {
                saida.println("Enter mantem o valor atual. Use - para limpar campos opcionais.");
                preencher.accept(registro);
                if (registro instanceof Paciente) {
                    pacientes.atualizarComConvenio((Paciente) registro, escolherConvenio((Paciente) registro));
                } else {
                    dao.atualizar(registro);
                }
                saida.println("Registro atualizado com sucesso.");
            } else if (opcao == 5) {
                if ("s".equalsIgnoreCase(entrada.ler("Confirma a exclusao? (s/N)"))) {
                    saida.println(dao.remover(id) ? "Registro excluido com sucesso." : "Registro nao encontrado.");
                } else {
                    saida.println("Exclusao cancelada.");
                }
            }
        }
    }

    private void preencherPessoa(Pessoa pessoa) {
        pessoa.setNome(entrada.texto("Nome", pessoa.getNome(), true, 45));
        pessoa.setCpf(entrada.cpf(pessoa.getCpf()));
        saida.println("Endereco opcional: Enter mantem o valor; - limpa o campo.");
        Endereco endereco = pessoa.getEndereco() == null ? new Endereco() : pessoa.getEndereco();
        endereco.setEndereco(entrada.texto("Logradouro", endereco.getEndereco(), false, 45));
        endereco.setBairro(entrada.texto("Bairro", endereco.getBairro(), false, 45));
        endereco.setCidade(entrada.texto("Cidade", endereco.getCidade(), false, 45));
        endereco.setUf(entrada.uf(endereco.getUf()));
        pessoa.setEndereco(endereco);
    }

    private void preencherPaciente(Paciente paciente) {
        preencherPessoa(paciente);
        paciente.setStatusPaciente(entrada.enumeracao("Status do paciente", StatusPaciente.values(),
                paciente.getStatusPaciente()));
    }

    private void preencherMedico(Medico medico) {
        preencherPessoa(medico);
        medico.setTelefone(entrada.texto("Telefone (opcional)", medico.getTelefone(), false, 20));
        medico.setStatusAtual(entrada.enumeracao("Status do medico", StatusMedico.values(), medico.getStatusAtual()));
        medico.setSalario(entrada.salario(medico.getSalario()));
    }

    private Long escolherConvenio(Paciente paciente) {
        Convenio atual = paciente.getConvenio();
        saida.println("Convenio atual: " + (atual == null ? "Sem convenio" : atual.getId() + " - " + atual.getNomeConvenio()));
        while (true) {
            String valor = entrada.ler("ID do convenio (L lista; Enter mantem atual; 0 sem convenio)");
            if (valor.isEmpty()) {
                return atual == null ? null : atual.getId();
            }
            if ("0".equals(valor)) {
                return null;
            }
            if ("l".equalsIgnoreCase(valor)) {
                listarPaginado(convenios, convenio -> saida.printf("ID: %d | Convenio: %s%n",
                        convenio.getId(), exibir(convenio.getNomeConvenio())));
                continue;
            }
            try {
                long id = Long.parseLong(valor);
                if (id > 0 && convenios.buscarPorId(id) != null) {
                    return id;
                }
            } catch (NumberFormatException ignored) { }
            saida.println("Convenio nao encontrado. Informe um ID valido ou L para listar.");
        }
    }

    private <T> void listarPaginado(GenericDAO<T, Long> dao, Consumer<T> mostrarRegistro) {
        listarPaginado(pagina -> dao.consultarPagina(pagina, TAMANHO_PAGINA), mostrarRegistro);
    }

    private <T> void listarPaginado(IntFunction<Pagina<T>> buscarPagina, Consumer<T> mostrarRegistro) {
        int pagina = 0;
        while (true) {
            Pagina<T> resultado = buscarPagina.apply(pagina);
            long total = resultado.getTotal();
            if (total == 0) {
                saida.println("Nenhum registro encontrado.");
                return;
            }
            long totalPaginas = resultado.getTotalPaginas();
            if (pagina >= totalPaginas) {
                pagina = (int) (totalPaginas - 1);
                continue;
            }
            if (totalPaginas > 1) {
                saida.printf("%n=== Pagina %d de %d | Total: %d ===%n", pagina + 1, totalPaginas, total);
            }
            for (T registro : resultado.getRegistros()) {
                mostrarRegistro.accept(registro);
            }
            if (totalPaginas == 1) {
                saida.println("Total: " + total);
                return;
            }
            saida.println("1 - Proxima pagina\n2 - Pagina anterior\n0 - Voltar");
            int opcao = entrada.opcao(2);
            if (opcao == 0) {
                return;
            }
            if (opcao == 1) {
                if (pagina + 1L < totalPaginas) {
                    pagina++;
                } else {
                    saida.println("Voce ja esta na ultima pagina.");
                }
            } else if (pagina > 0) {
                pagina--;
            } else {
                saida.println("Voce ja esta na primeira pagina.");
            }
        }
    }

    private <T> void listarResumo(List<T> registros, Consumer<T> mostrarRegistro) {
        if (registros.isEmpty()) {
            saida.println("Nenhum registro encontrado.");
        } else {
            for (T registro : registros) {
                mostrarRegistro.accept(registro);
            }
            saida.println("Total: " + registros.size());
        }
    }

    private void mostrar(Pessoa pessoa) {
        saida.printf("ID: %d | Nome: %s | CPF: %s%n", pessoa.getId(), pessoa.getNome(), pessoa.getCpf());
        Endereco endereco = pessoa.getEndereco();
        if (endereco != null) {
            saida.printf("Endereco: %s | Bairro: %s | Cidade: %s | UF: %s%n", exibir(endereco.getEndereco()),
                    exibir(endereco.getBairro()), exibir(endereco.getCidade()), exibir(endereco.getUf()));
        }
        if (pessoa instanceof Paciente) {
            saida.println("Status: " + ((Paciente) pessoa).getStatusPaciente());
            Convenio convenio = ((Paciente) pessoa).getConvenio();
            saida.println("Convenio: " + (convenio == null ? "Sem convenio" : convenio.getId() + " - " + convenio.getNomeConvenio()));
        } else if (pessoa instanceof Medico) {
            Medico medico = (Medico) pessoa;
            saida.printf("Status: %s | Telefone: %s | Salario: %s%n", medico.getStatusAtual(),
                    exibir(medico.getTelefone()), exibir(medico.getSalario()));
        }
    }

    private static String exibir(Object valor) {
        return valor == null ? "Nao informado" : valor.toString();
    }

    private void mostrarConsulta(Consulta consulta) {
        saida.printf("ID: %d | Consulta: %s | Horario: %s | Status: %s%n", consulta.getId(),
                exibir(consulta.getNome()), formatarData(consulta.getHorario()), exibir(consulta.getStatusConsulta()));
        Medico medico = consulta.getMedico();
        Paciente paciente = consulta.getPaciente();
        saida.println("Medico: " + (medico == null ? "Nao informado" : medico.getId() + " - " + medico.getNome()));
        saida.println("Paciente: " + (paciente == null ? "Nao informado" : paciente.getId() + " - " + paciente.getNome()));
    }

    private static String formatarData(LocalDateTime valor) {
        return valor == null ? "Nao informado" : valor.format(FORMATO_DATA);
    }

    private void executarOperacao(Runnable operacao) {
        try {
            operacao.run();
        } catch (EntradaConsole.FimDaEntrada e) {
            throw e;
        } catch (RuntimeException e) {
            // Engole as excessoes genéricas do JDBC.
            for (Throwable causa = e; causa != null; causa = causa.getCause()) {
                if (causa instanceof SQLException) {
                    String estado = ((SQLException) causa).getSQLState();
                    if ("23505".equals(estado)) {
                        saida.println("Nao foi possivel salvar: CPF ja cadastrado.");
                    } else if ("23503".equals(estado)) {
                        saida.println("Nao foi possivel excluir: registro vinculado a outros dados, como consultas ou prontuarios.");
                    } else {
                        saida.println("Falha ao acessar o banco de dados. Verifique a conexao e tente novamente.");
                    }
                    return;
                }
            }
            if (e instanceof IllegalArgumentException) {
                saida.println("Dados invalidos: " + e.getMessage());
            } else {
                saida.println("Nao foi possivel concluir a operacao. Verifique os dados e a conexao com o banco.");
            }
        }
    }
}
