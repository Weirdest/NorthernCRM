package net.northern.crm.controllers.web_controllers;

import net.northern.crm.config.EnvConfig;
import net.northern.crm.controllers.ControllerHelper;
import net.northern.crm.persistence.services.NotificationService;
import net.northern.crm.persistence.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class RootController extends ControllerHelper {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NotificationService notificationService;
    private final UsersService usersService;

    public RootController(EnvConfig envConfig, NotificationService notificationService, UsersService usersService) {
        super(notificationService, envConfig);
        this.notificationService = notificationService;
        this.usersService = usersService;
    }

    @GetMapping(value = {"/", "/dashboard"})
    public ModelAndView home() {
        return configurePlainView("dashboard");
    }

    @GetMapping("/login")
    public ModelAndView login() {
        return configurePlainView("login");
    }

    @GetMapping("/logout")
    public RedirectView logout(Authentication authentication, HttpServletRequest request) {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

        logoutHandler.logout(request, null, authentication);

        return new RedirectView("/login?logout");
    }

    @PostMapping("/dismiss/{id}")
    @ResponseBody
    public void dismissNoti(@PathVariable long id, Authentication authentication) {
        notificationService.dismiss(id, usersService.getUser(authentication.getName()));
    }

}
