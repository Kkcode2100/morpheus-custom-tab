package com.example.addonurl

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.model.OptionType

/**
 * Main plugin class for the Add-on URL Instance Tab plugin.
 * This plugin adds a new instance tab that displays cloud-specific external dashboard links.
 */
class AddonUrlPlugin extends Plugin {

    @Override
    String getCode() {
        return 'addon-url-tab'
    }

    @Override
    void initialize() {
        // Register the instance tab provider
        this.pluginProviders << new AddonUrlInstanceTabProvider(this, morpheus)
        
        // Register plugin settings
        this.setSettings([
            new OptionType([
                code: 'addonUrlJson',
                name: 'Cloud URL Mappings (JSON)',
                category: 'addon-url-tab',
                fieldName: 'addonUrlJson',
                displayOrder: 0,
                fieldLabel: 'Cloud URL Mappings (JSON)',
                helpText: 'JSON configuration for cloud provider URL mappings. Leave empty to use defaults.',
                required: false,
                inputType: OptionType.InputType.TEXTAREA,
                fieldContext: 'config'
            ])
        ])
    }

    @Override
    void onDestroy() {
        // Plugin cleanup if needed
    }
}