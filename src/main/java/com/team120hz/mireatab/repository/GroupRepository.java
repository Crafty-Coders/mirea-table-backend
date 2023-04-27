package com.team120hz.mireatab.repository;

import com.team120hz.mireatab.model.Group;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends CrudRepository<Group, Long> {
    List<Group> findAllByName(String name);
}
