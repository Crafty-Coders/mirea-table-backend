package com.craftyCoders.mireatab.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "study_group")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    public String name;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    public Set<Lesson> lessons;

    public Group() {

    }
}
