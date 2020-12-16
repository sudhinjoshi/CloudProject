package com.nci.prj;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration - PageConfig
 * <p>
 * This class returns a BCryptPasswordEncoder as well as Registers few views
 *
 * @author Sudhindra Joshi
 */
@Configuration
public class PageConfig implements WebMvcConfigurer {

    /**
     * Bean - retrieves the BCryptionPasswordEncoder used while storing User Password
     *
     * @return BCryptPasswordEncoder object
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }

    /**
     * Method - Register Views
     *
     * @param registry ViewContollerRegistry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        System.out.println("Inside addViewControllers");
        registry.addViewController("/home").setViewName("home");
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/dashboard").setViewName("dashboard");
        registry.addViewController("/login").setViewName("login");
    }


}
