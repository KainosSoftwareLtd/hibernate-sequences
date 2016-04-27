package com.kainos.learn.hibseq.db;

import com.kainos.learn.hibseq.domain.GenericGeneratorSeqPooled;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

public class GenericGeneratorSeqPooledDao extends AbstractDAO<GenericGeneratorSeqPooled> {

    public GenericGeneratorSeqPooledDao(SessionFactory factory) {
        super(factory);
    }

    public GenericGeneratorSeqPooled get(Long id) {
        return super.get(id);
    }

    public List<GenericGeneratorSeqPooled> list() {
        return list(criteria());
    }

    public void delete(GenericGeneratorSeqPooled record) {
        currentSession().delete(record);
        currentSession().flush();
        currentSession().clear();
    }

    @Override
    public GenericGeneratorSeqPooled persist(GenericGeneratorSeqPooled record) {
        return super.persist(record);
    }
}
