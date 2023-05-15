package com.craftycoders.mireatab.service;

import com.craftycoders.mireatab.repository.LessonRepository;
import com.craftycoders.mireatab.model.Lesson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;

    @Autowired
    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    public Lesson save(Lesson lesson) {
        return lessonRepository.save(lesson);
    }
}
