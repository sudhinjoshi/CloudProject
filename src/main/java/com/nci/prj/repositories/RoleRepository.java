package com.nci.prj.repositories;

import com.nci.prj.model.Role;
import com.nci.prj.model.myUser;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, String> {

    Role findByRole(String role);
}
