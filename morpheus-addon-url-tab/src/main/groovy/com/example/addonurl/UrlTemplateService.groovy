package com.example.addonurl

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.model.Instance
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UrlTemplateService {
	private static final Logger log = LoggerFactory.getLogger(UrlTemplateService.class)
	private final Plugin plugin
	private final MorpheusContext morpheus

	UrlTemplateService(Plugin plugin, MorpheusContext morpheus) {
		this.plugin = plugin
		this.morpheus = morpheus
	}

	Map<String,String> resolveContext(Instance instance) {
		Map<String,String> ctx = [:]
		try {
			def cloud = instance?.cloud
			def providerCode = cloud?.zoneType?.code
			ctx.provider = providerCode

			def cfg = morpheus.buildInstanceConfig(instance, [:], null, [], [:]).blockingGet()
			def ic = cfg?.config instanceof Map ? (Map)cfg.config : [:]

			if(providerCode == 'azure') {
				String subscriptionId = (cloud?.config?.subscriptionId ?: cloud?.config?.subscriberId ?: cloud?.externalId ?: cloud?.linkedAccountId ?: ic?.subscriptionId) as String
				if(subscriptionId) ctx.subscriptionId = subscriptionId
			}
			if(providerCode == 'amazon') {
				String accountId = (cloud?.linkedAccountId ?: cloud?.externalId ?: ic?.accountId) as String
				String region = (ic?.region ?: ic?.regionCode ?: instance?.cloudRegion?.regionCode ?: defaultAwsRegionSetting()) as String
				if(accountId) ctx.accountId = accountId
				if(region) ctx.region = region
			}
			if(providerCode == 'googlecloud') {
				String projectId = (cloud?.config?.projectId ?: ic?.projectId ?: ic?.project ?: cloud?.externalId) as String
				if(projectId) ctx.projectId = projectId
			}
		} catch(Throwable t) {
			log.warn("[addon-url] resolveContext error: ${t.message}", t)
		}
		ctx.findAll { k,v -> v != null }
	}

	String applyTemplate(String url, Map<String,String> ctx) {
		Set<String> tokens = (url =~ /\{([a-zA-Z0-9_]+)\}/).collect { it[1] } as Set<String>
		for(String token : tokens) {
			String value = ctx[token]
			if(!value) {
				log.debug("[addon-url] Skipping button; missing token {} for url {}", token, url)
				return null
			}
			url = url.replaceAll('\\{' + token + '\\}', java.util.regex.Matcher.quoteReplacement(value))
		}
		return url
	}

	protected String defaultAwsRegionSetting() {
		try {
			def settings = morpheus.getSettings(plugin).blockingGet()
			return settings?.configMap?.get('defaultAwsRegion') as String
		} catch(Throwable t) {
			return null
		}
	}
}