package net.northern.crm.controllers.web_controllers;

import net.northern.crm.config.EnvConfig;
import net.northern.crm.config.QuickBooksConfig;
import net.northern.crm.controllers.ControllerHelper;
import net.northern.crm.persistence.services.NotificationService;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.config.OAuth2Config;
import com.intuit.oauth2.config.Scope;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;
import net.northern.crm.persistence.services.QuickBooksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/qb")
public class QBController extends ControllerHelper {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final QuickBooksService quickBooksService;
    private final QuickBooksConfig quickBooksConfig;

    @Autowired
    public QBController(QuickBooksService quickBooksService, QuickBooksConfig quickBooksConfig, EnvConfig envConfig, NotificationService notificationService) {
        super(notificationService, envConfig);
        this.quickBooksService = quickBooksService;
        this.quickBooksConfig = quickBooksConfig;
    }

    @GetMapping()
    public ModelAndView mainPage() {
        return configurePlainView("qb");
    }

    @RequestMapping("/connectToQuickbooks")
    public View connectToQuickbooks() {
        logger.info("Attempting to connect to quickbooks...");
        OAuth2Config oauth2Config = quickBooksService.getOAuth2Config();

        String redirectUri = quickBooksConfig.getOAuth2AppRedirectUri();

        String csrf = oauth2Config.generateCSRFToken();
        quickBooksService.storeSetting(QuickBooksService.KEY_CSRF_TOKEN, csrf);
        try {
            List<Scope> scopes = new ArrayList<>();
            scopes.add(Scope.Accounting);
            return new RedirectView(oauth2Config.prepareUrl(scopes, redirectUri, csrf), true, true, false);
        } catch (Exception e) {
            logger.error("Exception calling connectToQuickbooks ", e);
        }
        return null;
    }

    @RequestMapping("/oauth2redirect")
    public RedirectView callBackFromOAuth(@RequestParam("code") String authCode, @RequestParam("state") String state, @RequestParam(value = "realmId", required = false) String realmId, RedirectAttributes redirectAttributes) {
        logger.info("Returned from QuickBooks");
        try {
            String csrfToken = quickBooksService.getSetting(QuickBooksService.KEY_CSRF_TOKEN);
            if (csrfToken.equals(state)) {
                //Unsure if session attributes are used in internal quickbooks api
                //session.setAttribute(QuickBooksModel.KEY_REALM_ID, realmId);
                //session.setAttribute(QuickBooksModel.KEY_AUTH_CODE, authCode);

                OAuth2PlatformClient client  = quickBooksService.getOAuth2PlatformClient();
                String redirectUri = quickBooksConfig.getOAuth2AppRedirectUri();

                BearerTokenResponse bearerTokenResponse = client.retrieveBearerTokens(authCode, redirectUri);

                //session.setAttribute(QuickBooksModel.KEY_ACCESS_TOKEN, bearerTokenResponse.getAccessToken());
                //session.setAttribute(QuickBooksModel.KEY_REFRESH_TOKEN, bearerTokenResponse.getRefreshToken());

                quickBooksService.storeSetting(QuickBooksService.KEY_REALM_ID, realmId);
                quickBooksService.storeSetting(QuickBooksService.KEY_AUTH_CODE, authCode);
                quickBooksService.storeSetting(QuickBooksService.KEY_ACCESS_TOKEN, bearerTokenResponse.getAccessToken());
                quickBooksService.storeSetting(QuickBooksService.KEY_REFRESH_TOKEN, bearerTokenResponse.getRefreshToken());

                configureMessage(redirectAttributes, "Connected!", AlertLevel.SUCCESS);
                return new RedirectView("/qb");
            }
            logger.info("csrf token mismatch " );
        } catch (OAuthException e) {
            logger.error("Exception in callback handler ", e);
        } catch (Exception e) {
            logger.error("Unknown Error", e);
        }
        configureMessage(redirectAttributes, "An error occurred", AlertLevel.ERROR);
        return new RedirectView("/qb");
    }
}
