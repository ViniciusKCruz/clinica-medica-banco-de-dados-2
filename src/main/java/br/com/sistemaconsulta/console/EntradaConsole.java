package br.com.sistemaconsulta.console;

import br.com.sistemaconsulta.model.enums.UnidadeFederativa;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

final class EntradaConsole {
    private final Scanner scanner;
    private final PrintStream saida;

    EntradaConsole(InputStream entrada, PrintStream saida) {
        this.scanner = new Scanner(entrada, "UTF-8");
        this.saida = saida;
    }

    String ler(String rotulo) {
        saida.print(rotulo + ": ");
        saida.flush();
        if (!scanner.hasNextLine()) {
            throw new FimDaEntrada();
        }
        return scanner.nextLine().trim();
    }

    int opcao(int maximo) {
        while (true) {
            try {
                int valor = Integer.parseInt(ler("Opcao"));
                if (valor >= 0 && valor <= maximo) {
                    return valor;
                }
            } catch (NumberFormatException ignored) { }
            saida.println("Opcao invalida. Informe um numero de 0 a " + maximo + ".");
        }
    }

    long id(String rotulo) {
        while (true) {
            try {
                long valor = Long.parseLong(ler(rotulo));
                if (valor > 0) {
                    return valor;
                }
            } catch (NumberFormatException ignored) { }
            saida.println("Informe um ID inteiro positivo.");
        }
    }

    Long idOpcional(String rotulo) {
        while (true) {
            String valor = ler(rotulo + " (Enter ignora)");
            if (valor.isEmpty()) {
                return null;
            }
            try {
                long id = Long.parseLong(valor);
                if (id > 0) {
                    return id;
                }
            } catch (NumberFormatException ignored) { }
            saida.println("Informe um ID inteiro positivo ou Enter para ignorar.");
        }
    }

    long quantidade() {
        while (true) {
            try {
                long valor = Long.parseLong(ler("Minimo de consultas por medico (0 inclui todos)"));
                if (valor >= 0) {
                    return valor;
                }
            } catch (NumberFormatException ignored) { }
            saida.println("Informe um numero inteiro nao negativo.");
        }
    }

    LocalDateTime dataHora(String rotulo, boolean opcional) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
                .withResolverStyle(ResolverStyle.STRICT);
        while (true) {
            String valor = ler(rotulo + " (dd/MM/aaaa HH:mm" + (opcional ? "; Enter ignora" : "") + ")");
            if (opcional && valor.isEmpty()) {
                return null;
            }
            try {
                return LocalDateTime.parse(valor, formato);
            } catch (DateTimeParseException ignored) { }
            saida.println("Informe uma data e hora validas, por exemplo 21/11/2026 14:30.");
        }
    }

    String texto(String rotulo, String atual, boolean obrigatorio, int limite) {
        while (true) {
            String valor = ler(rotulo + (atual == null ? "" : " [" + atual + "]"));
            if (valor.isEmpty()) {
                valor = atual;
            } else if (!obrigatorio && "-".equals(valor)) {
                valor = null;
            }
            if ((valor == null || valor.isEmpty()) && obrigatorio) {
                saida.println("Este campo e obrigatorio.");
            } else if (valor != null && valor.length() > limite) {
                saida.println("Informe no maximo " + limite + " caracteres.");
            } else {
                return valor;
            }
        }
    }

    String cpf(String atual) {
        while (true) {
            String valor = ler("CPF (11 digitos)" + (atual == null ? "" : " [" + atual + "]"));
            if (valor.isEmpty() && atual != null) {
                return atual;
            }
            valor = valor.replaceAll("[.\\-\\s]", "");
            if (valor.matches("[0-9]{11}")) {
                return valor;
            }
            saida.println("CPF deve conter exatamente 11 digitos.");
        }
    }

    String uf(String atual) {
        while (true) {
            String valor = texto("UF", atual, false, 2);
            if (valor == null) {
                return null;
            }
            try {
                return UnidadeFederativa.valueOf(valor.toUpperCase(Locale.ROOT)).name();
            } catch (IllegalArgumentException ignored) { }
            saida.println("Informe uma UF brasileira valida, por exemplo BA, SP ou RJ; - limpa o campo.");
        }
    }

    BigDecimal salario(BigDecimal atual) {
        while (true) {
            String valor = ler("Salario (ex.: 6500,00; - para limpar)"
                    + (atual == null ? "" : " [" + atual.toPlainString() + "]"));
            if (valor.isEmpty()) {
                return atual;
            }
            if ("-".equals(valor)) {
                return null;
            }
            if (valor.matches("[0-9]{1,8}([.,][0-9]{1,2})?")) {
                return new BigDecimal(valor.replace(',', '.'));
            }
            saida.println("Informe um valor de 0 a 99999999,99 com ate duas casas decimais, sem milhar.");
        }
    }

    <E extends Enum<E>> E enumeracao(String rotulo, E[] valores, E atual) {
        return enumeracao(rotulo, valores, atual, false);
    }

    <E extends Enum<E>> E enumeracaoOpcional(E[] valores) {
        return enumeracao("Status da consulta", valores, null, true);
    }

    private <E extends Enum<E>> E enumeracao(String rotulo, E[] valores, E atual, boolean opcional) {
        saida.println(rotulo + ":");
        for (int i = 0; i < valores.length; i++) {
            saida.println((i + 1) + " - " + valores[i]);
        }
        while (true) {
            String valor = ler("Escolha" + (opcional ? " (Enter ignora)" : "") + (atual == null ? "" : " [" + atual + "]"));
            if (valor.isEmpty() && opcional) {
                return null;
            }
            if (valor.isEmpty() && atual != null) {
                return atual;
            }
            try {
                int indice = Integer.parseInt(valor) - 1;
                if (indice >= 0 && indice < valores.length) {
                    return valores[indice];
                }
            } catch (NumberFormatException ignored) { }
            saida.println("Escolha um numero de 1 a " + valores.length + ".");
        }
    }

    static final class FimDaEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
