package com.example.addonurl

import com.morpheusdata.core.Plugin
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.views.HandlebarsRenderer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AddonUrlPlugin extends Plugin {
	private static final Logger log = LoggerFactory.getLogger(AddonUrlPlugin.class)

	@Override
	String getCode() { 'addon-url-tab' }

	@Override
	String getName() { 'Add-on URL Tab' }

	@Override
	void initialize() {
		MorpheusContext ctx = this.morpheus
		this.setRenderer(new HandlebarsRenderer(this.class.classLoader))
		this.registerProvider(new AddonUrlInstanceTabProvider(this, ctx, new ConfigService(this, ctx), new UrlTemplateService(this, ctx)))
		log.info('AddonUrlPlugin initialized')
	}

	@Override
	void onDestroy() {
		log.info('AddonUrlPlugin destroyed')
	}
}