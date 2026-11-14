package br.com.sistemaconsulta.util;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

final class DatabaseConfig {
    private DatabaseConfig() {
    }

    static Map<String, Object> load() {
        final Dotenv dotenv;
        try {
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        } catch (DotenvException e) {
            throw new IllegalStateException("Nao foi possivel ler o .env.");
        }
        return properties(System::getenv, dotenv::get);
    }

    static Map<String, Object> properties(Function<String, String> environment,
                                           Function<String, String> localFile) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", required("DB_URL", environment, localFile));
        properties.put("javax.persistence.jdbc.user", required("DB_USER", environment, localFile));
        properties.put("javax.persistence.jdbc.password", required("DB_PASSWORD", environment, localFile));
        return properties;
    }

    private static String required(String key, Function<String, String> environment,
                                   Function<String, String> localFile) {
        String value = environment.apply(key);
        if (value == null) {
            value = localFile.apply(key);
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Configuracao obrigatoria ausente ou vazia: " + key
                    + ". Defina a variavel de ambiente ou preencha o .env na raiz do projeto.");
        }
        return value;
    }
}
