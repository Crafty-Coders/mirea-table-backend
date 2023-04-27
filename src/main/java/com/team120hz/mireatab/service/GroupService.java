package com.team120hz.mireatab.service;

import com.team120hz.mireatab.model.Group;
import com.team120hz.mireatab.repository.GroupRepository;
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
}
