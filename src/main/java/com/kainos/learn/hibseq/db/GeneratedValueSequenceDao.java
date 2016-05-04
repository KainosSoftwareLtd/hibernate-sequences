package com.kainos.learn.hibseq.db;

import com.kainos.learn.hibseq.domain.GeneratedValueSequence;
import org.hibernate.SessionFactory;

import java.util.List;

public class GeneratedValueSequenceDao extends DomainEntityDao<GeneratedValueSequence> {

    public GeneratedValueSequenceDao(SessionFactory factory) {
        super(factory);
    }

    public GeneratedValueSequence get(Long id) {
        return super.get(id);
    }

    public List<GeneratedValueSequence> list() {
        return list(criteria());
    }

    public void delete(GeneratedValueSequence record) {
        currentSession().delete(record);
        currentSession().flush();
        currentSession().clear();
    }

    @Override
    public GeneratedValueSequence persist(GeneratedValueSequence record) {
        return super.persist(record);
    }
}
