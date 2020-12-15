package com.nci.prj.controller;

import com.nci.prj.model.myUser;
import com.nci.prj.model.Role;
import com.nci.prj.repositories.RoleRepository;
import com.nci.prj.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class AdminController {

    @Autowired
    private CustomUserDetailsService userService;

    @Autowired
    private RoleRepository roleRepository;

    @RequestMapping(value = "/newDashboard", method = RequestMethod.GET)
    public ModelAndView lognewDashboard() {
        System.out.println("AdminController.lognewDashboard()");
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("auth: " + auth.getName());
        myUser user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("user", user);
        modelAndView.addObject("fullName", "Welcome " + user.getFullname());
        modelAndView.addObject("adminMessage", "Content Available Only for Users with Admin Role");
        modelAndView.setViewName("newDashboard");
        
        for (Role chkRole : user.getRoles()) {
            System.out.println("chkRole: " + chkRole);
            if (chkRole.getRole().equalsIgnoreCase("admin")) {
                modelAndView.addObject("userisadmin", true);
            }
        }
        
        return modelAndView;
    }

}
