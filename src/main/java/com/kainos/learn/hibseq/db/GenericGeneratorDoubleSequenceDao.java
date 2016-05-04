package com.kainos.learn.hibseq.db;

import com.kainos.learn.hibseq.domain.GenericGeneratorDoubleSequence;
import org.hibernate.SessionFactory;

import java.util.List;

public class GenericGeneratorDoubleSequenceDao extends DomainEntityDao<GenericGeneratorDoubleSequence> {

    public GenericGeneratorDoubleSequenceDao(SessionFactory factory) {
        super(factory);
    }

    public GenericGeneratorDoubleSequence get(Long id) {
        return super.get(id);
    }

    public List<GenericGeneratorDoubleSequence> list() {
        return list(criteria());
    }

    public void delete(GenericGeneratorDoubleSequence record) {
        currentSession().delete(record);
        currentSession().flush();
        currentSession().clear();
    }

    @Override
    public GenericGeneratorDoubleSequence persist(GenericGeneratorDoubleSequence record) {
        return super.persist(record);
    }
}
