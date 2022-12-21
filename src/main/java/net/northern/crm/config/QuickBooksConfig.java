package net.northern.crm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "quickbooks", ignoreUnknownFields = false)
public class QuickBooksConfig {
    private final String oAuth2AppClientId;
    private final String oAuth2AppClientSecret;
    private final String oAuth2AppRedirectUri;
    private final String intuitAccountingAPIHost;

    @ConstructorBinding
    public QuickBooksConfig(String oAuth2AppClientId, String oAuth2AppClientSecret, String oAuth2AppRedirectUri, String intuitAccountingAPIHost) {
        this.oAuth2AppClientId = oAuth2AppClientId;
        this.oAuth2AppClientSecret = oAuth2AppClientSecret;
        this.oAuth2AppRedirectUri = oAuth2AppRedirectUri;
        this.intuitAccountingAPIHost = intuitAccountingAPIHost;
    }

    public String getOAuth2AppClientId() {
        return oAuth2AppClientId;
    }

    public String getOAuth2AppClientSecret() {
        return oAuth2AppClientSecret;
    }

    public String getOAuth2AppRedirectUri() {
        return oAuth2AppRedirectUri;
    }

    public String getIntuitAccountingAPIHost() {
        return intuitAccountingAPIHost;
    }
}
