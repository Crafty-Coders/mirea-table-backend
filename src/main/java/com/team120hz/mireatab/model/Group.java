package com.team120hz.mireatab.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import java.util.Set;

@Entity
public class Group {
    @Id
    private Long id;

    public String name;

    @ManyToMany
    public Set<Lesson> lessons;
}
