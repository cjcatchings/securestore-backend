package com.ccatchings.securestore.database.postgresql;

import com.ccatchings.securestore.database.hibernate.model.DBBackedModel;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class PostgresAdapter {

    private Logger logger = LoggerFactory.getLogger(PostgresAdapter.class);

    private SessionFactory sessionFactory;
    private Session session;

    public PostgresAdapter(){
        logger.info("Initiating new Postgres Adapter");
        try {
            Configuration config = new Configuration().configure();
            config.setProperty("hibernate.connection.url", System.getenv("PG_JDBC_URL"));
            config.setProperty("hibernate.connection.username", System.getenv("PG_JDBC_USER"));
            config.setProperty("hibernate.connection.password", System.getenv("PG_JDBC_PASSWORD"));
            sessionFactory = config.buildSessionFactory();
            session = sessionFactory.openSession();
        }catch(ServiceException svcEx){
            logger.error(String.format("Unable to connect to database:  %s", svcEx.getMessage()));
            sessionFactory = null;
            session = null;
        }
    }

    public void persistRecord(DBBackedModel modelItem){
        session.beginTransaction();
        session.persist(modelItem);
        session.getTransaction().commit();
    }

    public <T> T executeQueryWithSingleResult(CriteriaQuery<T> critQuery){
        return session.createQuery(critQuery).uniqueResult();
    }

    public CriteriaBuilder getCriteriaBuilder(){
        return session.getCriteriaBuilder();
    }

    public <T> List<T> executeQueryWithMultipleResults(CriteriaQuery<T> critQuery){
        return session.createQuery(critQuery).getResultList();
    }

    @PreDestroy
    public void tearDown(){
        logger.info("Destroying PostgresAdapter");
        if(sessionFactory != null){
            sessionFactory.close();
        }
    }

}
