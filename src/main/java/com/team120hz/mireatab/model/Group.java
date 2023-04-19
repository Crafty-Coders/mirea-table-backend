package com.team120hz.mireatab.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "group")
public class Group {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    public String name;

    @OneToMany
    @Column(name = "lessons")
    public Set<Lesson> lessons;
}
