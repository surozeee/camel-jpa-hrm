package com.jojolaptech.camel;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class CamelJpaTnApplication {

    public static void main(String[] args) {
        // Load .env file if it exists
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            // Set system properties from .env file
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
        } catch (Exception e) {
            // .env file not found or error loading it - use default values from application.yml
            System.out.println("Note: .env file not found, using default configuration from application.yml");
        }

        // When running step-by-step, disable the master timer so only MigrationStepRunner executes.
        String mode = firstNonBlank(
                System.getenv("MIGRATION_MODE"),
                System.getProperty("MIGRATION_MODE"),
                System.getProperty("migration.mode"),
                "chain");
        if ("from".equalsIgnoreCase(mode) || "single".equalsIgnoreCase(mode)) {
            System.setProperty("MIGRATION_CHAIN_ENABLED", "false");
            System.setProperty("migration.chain-enabled", "false");
            System.out.println("Migration mode=" + mode + " → master chain disabled; step runner active");
        }

        SpringApplication.run(CamelJpaTnApplication.class, args);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }
}

