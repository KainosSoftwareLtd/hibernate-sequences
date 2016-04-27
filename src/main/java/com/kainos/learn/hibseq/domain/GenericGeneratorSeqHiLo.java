package com.kainos.learn.hibseq.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

@Entity
@Table(name = "generic_generator_seqhilo")
@GenericGenerator(
        name = "generic_generator_seqhilo",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @Parameter(name = "sequence_name", value = "hilo_seqeunce"),
                @Parameter(name = "increment_size", value = "50"),
                @Parameter(name = "optimizer", value = "hilo")
        })
public class GenericGeneratorSeqHiLo {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generic_generator_seqhilo")
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
