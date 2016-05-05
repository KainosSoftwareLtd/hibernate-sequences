package com.kainos.learn.hibseq.db.id.generator;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.BulkInsertionCapableIdentifierGenerator;
import org.hibernate.id.Configurable;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * A sample custom sequence generator. Uses two sequences to create a value of "ID1_ID2" String format.
 */
public class DoubleSequenceGenerator implements PersistentIdentifierGenerator, BulkInsertionCapableIdentifierGenerator, Configurable {
    public static final String SECOND_SEQ = "second_sequence";
    public static final String FIRST_SEQ = "sequence";

    private String sequenceName;
    private String secondSequenceName;
    private String sql;
    private String sql2;

    public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
        ObjectNameNormalizer normalizer = (ObjectNameNormalizer) params.get(IDENTIFIER_NORMALIZER);
        secondSequenceName =  normalizer.normalizeIdentifierQuoting(
                ConfigurationHelper.getString(SECOND_SEQ, params)
        );
        sequenceName = normalizer.normalizeIdentifierQuoting(
                ConfigurationHelper.getString(FIRST_SEQ, params, "hibernate_sequence")
        );

        sql = dialect.getSequenceNextValString(sequenceName);
        sql2 = dialect.getSequenceNextValString(secondSequenceName);
    }

    @Override
    public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
        String[] ddl = Stream.concat(
                Arrays.stream(dialect.getCreateSequenceStrings(sequenceName, 1, 1)),
                Arrays.stream(dialect.getCreateSequenceStrings(secondSequenceName, 1, 1)))
                .toArray(String[]::new);

        return ddl;
    }

    @Override
    public String[] sqlDropStrings(Dialect dialect) throws HibernateException {
        String[] ddl = Stream.concat(
                Arrays.stream(dialect.getDropSequenceStrings(sequenceName)),
                Arrays.stream(dialect.getDropSequenceStrings(secondSequenceName)))
                .toArray(String[]::new);

        return ddl;
    }

    @Override
    public Object generatorKey() {
        return sequenceName;
    }

    /**
     * Method used to generate ID1_ID2 identifier. Should be synchronized if we want them to be ordered in some way,
     * since we call two sequences.
     *
     * @param session
     * @param obj
     * @return
     */
    public Serializable generate(final SessionImplementor session, Object obj) {
        Long value = null;
        while (value == null || value < 0) {
            value = generateValue(session, sql);
        }

        Long value2 = null;
        while (value2 == null || value2 < 0) {
            value2 = generateValue(session, sql2);
        }

        return value + "_" + value2;
    }

    private Long generateValue(SessionImplementor session, String query) {
        try {
            PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(query);
            try {
                ResultSet rs = session.getTransactionCoordinator().getJdbcCoordinator().getResultSetReturn().extract(st);
                try {
                    rs.next();
                    Long id = rs.getLong(1);
                    return id;
                }
                finally {
                    session.getTransactionCoordinator().getJdbcCoordinator().release(rs, st);
                }
            }
            finally {
                session.getTransactionCoordinator().getJdbcCoordinator().release(st);
            }
        }
        catch (SQLException sqle) {
            throw session.getFactory().getSQLExceptionHelper().convert(
                    sqle,
                    "could not get next sequence value",
                    sql2
            );
        }
    }

    @Override
    public boolean supportsBulkInsertionIdentifierGeneration() {
        return false;
    }

    @Override
    public String determineBulkInsertionIdentifierGenerationSelectFragment(Dialect dialect) {
        return null;
    }
}
