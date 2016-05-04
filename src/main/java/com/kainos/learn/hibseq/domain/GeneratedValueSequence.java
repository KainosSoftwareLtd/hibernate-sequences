package com.kainos.learn.hibseq.domain;

import javax.persistence.*;

@Entity
@Table(name = "generated_value_sequence")
@SequenceGenerator(name = "gen_value_sequence", sequenceName = "gen_value_sequence")    // defaults to using pooled optimizer
public class GeneratedValueSequence extends DomainEntity {

    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(generator = "gen_value_sequence", strategy = GenerationType.SEQUENCE)
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
