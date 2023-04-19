package com.team120hz.mireatab.model;

import com.team120hz.mireatab.tools.Campus;
import com.team120hz.mireatab.tools.LessonType;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "lesson")
public class Lesson {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    public String name;

    @Column(name = "even_week")
    public boolean evenWeek;

    @Column(name = "excluded_weeks")
    @ElementCollection
    public Collection<Integer> excludedWeeks;

    @Column(name = "included_weeks")
    @ElementCollection
    public Collection<Integer> includedWeeks;

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

    @ElementCollection
    @Column(name = "teachers")
    public Collection<String> teachers;

    @ManyToOne
    @Column(name = "group")
    public Group group;
}
