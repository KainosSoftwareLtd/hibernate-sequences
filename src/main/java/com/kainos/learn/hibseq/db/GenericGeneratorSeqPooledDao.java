package com.kainos.learn.hibseq.db;

import com.kainos.learn.hibseq.domain.GenericGeneratorSeqPooled;
import org.hibernate.SessionFactory;

import java.util.List;

public class GenericGeneratorSeqPooledDao extends DomainEntityDao<GenericGeneratorSeqPooled> {

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
