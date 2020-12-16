package com.nci.prj.services;

import com.nci.prj.model.Role;
import com.nci.prj.model.myUser;
import com.nci.prj.repositories.RoleRepository;
import com.nci.prj.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service - CustomUserDetailsService
 * <p>
 * This service is used for Authentication, Authorisation and saving of Users
 *
 * @author Sudhindra Joshi
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * Method to find users based on email Id
     *
     * @param email - email Name to be searched
     * @return user details
     */
    public myUser findUserByEmail(String email) {
        System.out.println("Inside findUserByEmail: " + email);
        myUser user = userRepository.findByEmail(email);
        if (user != null) {
            System.out.println("findUserByEmail user!= null: " + user.getFullname());
            return user;
        } else {
            System.out.println("user  not found ");
            throw new UsernameNotFoundException("username not found");
        }
    }

    /**
     * Method to find users and map them in UserDetails
     *
     * @param email - email Name to be searched
     * @return UserDetails
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Inside loadUserByUsername: " + email);
        myUser user = userRepository.findByEmail(email);
        if (user != null) {
            List<GrantedAuthority> authorities = getUserAuthority(user.getRoles());
            return buildUserForAuthentication(user, authorities);
        } else {
            throw new UsernameNotFoundException("username not found");
        }
    }

    /**
     * Method to update UserDetails object with username, password and authorities
     *
     * @param user - email Name to be searched
     * @return UseDetails
     */
    private UserDetails buildUserForAuthentication(myUser user, List<GrantedAuthority> authorities) {
        System.out.println("Inside buildUserForAuthentication: ");
        return new org.springframework.security.core.userdetails.User
                (user.getEmail(), user.getPassword(), authorities);
    }

    /**
     * Method to find users authority
     *
     * @param userRoles - Role for the User
     * @return list of Roles
     */
    private List<GrantedAuthority> getUserAuthority(Set<Role> userRoles) {
        System.out.println("Inside getUserAuthority(): ");
        Set<GrantedAuthority> roles = new HashSet<>();
        userRoles.forEach((role) -> {
            roles.add(new SimpleGrantedAuthority(role.getRole()));
        });

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>(roles);
        return grantedAuthorities;
    }

    /**
     * Method to save users
     *
     * @param user - email Name/user to be saved
     * @param role - User Role
     */
    public void saveUser(myUser user, String role) {
        System.out.println("Inside saveUser(): ");
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        Role userRole = roleRepository.findByRole(role);
        user.setRoles(new HashSet<>(Arrays.asList(userRole)));
        userRepository.save(user);
    }

}
