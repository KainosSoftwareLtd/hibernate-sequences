package com.kainos.learn.hibseq.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

@Entity
@Table(name = "generic_generator_double_sequence")
@GenericGenerator(name = "gen_double_sequence", strategy = "com.kainos.learn.hibseq.db.id.generator.DoubleSequenceGenerator",
        parameters = {
                @Parameter(name = "sequence", value = "gen_double_seq"),
                @Parameter(name = "second_sequence", value = "gen_double_seq_2")
        })
public class GenericGeneratorDoubleSequence extends DomainEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_double_sequence")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
