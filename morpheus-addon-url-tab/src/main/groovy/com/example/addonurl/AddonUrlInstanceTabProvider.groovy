package com.example.addonurl

import com.morpheusdata.core.AbstractInstanceTabProvider
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.model.Account
import com.morpheusdata.model.Instance
import com.morpheusdata.model.User
import com.morpheusdata.views.HTMLResponse
import com.morpheusdata.views.ViewModel
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AddonUrlInstanceTabProvider extends AbstractInstanceTabProvider {
	private static final Logger log = LoggerFactory.getLogger(AddonUrlInstanceTabProvider.class)

	protected final Plugin plugin
	protected final MorpheusContext morpheus
	protected final ConfigService configService
	protected final UrlTemplateService urlTemplateService

	AddonUrlInstanceTabProvider(Plugin plugin, MorpheusContext morpheus, ConfigService configService, UrlTemplateService urlTemplateService) {
		this.plugin = plugin
		this.morpheus = morpheus
		this.configService = configService
		this.urlTemplateService = urlTemplateService
	}

	@Override
	Plugin getPlugin() { plugin }

	@Override
	MorpheusContext getMorpheus() { morpheus }

	@Override
	String getCode() { 'addon-url-instance-tab' }

	@Override
	String getName() { 'Add-on URL' }

	@Override
	Boolean show(Instance instance, User user, Account account) {
		try {
			String providerCode = instance?.cloud?.zoneType?.code
			Long instanceId = instance?.id
			Long cloudId = instance?.cloud?.id
			log.debug("[addon-url] show() for instance={}, cloud={}, providerCode={}", instanceId, cloudId, providerCode)

			def config = configService.loadEffectiveConfig()
			if(!providerCode || !(config?.providers instanceof Map)) return false
			def buttons = config.providers[providerCode]
			if(!(buttons instanceof List) || buttons.isEmpty()) return false

			Map<String,String> ctx = urlTemplateService.resolveContext(instance)
			List<Map> usable = []
			buttons.each { Map btn ->
				String url = btn?.url as String
				String label = btn?.label as String
				if(url && label) {
					String applied = urlTemplateService.applyTemplate(url, ctx)
					if(applied != null) usable << [label: label, url: applied]
				}
			}
			return !usable.isEmpty()
		} catch(Throwable t) {
			log.warn("[addon-url] show() error: ${t.message}", t)
			return false
		}
	}

	@Override
	HTMLResponse renderTemplate(Instance instance) {
		def config = configService.loadEffectiveConfig()
		String providerCode = instance?.cloud?.zoneType?.code
		Map<String,String> ctx = urlTemplateService.resolveContext(instance)
		List<Map> buttons = []
		if(providerCode && (config?.providers instanceof Map)) {
			List<Map> defs = (config.providers[providerCode] as List<Map>) ?: []
			defs?.each { Map btn ->
				String url = btn?.url as String
				String label = btn?.label as String
				if(url && label) {
					String applied = urlTemplateService.applyTemplate(url, ctx)
					if(applied != null) buttons << [label: label, url: applied]
				}
			}
		}

		ViewModel<Map> model = new ViewModel<>()
		model.object = [buttons: buttons]
		if(buttons.isEmpty()) {
			return getRenderer().renderTemplate('hbs/addonUrlEmpty', model)
		}
		return getRenderer().renderTemplate('hbs/addonUrl', model)
	}
}