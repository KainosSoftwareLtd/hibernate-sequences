package com.kainos.learn.hibseq.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

@Entity
@Table(name = "generic_generator_sequence")
@GenericGenerator(name = "gen_generator_sequence", strategy = "sequence",
        parameters = {
                @Parameter(name = "parameters", value = "CACHE 100"),
                @Parameter(name = "sequence", value = "gen_generator_seq")
        })
public class GenericGeneratorSequence extends DomainEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_generator_sequence")
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
