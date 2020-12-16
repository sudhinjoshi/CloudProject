package com.nci.prj;

import com.nci.prj.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuration - SpringbootSecurity
 * <p>
 * This class responsible to managing the Spring boot Security
 *
 * @author Sudhindra Joshi
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    CustomizeAuthenticationSuccessHandler customizeAuthenticationSuccessHandler;

    @Bean
    public UserDetailsService mongoUserDetails() {
        return new CustomUserDetailsService();
    }

    /**
     * Method retrieve mongoDB details and peform authorization
     *
     * @param auth - AuthenticationManagerBUilder
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        System.out.println("Inside configure() ... ");
        UserDetailsService userDetailsService = mongoUserDetails();
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder);

    }

    /**
     * Method configures the various rules for the application on which Spring boot will operate
     *
     * @param http - HttpSecurity
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        System.out.println("Inside config(HttpSecurity)");

        http
                .authorizeRequests()
                .antMatchers("/", "/home", "/js/**", "/css/**","/images/**").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/signup").permitAll()
                //.antMatchers("/**","/dashboard","/").hasAuthority("USER")
                //.antMatchers("/productAdminDashboard/**").hasAuthority("PRODUCT_ADMIN")
                //.antMatchers("/userAdminDashboard/**").hasAuthority("USER_ADMIN")
                .antMatchers("/**","/dashboard","/").hasAnyAuthority("ADMIN","USER").anyRequest()
                .authenticated().and().csrf().disable().formLogin().successHandler(customizeAuthenticationSuccessHandler)
                .loginPage("/login").failureUrl("/login?error=true")
                .usernameParameter("email")
                .passwordParameter("password")
                .and().logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/").and().exceptionHandling();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/static/images/**");
    }

}