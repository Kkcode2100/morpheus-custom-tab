package com.example.addonurl

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ConfigService {
	private static final Logger log = LoggerFactory.getLogger(ConfigService.class)
	private final Plugin plugin
	private final MorpheusContext morpheus

	ConfigService(Plugin plugin, MorpheusContext morpheus) {
		this.plugin = plugin
		this.morpheus = morpheus
	}

	Map loadEffectiveConfig() {
		Map cfg = loadFromSettings()
		if(cfg != null) return cfg
		return loadBundledConfig()
	}

	protected Map loadFromSettings() {
		try {
			def settings = morpheus.getSettings(plugin).blockingGet()
			def raw = settings?.configMap?.get('addonUrlJson') as String
			if(raw) {
				def parsed = new JsonSlurper().parseText(raw)
				if(validate(parsed)) return parsed as Map
				log.warn('[addon-url] Invalid JSON schema in addonUrlJson setting; falling back to bundled config')
			}
		} catch(Throwable t) {
			log.warn("[addon-url] Failed reading settings addonUrlJson: ${t.message}", t)
		}
		return null
	}

	protected Map loadBundledConfig() {
		try {
			InputStream is = this.class.classLoader.getResourceAsStream('config/addon-urls.json')
			if(is == null) return [providers: [:]]
			String text = is.getText('UTF-8')
			def parsed = new JsonSlurper().parseText(text)
			if(validate(parsed)) return parsed as Map
		} catch(Throwable t) {
			log.warn("[addon-url] Error loading bundled config: ${t.message}", t)
		}
		return [providers: [:]]
	}

	protected boolean validate(def json) {
		return (json instanceof Map) && (json.providers instanceof Map)
	}
}