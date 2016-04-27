package com.kainos.learn.hibseq.db;

import com.kainos.learn.hibseq.domain.GenericGeneratorSeqPooledLo;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

public class GenericGeneratorSeqPooledLoDao extends AbstractDAO<GenericGeneratorSeqPooledLo> {

    public GenericGeneratorSeqPooledLoDao(SessionFactory factory) {
        super(factory);
    }

    public GenericGeneratorSeqPooledLo get(Long id) {
        return super.get(id);
    }

    public List<GenericGeneratorSeqPooledLo> list() {
        return list(criteria());
    }

    public void delete(GenericGeneratorSeqPooledLo record) {
        currentSession().delete(record);
        currentSession().flush();
        currentSession().clear();
    }

    @Override
    public GenericGeneratorSeqPooledLo persist(GenericGeneratorSeqPooledLo record) {
        return super.persist(record);
    }
}
