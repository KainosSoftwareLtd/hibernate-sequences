package com.kainos.learn.hibseq.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

@Entity
@Table(name = "generic_generator_seqpooledlo")
@GenericGenerator(
        name = "generic_generator_seqpooledlo_seq",
        strategy = "enhanced-sequence",     // org.hibernate.id.enhanced.SequenceStyleGenerator
        parameters = {
                @Parameter(name = "sequence_name", value = "generic_generator_seqpooledlo_seq"),
                @Parameter(name = "increment_size", value = "50"),
                @Parameter(name = "optimizer", value = "pooled-lo")
        })
public class GenericGeneratorSeqPooledLo extends DomainEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generic_generator_seqpooledlo_seq")
    private Long id;

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
