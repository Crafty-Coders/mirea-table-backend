package com.craftycoders.mireatab.service;

import com.craftycoders.mireatab.repository.GroupRepository;
import com.craftycoders.mireatab.model.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {
    private final GroupRepository groupRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public Group save(Group group) {
        return groupRepository.save(group);
    }

    public List<Group> findAllByName(String name) {
        return groupRepository.findAllByName(name);
    }

    public List<Group> findAll() {
        return (List<Group>) groupRepository.findAll();
    }
}
