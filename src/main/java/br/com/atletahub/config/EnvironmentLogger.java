package br.com.atletahub.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component // Marca a classe como um componente Spring para ser detectada automaticamente
public class EnvironmentLogger {

    private final Environment env;

    @Autowired // Injeta o Environment
    public EnvironmentLogger(Environment env) {
        this.env = env;
    }

    @PostConstruct // Este método será executado logo após a construção do bean
    public void logEnvironmentVariables() {
        System.out.println("--- ENVIRONMENT VARIABLES CHECK ---");
        System.out.println("DB_URL: " + env.getProperty("DB_URL"));
        System.out.println("DB_USERNAME: " + env.getProperty("DB_USERNAME"));
        System.out.println("DB_PASSWORD: " + env.getProperty("DB_PASSWORD"));
        System.out.println("jwt.secret-key: " + env.getProperty("jwt.secret-key"));
        System.out.println("jwt.expiration-time: " + env.getProperty("jwt.expiration-time"));
        System.out.println("--- END OF ENVIRONMENT VARIABLES CHECK ---");
    }
}
