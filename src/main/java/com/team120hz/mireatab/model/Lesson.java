package com.team120hz.mireatab.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import java.util.Collection;
import java.util.Set;

@Entity
public class Lesson {
    @Id
    private Long id;

    public String name;

    public int week;

    public int number;

    public int day;

    @ElementCollection
    public Collection<String> teachers;

    @ManyToMany
    public Set<Group> groups;
}
