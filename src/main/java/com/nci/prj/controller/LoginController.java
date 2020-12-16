package com.nci.prj.controller;

import com.nci.prj.model.myUser;
import com.nci.prj.model.Role;
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

/**
 * Login Controller
 * <p>
 * This controller handles GET Endpoints for Signup as well as User Dashboard and User Listing
 *
 * @author Sudhindra Joshi
 */
@Controller
public class LoginController {

    @Autowired
    private CustomUserDetailsService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Method renders the login view
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView login() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }

    /**
     * Method renders the signup view
     */
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

    /**
     * Method perform POST operation Signup for new users
     *
     * @param user  - User details
     * @param roles - Role of the User
     */
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

    /**
     * Method perform GET operation for admin user dashboard
     *
     * @return ModelAndView - newDashboard
     */
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

        for (Role chkRole : user.getRoles()) {
            System.out.println("chkRole: " + chkRole);
            if (chkRole.getRole().equalsIgnoreCase("admin")) {
                modelAndView.addObject("userisadmin", true);
            }
        }

        return modelAndView;
    }

    /**
     * Method perform GET operation for common user dashboard
     *
     * @return ModelAndView - userDashboard
     */
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

        for (Role chkRole : user.getRoles()) {
            System.out.println("chkRole: " + chkRole);
            if (chkRole.getRole().equalsIgnoreCase("admin")) {
                modelAndView.addObject("userisadmin", true);
            }
        }

        return modelAndView;
    }

    /**
     * Method perform GET operation for listing all user
     *
     * @return ModelAndView - listUsers
     */
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

        for (Role chkRole : user.getRoles()) {
            System.out.println("chkRole: " + chkRole);
            if (chkRole.getRole().equalsIgnoreCase("admin")) {
                modelAndView.addObject("userisadmin", true);
            }
        }

        return modelAndView;
    }

    @RequestMapping(value = {"/", "/home"}, method = RequestMethod.GET)
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        return modelAndView;
    }

}
