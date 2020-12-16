package com.nci.prj.repositories;

import com.nci.prj.model.Role;
import org.springframework.data.repository.CrudRepository;

/**
 * Interface RoleRepository
 * <p>
 *
 * @author Sudhindra Joshi
 */
public interface RoleRepository extends CrudRepository<Role, String> {

    Role findByRole(String role);
}
