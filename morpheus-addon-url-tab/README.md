# Add-on URL Tab (Morpheus 8.x / API 1.2.7)

Adds an Instance Tab named "Add-on URL" that renders cloud-specific buttons (AWS/Azure/GCP) linking to external security dashboards. Visibility and button list are driven by a JSON configuration and the instance's cloud provider code.

## Features
- Instance Tab using `AbstractInstanceTabProvider`
- Provider detection via `instance.cloud.zoneType.code` (values: `amazon`, `azure`, `googlecloud`)
- Configurable buttons per provider from JSON (bundled or settings override)
- URL placeholder templating for `{region}`, `{projectId}`, `{subscriptionId}`
- Links open in a new tab with `target="_blank"` and `rel="noopener noreferrer"`

## Build
```bash
./gradlew clean shadowJar
```
Artifact: `build/libs/morpheus-addon-url-tab-1.0.0-all.jar`

## Install
- Morpheus UI → Administration → Integrations → Plugins → Upload the JAR

## Configure
- Edit the plugin settings and (optionally) paste JSON in "Cloud URL Mappings (JSON)" to override defaults.
- Optional: set "Default AWS Region (optional)" if your AWS links require a default region.

### JSON Schema
```json
{
  "providers": {
    "azure": [ { "label": "...", "url": "..." } ],
    "googlecloud": [ { "label": "...", "url": "..." } ],
    "amazon": [ { "label": "...", "url": "..." } ]
  }
}
```
Bundled default lives at `src/main/resources/config/addon-urls.json`.

## Behavior
- Tab is shown only when the instance cloud provider code exists in the config and at least one button can be fully templated.
- Placeholder resolution:
  - Azure `{subscriptionId}`: `cloud.config.subscriptionId` → `cloud.config.subscriberId` → `cloud.externalId` → `cloud.linkedAccountId` → `buildInstanceConfig().config.subscriptionId`
  - AWS `{accountId}`: `cloud.linkedAccountId` → `cloud.externalId` → `buildInstanceConfig().config.accountId`
  - AWS `{region}`: `buildInstanceConfig().config.region|regionCode` → `instance.cloudRegion.regionCode` → plugin setting `defaultAwsRegion`
  - GCP `{projectId}`: `cloud.config.projectId` → `buildInstanceConfig().config.projectId|project` → `cloud.externalId`
- Only links are rendered; no iframes.

## Template Paths
- `renderer/hbs/addonUrl.hbs`
- `renderer/hbs/addonUrlEmpty.hbs`

## Compatibility
- Morpheus Appliance 8.0.0+
- Plugin API 1.2.7
- Gradle 7.x wrapper, Java 11/17