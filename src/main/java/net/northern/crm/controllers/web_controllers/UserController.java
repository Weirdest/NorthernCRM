package net.northern.crm.controllers.web_controllers;

import net.northern.crm.config.EnvConfig;
import net.northern.crm.controllers.ControllerHelper;
import net.northern.crm.persistence.repositories.AuthoritiesRepository;
import net.northern.crm.persistence.services.NotificationService;
import net.northern.crm.persistence.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController extends ControllerHelper {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UsersService usersService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthoritiesRepository authoritiesRepository;

    @Autowired
    public UserController(UsersService usersService, PasswordEncoder passwordEncoder, AuthoritiesRepository authoritiesRepository, EnvConfig envConfig, NotificationService notificationService) {
        super(notificationService, envConfig);
        this.usersService = usersService;
        this.passwordEncoder = (BCryptPasswordEncoder) passwordEncoder;
        this.authoritiesRepository = authoritiesRepository;
    }

    @GetMapping
    public ModelAndView manageUsers() {

        ModelAndView mav = configurePlainView("users");
        mav.addObject("users", usersService.getAllUsers());

        return mav;
    }

    @PostMapping
    public RedirectView createUser(@RequestParam String username, RedirectAttributes redirectAttributes) {

        if (usersService.createUser(username) == UsersService.Result.SUCCESS) {
            configureMessage(redirectAttributes,
                    "User Created, please assign a password, permissions, and enable them.",
                    AlertLevel.SUCCESS);

            //Create location to be used as cart
            //locationsRepository.save(new LocationEntity(username, LocationEntity.LocationType.CART));
        } else {
            configureMessage(redirectAttributes, "Username Taken", AlertLevel.ERROR);
        }

        return new RedirectView("/users/" + username);
    }

    @GetMapping("/{username}")
    public ModelAndView displayUser(@PathVariable String username) {
        ModelAndView modelAndView = configurePlainView("solo_user");

        List<String> authorities = new LinkedList<>();
        authoritiesRepository.findAll().forEach(authorityEntity -> authorities.add(authorityEntity.getAuthority()));

        modelAndView.addObject("globalAuthorities", authorities);

        UserDetails userDetails = usersService.getUser(username);

        if (userDetails == null) {
            modelAndView.addObject("user", null);

            configureMessage(modelAndView, "User not found", AlertLevel.ERROR);
            return modelAndView;
        }

        ListUser user = new ListUser(userDetails.getUsername(), userDetails.isEnabled());
        user.setAuthorityList(new ArrayList<>(userDetails.getAuthorities()));

        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping("/{username}/change_state")
    public RedirectView changeState(@RequestParam boolean enabled, @PathVariable String username, RedirectAttributes redirectAttributes) {

        String requester = ((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        switch (usersService.changeUserState(enabled, username, requester)) {
            case DENIED_CHANGE -> {
                configureMessage(redirectAttributes, "Cannot change another admins attributes", AlertLevel.ERROR);
                return new RedirectView("/users/" + username);
            }
            case NOT_FOUND -> {
                configureMessage(redirectAttributes, "Username not found", AlertLevel.ERROR);
                return new RedirectView("/users/");
            }
            case SUCCESS -> {
                configureMessage(redirectAttributes, "User " + (enabled ? "Enabled" : "Disabled"), enabled ? AlertLevel.INFO : AlertLevel.WARN);
                return new RedirectView("/users/" + username);
            }
            default -> {
                configureMessage(redirectAttributes, "Unknown Error", AlertLevel.ERROR);
                return new RedirectView("/users/");
            }
        }
    }

    @PostMapping("/{username}/change_authorities")
    public RedirectView changeAuthorities(@RequestParam(required = false) String[] authorities, @PathVariable String username, RedirectAttributes redirectAttributes) {
        String requester = ((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        switch (usersService.changeUserAuthorities(authorities, username, requester)) {
            case DENIED_CHANGE -> {
                configureMessage(redirectAttributes, "Cannot change another admins attributes", AlertLevel.ERROR);
                return new RedirectView("/users/" + username);
            }
            case NOT_FOUND -> {
                configureMessage(redirectAttributes, "Username not found", AlertLevel.ERROR);
                return new RedirectView("/users/");
            }
            case SUCCESS -> {
                configureMessage(redirectAttributes, "Authorities updated", AlertLevel.SUCCESS);
                return new RedirectView("/users/" + username);
            }
            default -> {
                configureMessage(redirectAttributes, "Unknown Error", AlertLevel.ERROR);
                return new RedirectView("/users/");
            }
        }
    }

    @PostMapping("/{username}/change_password")
    public RedirectView changePassword(@RequestParam String password, @PathVariable String username, RedirectAttributes redirectAttributes) {
        String requester = ((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        switch (usersService.changeUserPassword(username, passwordEncoder.encode(password), requester)) {
            case DENIED_CHANGE -> {
                configureMessage(redirectAttributes, "Cannot change another admins attributes", AlertLevel.ERROR);
                return new RedirectView("/users/" + username);
            }
            case NOT_FOUND -> {
                configureMessage(redirectAttributes, "Username not found", AlertLevel.ERROR);
                return new RedirectView("/users/");
            }
            case SUCCESS -> {
                configureMessage(redirectAttributes, "Password Changed", AlertLevel.SUCCESS);
                return new RedirectView("/users/" + username);
            }
            default -> {
                configureMessage(redirectAttributes, "Unknown Error", AlertLevel.ERROR);
                return new RedirectView("/users/");
            }
        }
    }

    public static class ListUser {

        private final String username;
        private List<GrantedAuthority> authorityList;
        private final boolean enabled;

        public ListUser(String username, boolean enabled) {
            this.username = username;
            this.authorityList = new ArrayList<>();
            this.enabled = enabled;
        }

        public void setAuthorityList(List<GrantedAuthority> authorityList) {
            this.authorityList = authorityList;
        }

        public boolean hasAuthority(String authority) {
            for (GrantedAuthority auth : authorityList) {
                if(auth.getAuthority().equals(authority)) {
                    return true;
                }
            }

            return false;
        }

        public String getUsername() {
            return username;
        }

        public void addAuthority(GrantedAuthority authority) {
            this.authorityList.add(authority);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean hasAuthority() {
            return authorityList.size() > 0;
        }

        public ArrayList<GrantedAuthority> getAuthorities() {
            return (ArrayList<GrantedAuthority>) authorityList;
        }

    }
}
