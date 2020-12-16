package com.nci.prj.repositories;

import com.nci.prj.model.myUser;
import org.springframework.data.repository.CrudRepository;

/**
 * Interface UserRepository
 * <p>
 *
 * @author Sudhindra Joshi
 */
public interface UserRepository extends CrudRepository<myUser, String> {

    @Override
    public void delete(myUser user);

    myUser findByEmail(String email);
}
