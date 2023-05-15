package com.craftycoders.mireatab.controller;

import com.craftycoders.mireatab.dto.GetGroupLessonsForm;
import com.craftycoders.mireatab.model.Group;
import com.craftycoders.mireatab.model.Lesson;
import com.craftycoders.mireatab.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
public class LessonsController {

    private final GroupService groupService;

    @Autowired
    public LessonsController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/get-group-lessons")
    public ResponseEntity<?> getGroupLessons(@RequestBody GetGroupLessonsForm body) {
        List<Group> groups = groupService.findAllByName(body.getName());
        if (groups.size() == 0) {
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Set<Lesson> groupLessons = groups.get(0).lessons;
        return new ResponseEntity<>(groupLessons, HttpStatus.OK);
    }

    @GetMapping("get-groups")
    public ResponseEntity<?> getGroups() {
        List<Group> groups = groupService.findAll();
        return new ResponseEntity<>(groups, HttpStatus.OK);
    }
}
