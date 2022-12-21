package net.northern.crm.controllers;

import net.northern.crm.config.EnvConfig;
import net.northern.crm.persistence.services.NotificationService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;

public abstract class ControllerHelper {

    /**
     * INFO -> General Info<br>
     * ERROR -> Fatal error, will block request<br>
     * WARN -> Bad but will proceed<br>
     * SUCCESS -> Request succeeded
     */
    public enum AlertLevel {
        INFO,
        ERROR,
        WARN,
        SUCCESS;

        public String toLowerCase() {
            return name().toLowerCase();
        }
    }

    private final NotificationService notificationService;
    private final EnvConfig envConfig;

    public ControllerHelper(NotificationService notificationService, EnvConfig envConfig) {
        this.notificationService = notificationService;
        this.envConfig = envConfig;
    }

    protected ModelAndView configurePlainView(String view) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName(view);

        mv.addObject("notiList", notificationService.getNotiList());

        return mv;
    }

    protected void configureMessage(RedirectAttributes redirectAttributes, String message, AlertLevel alertLevel) {
        redirectAttributes.addAttribute("message", message);
        redirectAttributes.addAttribute("alert_level", alertLevel);
    }

    protected void configureMessage(ModelAndView mav, String message, AlertLevel alertLevel) {
        mav.addObject("message", message);
        mav.addObject("alert_level", alertLevel);
    }

    protected void cacheResponse(HttpServletResponse response) {
        if (envConfig.isCacheControlEnabled()) {
            response.setHeader("Cache-Control", "no-transform, must-revalidate, max-age=43200");
        }
    }


}
