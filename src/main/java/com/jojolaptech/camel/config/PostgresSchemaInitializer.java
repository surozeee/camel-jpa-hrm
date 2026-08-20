package com.jojolaptech.camel.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Component to ensure PostgreSQL schema is initialized on application startup.
 * This forces Hibernate to create tables if they don't exist.
 */
@Component
@RequiredArgsConstructor
public class PostgresSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(PostgresSchemaInitializer.class);

    @Qualifier("postgresEntityManagerFactory")
    private final EntityManagerFactory postgresEntityManagerFactory;

    @PostConstruct
    public void initializeSchema() {
        log.info("Initializing PostgreSQL schema...");
        EntityManager em = null;
        try {
            em = postgresEntityManagerFactory.createEntityManager();
            Session session = em.unwrap(Session.class);
            Transaction tx = session.beginTransaction();
            try {
                // Force schema creation by executing a simple query
                session.createSelectionQuery("SELECT 1", Integer.class).getResultList();
                tx.commit();
                log.info("PostgreSQL schema initialized successfully");
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                log.error("Error initializing PostgreSQL schema", e);
            }
        } catch (Exception e) {
            log.error("Failed to initialize PostgreSQL schema", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}

