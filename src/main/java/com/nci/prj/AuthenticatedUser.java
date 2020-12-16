package com.nci.prj;

import com.nci.prj.model.myUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Class - AuthenticatedUser
 * <p>
 * This class maps custom User to Spring UserDetails
 *
 * @author Sudhindra Joshi
 */
public class AuthenticatedUser implements UserDetails {
    private myUser user; // user is my own model, not of spring-framework

    public AuthenticatedUser(myUser user) {
        this.user = user;
    }

    //no roles or authorities modeled, yet
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
