package com.example.addonurl

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import groovy.json.JsonSlurper
// import groovy.transform.CompileStatic // Removed to fix compilation issues
import groovy.util.logging.Slf4j

/**
 * Service for loading and managing the JSON configuration for cloud provider URLs.
 * Supports both bundled default configuration and runtime override via plugin settings.
 */
@Slf4j
// @CompileStatic // Removed to fix compilation issues
class ConfigService {

    private final Plugin plugin
    private final MorpheusContext morpheus
    private final JsonSlurper jsonSlurper = new JsonSlurper()

    ConfigService(Plugin plugin, MorpheusContext morpheus) {
        this.plugin = plugin
        this.morpheus = morpheus
    }

    /**
     * Load the effective configuration, preferring plugin settings over bundled defaults.
     * @return Map containing the provider configurations
     */
    Map<String, Object> loadEffectiveConfig() {
        try {
            // Try to load from plugin settings first
            def settingsJson = getPluginSettingJson()
            if (settingsJson) {
                log.debug("Using plugin settings configuration")
                def config = jsonSlurper.parseText(settingsJson) as Map<String, Object>
                if (validateConfig(config)) {
                    return config
                } else {
                    log.warn("Plugin settings JSON is invalid, falling back to bundled config")
                }
            }

            // Fall back to bundled configuration
            log.debug("Using bundled configuration")
            return loadBundledConfig()

        } catch (Exception e) {
            log.error("Error loading configuration, using bundled defaults", e)
            return loadBundledConfig()
        }
    }

    /**
     * Get the JSON configuration from plugin settings.
     * @return String JSON content or null if not set
     */
    private String getPluginSettingJson() {
        try {
            def settings = morpheus.getSettings(plugin).blockingGet()
            return settings?.addonUrlJson?.toString()?.trim()
        } catch (Exception e) {
            log.warn("Error retrieving plugin settings", e)
            return null
        }
    }

    /**
     * Load the bundled JSON configuration from resources.
     * @return Map containing the provider configurations
     */
    private Map<String, Object> loadBundledConfig() {
        try {
            def configStream = this.class.classLoader.getResourceAsStream('config/addon-urls.json')
            if (configStream) {
                def configText = configStream.text
                return jsonSlurper.parseText(configText) as Map<String, Object>
            } else {
                log.error("Bundled configuration file not found")
                return [providers: [:]]
            }
        } catch (Exception e) {
            log.error("Error loading bundled configuration", e)
            return [providers: [:]]
        }
    }

    /**
     * Validate that the configuration has the required structure.
     * @param config The configuration map to validate
     * @return true if valid, false otherwise
     */
    private boolean validateConfig(Map<String, Object> config) {
        if (!config) {
            return false
        }
        
        def providers = config.providers
        if (!(providers instanceof Map)) {
            return false
        }

        // Validate each provider has a list of URL configurations
        for (entry in providers.entrySet()) {
            def providerUrls = entry.value
            if (!(providerUrls instanceof List)) {
                return false
            }
            
            for (urlConfig in providerUrls) {
                if (!(urlConfig instanceof Map)) {
                    return false
                }
                if (!urlConfig.label || !urlConfig.url) {
                    return false
                }
            }
        }

        return true
    }

    /**
     * Get the URL configurations for a specific cloud provider.
     * @param providerCode The provider code (e.g., 'azure', 'amazon', 'googlecloud')
     * @return List of URL configurations for the provider
     */
    List<Map<String, String>> getProviderUrls(String providerCode) {
        def config = loadEffectiveConfig()
        def providers = config.providers as Map<String, List<Map<String, String>>>
        return providers.get(providerCode) ?: []
    }

    /**
     * Check if a provider is configured in the current configuration.
     * @param providerCode The provider code to check
     * @return true if the provider is configured with at least one URL
     */
    boolean isProviderConfigured(String providerCode) {
        def urls = getProviderUrls(providerCode)
        return urls && !urls.isEmpty()
    }
}