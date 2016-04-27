package com.kainos.learn.hibseq.db;

import com.kainos.learn.hibseq.domain.GenericGeneratorSequence;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

public class GenericGeneratorSequenceDao extends AbstractDAO<GenericGeneratorSequence> {

    public GenericGeneratorSequenceDao(SessionFactory factory) {
        super(factory);
    }

    public GenericGeneratorSequence get(Long id) {
        return super.get(id);
    }

    public List<GenericGeneratorSequence> list() {
        return list(criteria());
    }

    public void delete(GenericGeneratorSequence record) {
        currentSession().delete(record);
        currentSession().flush();
        currentSession().clear();
    }

    @Override
    public GenericGeneratorSequence persist(GenericGeneratorSequence record) {
        return super.persist(record);
    }
}
