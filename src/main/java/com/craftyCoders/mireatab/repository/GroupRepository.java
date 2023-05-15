package com.craftyCoders.mireatab.repository;

import com.craftyCoders.mireatab.model.Group;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends CrudRepository<Group, Long> {
    List<Group> findAllByName(String name);
}
