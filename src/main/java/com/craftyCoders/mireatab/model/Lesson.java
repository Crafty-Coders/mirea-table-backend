package com.craftyCoders.mireatab.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.craftyCoders.mireatab.tools.Campus;
import com.craftyCoders.mireatab.tools.LessonType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "lesson")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    public String name;

    @Column(name = "even_week")
    public boolean evenWeek;

    @Column(name = "excluded_weeks")
    @ElementCollection
    public List<Integer> excludedWeeks = new ArrayList<>();

    @Column(name = "included_weeks")
    @ElementCollection
    public List<Integer> includedWeeks = new ArrayList<>();

    @Column(name = "number")
    public int number;

    @Column(name = "day")
    public int day;

    @Column(name = "type")
    public LessonType type;

    @Column(name = "campus")
    public Campus campus;

    @Column(name = "auditory")
    public String auditory;

    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonIgnore
    public Group group;

    @ElementCollection
    public List<String> teachers = new ArrayList<>();


}
