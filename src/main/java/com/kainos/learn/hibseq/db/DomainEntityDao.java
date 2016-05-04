package com.kainos.learn.hibseq.db;

import com.kainos.learn.hibseq.domain.DomainEntity;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

public class DomainEntityDao<T extends DomainEntity> extends AbstractDAO<T> {
    public DomainEntityDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public void flushClearSession() {
        currentSession().flush();
        currentSession().clear();
    }
}
