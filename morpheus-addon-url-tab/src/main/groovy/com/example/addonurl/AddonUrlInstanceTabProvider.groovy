package com.example.addonurl

import com.morpheusdata.core.AbstractInstanceTabProvider
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.model.Account
import com.morpheusdata.model.Instance
import com.morpheusdata.model.User
import com.morpheusdata.views.HTMLResponse
import com.morpheusdata.views.ViewModel
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Instance Tab Provider for the Add-on URL plugin.
 * Provides a tab that displays cloud-specific external dashboard links.
 */
@Slf4j
@CompileStatic
class AddonUrlInstanceTabProvider extends AbstractInstanceTabProvider {

    private final Plugin plugin
    private final MorpheusContext morpheus
    private final ConfigService configService
    private final UrlTemplateService urlTemplateService

    AddonUrlInstanceTabProvider(Plugin plugin, MorpheusContext morpheus) {
        this.plugin = plugin
        this.morpheus = morpheus
        this.configService = new ConfigService(plugin, morpheus)
        this.urlTemplateService = new UrlTemplateService(plugin, morpheus)
    }

    @Override
    MorpheusContext getMorpheus() {
        return morpheus
    }

    @Override
    Plugin getPlugin() {
        return plugin
    }

    @Override
    String getCode() {
        return 'addon-url-tab'
    }

    @Override
    String getName() {
        return 'Add-on URL'
    }

    @Override
    String getIcon() {
        return 'addon-url.svg'
    }

    /**
     * Determine if the tab should be shown for the given instance.
     * The tab is only shown if the instance's cloud provider is configured in the JSON.
     */
    @Override
    Boolean show(Instance instance, User user, Account account) {
        try {
            if (!instance?.cloud?.zoneType?.code) {
                log.debug("Instance ${instance?.id} has no cloud or zone type code, hiding tab")
                return false
            }

            def providerCode = instance.cloud.zoneType.code
            log.debug("Checking if tab should be shown for instance ${instance.id}, provider: ${providerCode}")

            // Check if this provider is configured
            def isConfigured = configService.isProviderConfigured(providerCode)
            
            if (isConfigured) {
                // Additionally check if at least one URL can be templated
                def urlConfigs = configService.getProviderUrls(providerCode)
                def context = urlTemplateService.resolveContext(instance)
                def processedButtons = urlTemplateService.processUrlConfigs(urlConfigs, context)
                
                def shouldShow = !processedButtons.isEmpty()
                log.debug("Tab visibility for instance ${instance.id}: ${shouldShow} (${processedButtons.size()} buttons available)")
                return shouldShow
            } else {
                log.debug("Provider ${providerCode} not configured, hiding tab for instance ${instance.id}")
                return false
            }

        } catch (Exception e) {
            log.error("Error determining tab visibility for instance ${instance?.id}", e)
            return false
        }
    }

    /**
     * Render the tab content.
     */
    @Override
    HTMLResponse renderTemplate(Instance instance, User user, Account account) {
        try {
            log.debug("Rendering Add-on URL tab for instance ${instance.id}")

            def providerCode = instance?.cloud?.zoneType?.code
            if (!providerCode) {
                return renderEmptyTemplate("Instance has no cloud provider")
            }

            // Get URL configurations for this provider
            def urlConfigs = configService.getProviderUrls(providerCode)
            if (!urlConfigs) {
                return renderEmptyTemplate("No URLs configured for provider: ${providerCode}")
            }

            // Resolve context and process URLs
            def context = urlTemplateService.resolveContext(instance)
            def buttons = urlTemplateService.processUrlConfigs(urlConfigs, context)

            if (buttons.isEmpty()) {
                return renderEmptyTemplate("No URLs available (missing required configuration)")
            }

            // Create view model
            def model = new ViewModel()
            model.addObject('buttons', buttons)
            model.addObject('providerCode', providerCode)
            model.addObject('instanceId', instance.id)

            log.debug("Rendering ${buttons.size()} buttons for instance ${instance.id}")
            return renderTemplate("hbs/addonUrl", model)

        } catch (Exception e) {
            log.error("Error rendering Add-on URL tab for instance ${instance?.id}", e)
            return renderEmptyTemplate("Error loading add-on URLs")
        }
    }

    /**
     * Render an empty template when no URLs are available.
     * @param message The message to display
     * @return HTMLResponse with empty template
     */
    private HTMLResponse renderEmptyTemplate(String message) {
        def model = new ViewModel()
        model.addObject('message', message)
        return renderTemplate("hbs/addonUrlEmpty", model)
    }
}