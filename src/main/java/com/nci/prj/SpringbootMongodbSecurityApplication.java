package com.nci.prj;

import com.nci.prj.model.Role;
import com.nci.prj.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

public class SpringbootMongodbSecurityApplication {

    /**
     * CommandLineRunner
     * <p>
     * This class executes before the application and creates the ADMIN and USER roles in MongoDB
     *
     * @author Sudhindra Joshi
     */
    @Bean
    CommandLineRunner init(RoleRepository roleRepository) {

        return args -> {

            Role adminRole = roleRepository.findByRole("ADMIN");
            if (adminRole == null) {
                Role newAdminRole = new Role();
                newAdminRole.setRole("ADMIN");
                roleRepository.save(newAdminRole);
            }

            Role userRole = roleRepository.findByRole("USER");
            if (userRole == null) {
                Role newUserRole = new Role();
                newUserRole.setRole("USER");
                roleRepository.save(newUserRole);
            }
        };

    }


}