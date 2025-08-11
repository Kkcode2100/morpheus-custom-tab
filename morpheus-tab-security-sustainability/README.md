# Security & Sustainability Tab (Morpheus Plugin)

A production‑ready Morpheus Java plugin that adds an Instance tab named "Security & Sustainability". The tab provides buttons to open cloud‑native Security and Sustainability dashboards for the instance's cloud provider (AWS, Azure, GCP). URLs are configurable via plugin settings and can be overridden per instance using tags/metadata.

## Build

Prereqs: JDK 11

- ./gradlew clean build
- The plugin JAR will be at build/libs/

If behind an HTTP/HTTPS proxy, add to ~/.gradle/gradle.properties:

```
systemProp.http.proxyHost=<host>
systemProp.http.proxyPort=<port>
systemProp.https.proxyHost=<host>
systemProp.https.proxyPort=<port>
systemProp.http.nonProxyHosts=localhost|127.0.0.1|*.local|10.*|192.168.*
```

## Install

- In Morpheus: Administration → Integrations → Plugins → Upload → select the built JAR.

## Configure Settings

- Show icons: showIcons (bool)
- Show resolved info table: showResolvedInfo (bool)
- URL templates:
  - AWS: awsSecurityUrlTemplate, awsSustainabilityUrlTemplate
  - Azure: azureSecurityUrlTemplate, azureSustainabilityUrlTemplate
  - GCP: gcpSecurityUrlTemplate, gcpSustainabilityUrlTemplate
- Tag/metadata key overrides:
  - keys.aws.accountId (default aws:accountId)
  - keys.aws.instanceId (default aws:instanceId)
  - keys.azure.subscriptionId (default azure:subscriptionId)
  - keys.azure.resourceGroup (default azure:resourceGroup)
  - keys.azure.vmName (default azure:vmName)
  - keys.gcp.projectId (default gcp:projectId)
  - keys.gcp.billingAccountId (default gcp:billingAccountId)
  - keys.gcp.instanceName (default gcp:instanceName)

## Usage

- Navigate to an Instance → "Security & Sustainability" tab.
- Buttons are enabled when required identifiers are resolved; otherwise disabled with a tooltip indicating missing data.

## Troubleshooting

- Permissions: user must have INSTANCE_READ to see the tab.
- Provider detection: relies on instance.cloud.providerCode. For custom providers, set correct tags.
- Missing identifiers: ensure the required tags/metadata exist on the instance. Buttons will stay disabled if required placeholders are not available.
- Logs: check Morpheus plugin logs for initialization or rendering errors.