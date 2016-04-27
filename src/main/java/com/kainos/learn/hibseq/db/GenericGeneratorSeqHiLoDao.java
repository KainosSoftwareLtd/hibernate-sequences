package com.kainos.learn.hibseq.db;

import com.kainos.learn.hibseq.domain.GenericGeneratorSeqHiLo;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

public class GenericGeneratorSeqHiLoDao extends AbstractDAO<GenericGeneratorSeqHiLo> {

    public GenericGeneratorSeqHiLoDao(SessionFactory factory) {
        super(factory);
    }

    public GenericGeneratorSeqHiLo get(Long id) {
        return super.get(id);
    }

    public List<GenericGeneratorSeqHiLo> list() {
        return list(criteria());
    }

    public void delete(GenericGeneratorSeqHiLo record) {
        currentSession().delete(record);
        currentSession().flush();
        currentSession().clear();
    }

    @Override
    public GenericGeneratorSeqHiLo persist(GenericGeneratorSeqHiLo record) {
        return super.persist(record);
    }
}
