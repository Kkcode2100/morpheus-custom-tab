package com.example.addonurl

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.model.Instance
// import groovy.transform.CompileStatic // Removed to fix compilation issues
import groovy.util.logging.Slf4j

/**
 * Service for resolving cloud-specific placeholders in URLs.
 * Handles different cloud providers and their specific context requirements.
 */
@Slf4j
// @CompileStatic // Removed to fix compilation issues
class UrlTemplateService {

    private final Plugin plugin
    private final MorpheusContext morpheus

    UrlTemplateService(Plugin plugin, MorpheusContext morpheus) {
        this.plugin = plugin
        this.morpheus = morpheus
    }

    /**
     * Resolve placeholders for a given instance based on its cloud provider.
     * @param instance The Morpheus instance
     * @return Map of placeholder keys to resolved values
     */
    Map<String, String> resolveContext(Instance instance) {
        def context = [:]
        
        try {
            if (!instance?.cloud?.zoneType?.code) {
                log.warn("Instance ${instance?.id} has no cloud or zone type code")
                return context
            }

            def providerCode = instance.cloud.zoneType.code
            log.debug("Resolving context for instance ${instance.id}, cloud ${instance.cloud.id}, provider: ${providerCode}")

            // Get enriched instance config
            def enrichedConfig = getEnrichedInstanceConfig(instance)

            switch (providerCode) {
                case 'azure':
                    context.putAll(resolveAzureContext(instance, enrichedConfig))
                    break
                case 'amazon':
                    context.putAll(resolveAwsContext(instance, enrichedConfig))
                    break
                case 'googlecloud':
                    context.putAll(resolveGcpContext(instance, enrichedConfig))
                    break
                default:
                    log.debug("No specific context resolver for provider: ${providerCode}")
            }

            log.debug("Resolved context for instance ${instance.id}: ${context}")
            return context

        } catch (Exception e) {
            log.error("Error resolving context for instance ${instance?.id}", e)
            return [:]
        }
    }

    /**
     * Apply template substitution to a URL with the given context.
     * @param url The URL template with placeholders
     * @param context Map of placeholder keys to values
     * @return The URL with placeholders replaced, or null if required placeholders are missing
     */
    String applyTemplate(String url, Map<String, String> context) {
        if (!url) {
            return null
        }

        def result = url
        def missingKeys = []

        // Find all placeholders in the URL
        def placeholders = url.findAll(/\{(\w+)\}/) { match, key ->
            if (context.containsKey(key)) {
                result = result.replace("{${key}}", context[key])
            } else {
                missingKeys << key
            }
        }

        if (missingKeys) {
            log.debug("Missing required placeholder values for URL template: ${missingKeys}")
            return null
        }

        return result
    }

    /**
     * Get enriched instance configuration from Morpheus context.
     * @param instance The instance
     * @return Map containing enriched configuration
     */
    private Map<String, Object> getEnrichedInstanceConfig(Instance instance) {
        try {
            def enrichedContext = morpheus.buildInstanceConfig(instance, [:], null, [], [:]).blockingGet()
            return enrichedContext?.config ?: [:]
        } catch (Exception e) {
            log.debug("Could not get enriched instance config for instance ${instance.id}", e)
            return [:]
        }
    }

    /**
     * Resolve Azure-specific context placeholders.
     * @param instance The instance
     * @param enrichedConfig Enriched instance configuration
     * @return Map of resolved Azure context
     */
    private Map<String, String> resolveAzureContext(Instance instance, Map<String, Object> enrichedConfig) {
        def context = [:]

        // Resolve subscriptionId with multiple fallback strategies
        def subscriptionId = resolveAzureSubscriptionId(instance, enrichedConfig)
        if (subscriptionId) {
            context.subscriptionId = subscriptionId
        }

        return context
    }

    /**
     * Resolve Azure subscription ID from various sources.
     * @param instance The instance
     * @param enrichedConfig Enriched configuration
     * @return The subscription ID or null if not found
     */
    private String resolveAzureSubscriptionId(Instance instance, Map<String, Object> enrichedConfig) {
        // Try multiple sources in order of preference
        def sources = [
            { instance.cloud?.config?.subscriptionId?.toString() },
            { instance.cloud?.config?.subscriberId?.toString() }, // Typo observed in wild
            { instance.cloud?.externalId?.toString() },
            { instance.cloud?.linkedAccountId?.toString() },
            { enrichedConfig?.subscriptionId?.toString() }
        ]

        for (source in sources) {
            try {
                def value = source()
                if (value && value.trim()) {
                    log.debug("Found Azure subscriptionId: ${value}")
                    return value.trim()
                }
            } catch (Exception e) {
                // Continue to next source
            }
        }

        log.debug("Could not resolve Azure subscriptionId for instance ${instance.id}")
        return null
    }

    /**
     * Resolve AWS-specific context placeholders.
     * @param instance The instance
     * @param enrichedConfig Enriched instance configuration
     * @return Map of resolved AWS context
     */
    private Map<String, String> resolveAwsContext(Instance instance, Map<String, Object> enrichedConfig) {
        def context = [:]

        // Resolve accountId
        def accountId = resolveAwsAccountId(instance, enrichedConfig)
        if (accountId) {
            context.accountId = accountId
        }

        // Resolve region
        def region = resolveAwsRegion(instance, enrichedConfig)
        if (region) {
            context.region = region
        }

        return context
    }

    /**
     * Resolve AWS account ID from various sources.
     * @param instance The instance
     * @param enrichedConfig Enriched configuration
     * @return The account ID or null if not found
     */
    private String resolveAwsAccountId(Instance instance, Map<String, Object> enrichedConfig) {
        def sources = [
            { instance.cloud?.linkedAccountId?.toString() },
            { instance.cloud?.externalId?.toString() },
            { enrichedConfig?.accountId?.toString() }
        ]

        for (source in sources) {
            try {
                def value = source()
                if (value && value.trim()) {
                    log.debug("Found AWS accountId: ${value}")
                    return value.trim()
                }
            } catch (Exception e) {
                // Continue to next source
            }
        }

        log.debug("Could not resolve AWS accountId for instance ${instance.id}")
        return null
    }

    /**
     * Resolve AWS region from various sources.
     * @param instance The instance
     * @param enrichedConfig Enriched configuration
     * @return The region or null if not found
     */
    private String resolveAwsRegion(Instance instance, Map<String, Object> enrichedConfig) {
        def sources = [
            { enrichedConfig?.region?.toString() },
            { enrichedConfig?.regionCode?.toString() },
            { instance.cloudRegion?.regionCode?.toString() },
            { getDefaultAwsRegion() }
        ]

        for (source in sources) {
            try {
                def value = source()
                if (value && value.trim()) {
                    log.debug("Found AWS region: ${value}")
                    return value.trim()
                }
            } catch (Exception e) {
                // Continue to next source
            }
        }

        log.debug("Could not resolve AWS region for instance ${instance.id}")
        return null
    }

    /**
     * Get the default AWS region from plugin settings.
     * @return Default AWS region or null
     */
    private String getDefaultAwsRegion() {
        try {
            def settings = morpheus.getSettings(plugin).blockingGet()
            return settings?.defaultAwsRegion?.toString()?.trim()
        } catch (Exception e) {
            log.debug("Could not get default AWS region setting", e)
            return null
        }
    }

    /**
     * Resolve GCP-specific context placeholders.
     * @param instance The instance
     * @param enrichedConfig Enriched instance configuration
     * @return Map of resolved GCP context
     */
    private Map<String, String> resolveGcpContext(Instance instance, Map<String, Object> enrichedConfig) {
        def context = [:]

        // Resolve projectId
        def projectId = resolveGcpProjectId(instance, enrichedConfig)
        if (projectId) {
            context.projectId = projectId
        }

        return context
    }

    /**
     * Resolve GCP project ID from various sources.
     * @param instance The instance
     * @param enrichedConfig Enriched configuration
     * @return The project ID or null if not found
     */
    private String resolveGcpProjectId(Instance instance, Map<String, Object> enrichedConfig) {
        def sources = [
            { instance.cloud?.config?.projectId?.toString() },
            { enrichedConfig?.projectId?.toString() },
            { enrichedConfig?.project?.toString() },
            { instance.cloud?.externalId?.toString() }
        ]

        for (source in sources) {
            try {
                def value = source()
                if (value && value.trim()) {
                    log.debug("Found GCP projectId: ${value}")
                    return value.trim()
                }
            } catch (Exception e) {
                // Continue to next source
            }
        }

        log.debug("Could not resolve GCP projectId for instance ${instance.id}")
        return null
    }

    /**
     * Process a list of URL configurations and return only those that can be fully templated.
     * @param urlConfigs List of URL configuration maps
     * @param context The resolved context for placeholder substitution
     * @return List of processed button configurations with resolved URLs
     */
    List<Map<String, String>> processUrlConfigs(List<Map<String, String>> urlConfigs, Map<String, String> context) {
        def processedButtons = []

        for (urlConfig in urlConfigs) {
            def url = applyTemplate(urlConfig.url as String, context)
            if (url) {
                processedButtons << [
                    label: urlConfig.label as String,
                    url: url
                ]
            } else {
                log.debug("Skipping button '${urlConfig.label}' due to missing placeholder values")
            }
        }

        return processedButtons
    }
}