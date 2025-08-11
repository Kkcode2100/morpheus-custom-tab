package com.morpheusdata.generic

import com.morpheusdata.core.providers.AbstractGenericIntegrationProvider
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.views.HTMLResponse
import com.morpheusdata.views.ViewModel
import groovy.util.logging.Slf4j
import com.morpheusdata.model.AccountIntegration

import com.morpheusdata.model.OptionType
import com.morpheusdata.model.Icon

@Slf4j
class MorpheusGenericIntegrationProvider extends AbstractGenericIntegrationProvider {

	Plugin plugin
	MorpheusContext morpheus

	static final String PROVIDER_CODE = 'morpheus-generic-integration-provider'
	static final String PROVIDER_NAME = 'Morpheus Generic Integration Sample'

	MorpheusGenericIntegrationProvider(Plugin plugin, MorpheusContext context) {
		this.plugin = plugin
		this.morpheus = context
	}

	@Override
	MorpheusContext getMorpheus() {
		return this.morpheus
	}

	@Override
	Plugin getPlugin() {
		return plugin
	}

	@Override
	String getCode() {
		return PROVIDER_CODE
	}

	@Override
	String getName() {
		return PROVIDER_NAME
	}

	@Override
	String getCategory() {
		return AccountIntegration.Category.other
	}

	@Override
	Icon getIcon() {
		return new Icon(path:"hooli.svg", darkPath: "hooli.svg")
	}

	@Override
	void refresh(AccountIntegration accountIntegration) {
		log.info("Integration refresh for ${accountIntegration.name}")
		accountIntegration.serviceVersion = 'Hooli 2.0'
		morpheus.services.accountIntegration.save(accountIntegration)
		sleep(30000) // so we can see the syncing status in UI
	}

	@Override
	List<OptionType> getOptionTypes() {
		OptionType apiUrl = new OptionType(
			name: 'API Endpoint',
			code: 'generic.integration.api',
			fieldName: 'serviceUrl',
			placeHolder: 'https://10.0.0.100:6443',
			defaultValue: 'https://10.0.0.151:31366',
			displayOrder: 0,
			fieldLabel: 'Service URL',
			required: true,
			inputType: OptionType.InputType.TEXT
		)
		OptionType apiUsername = new OptionType(
			name: 'Username',
			code: 'generic.integration.username',
			fieldName: 'serviceUsername',
			placeHolder: 'admin',
			displayOrder: 2,
			fieldLabel: 'Username',
			required: false,
			inputType: OptionType.InputType.TEXT
		)
		OptionType apiPassword = new OptionType(
			name: 'Password',
			code: 'generic.integration.password',
			fieldName: 'servicePassword',
			displayOrder: 3,
			fieldLabel: 'Password',
			required: false,
			inputType: OptionType.InputType.PASSWORD
		)
		return [apiUrl, apiUsername, apiPassword]
	}

	@Override
	HTMLResponse renderTemplate(AccountIntegration integration) {
		ViewModel<AccountIntegration> model = new ViewModel<AccountIntegration>()
		model.object = integration

		getRenderer().renderTemplate("hbs/show", model)
	}
}