package com.kainos.learn.hibseq.domain;

import javax.persistence.*;

@Entity
@Table(name = "generated_value_sequence")
// defaults to using pooled optimizer with increment_size = 50 when allocationSize = 1 not set
@SequenceGenerator(name = "gen_value_sequence", sequenceName = "gen_value_sequence"/*, allocationSize = 1*/)
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
