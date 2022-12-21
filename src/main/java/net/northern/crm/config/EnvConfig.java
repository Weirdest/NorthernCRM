package net.northern.crm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "cccandle")
public class EnvConfig {

    private final boolean production;
    private final boolean enableInMemorySettingsCache;
    private final boolean cacheControlEnabled;

    public EnvConfig(boolean production, boolean enableInMemorySettingsCache, boolean cacheControlEnabled) {
        this.production = production;
        this.enableInMemorySettingsCache = enableInMemorySettingsCache;
        this.cacheControlEnabled = cacheControlEnabled;
    }

    public boolean isProduction() {
        return production;
    }

    public boolean isEnableInMemorySettingsCache() {
        return enableInMemorySettingsCache;
    }

    public boolean isCacheControlEnabled() {
        return cacheControlEnabled;
    }
}
