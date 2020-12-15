package com.nci.prj.controller;

import com.nci.prj.model.myUser;
import com.nci.prj.repositories.RoleRepository;
import com.nci.prj.repositories.UserRepository;
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
public class LoginController {

    @Autowired
    private CustomUserDetailsService userService;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView login() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public ModelAndView signup() {
        ModelAndView modelAndView = new ModelAndView();
        myUser user = new myUser();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("signup");
        modelAndView.addObject("roles", roleRepository.findAll());
        modelAndView.addObject("successMessage", "");
        return modelAndView;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ModelAndView createNewUser(@Valid myUser user, BindingResult bindingResult, @RequestParam String roles) {
        System.out.println("createNewUser ##: " + roles);
        myUser userExists = null;
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("roles", roleRepository.findAll());
        try {
            userExists = userService.findUserByEmail(user.getEmail());
            System.out.println("userExists? " + userExists.toString());
        } catch (Exception e) {
            System.out.println("Caught exception: " + e.getMessage());
        }
        if (userExists != null) {
            System.out.println("User already exits");
            bindingResult
                    .rejectValue("email", "error.user",
                            "There is already a user registered with the username provided");
            modelAndView.addObject("successMessage", "There is already a user registered with the username provided");
        } else {
            userService.saveUser(user, roles);
            modelAndView.addObject("successMessage", "User has been registered successfully. Please login");
            //modelAndView.addObject("user", new myUser());
            modelAndView.setViewName("login");

        }
        return modelAndView;
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public ModelAndView dashboard() {
        System.out.println("LoginController.dashboard()");
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        myUser user = userService.findUserByEmail(auth.getName());
        System.out.println("currentUser(): " + user.getFullname());
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentUser", user);
        modelAndView.addObject("fullName", "Welcome " + user.getFullname());
        modelAndView.addObject("adminMessage", "Content Available Only for Users with Admin Role");
        modelAndView.setViewName("newDashboard");
        return modelAndView;
    }

    @RequestMapping(value = "/userdashboard", method = RequestMethod.GET)
    public ModelAndView userdashboard() {
        System.out.println("LoginController.userdashboard()");
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        myUser user = userService.findUserByEmail(auth.getName());
        System.out.println("currentUser(): " + user.getFullname());
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentUser", user);
        modelAndView.addObject("fullName", "Welcome " + user.getFullname());
        modelAndView.addObject("adminMessage", "Content Available Only for Users with Admin Role");
        modelAndView.setViewName("userdashboard");
        return modelAndView;
    }
    
    @RequestMapping(value = "/listUsers", method = RequestMethod.GET)
    public ModelAndView listUserDetails() {
        System.out.println("Listing Users");
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        myUser user = userService.findUserByEmail(auth.getName());
        System.out.println("currentUser(): " + user.getFullname());
        modelAndView.addObject("user", user);
        modelAndView.addObject("users", userRepository.findAll());
        //modelAndView.addObject("roles", roleRepository.findAll());
        modelAndView.addObject("currentUser", user);
        modelAndView.addObject("fullName", "Welcome " + user.getFullname());
        modelAndView.addObject("adminMessage", "Content Available Only for Users with Admin Role");
        modelAndView.setViewName("listUsers");
        return modelAndView;
    }

    @RequestMapping(value = {"/", "/home"}, method = RequestMethod.GET)
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        return modelAndView;
    }

}
