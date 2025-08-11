# Morpheus Add-on URL Instance Tab Plugin

A Morpheus UI extension plugin that adds an "Add-on URL" Instance Tab displaying cloud-specific external dashboard links. This plugin automatically detects the cloud provider of an instance (AWS, Azure, GCP) and displays relevant external links such as security dashboards, cost management tools, and monitoring consoles.

## Features

- **Cloud Provider Detection**: Automatically detects AWS (`amazon`), Azure (`azure`), and GCP (`googlecloud`) instances
- **Smart Placeholder Resolution**: Resolves cloud-specific placeholders like `{region}`, `{subscriptionId}`, `{projectId}`, etc.
- **Configurable URLs**: JSON-based configuration with runtime override capability
- **Security Compliant**: Links open in new tabs with proper security attributes
- **Performance Optimized**: Fast tab visibility checks with minimal overhead
- **Fallback Handling**: Graceful degradation when cloud configuration is incomplete

## Compatibility

- **Morpheus Version**: 8.0.0+
- **Plugin API**: 1.2.7
- **Java**: 11/17
- **Gradle**: 7.x

## Installation

1. **Download/Build the Plugin**:
   ```bash
   ./gradlew clean shadowJar
   ```
   This creates `build/libs/morpheus-addon-url-tab-1.0.0-all.jar`

2. **Upload to Morpheus**:
   - Navigate to **Administration → Integrations → Plugins**
   - Click **"Choose File"** and select the JAR file
   - Click **"Upload"**

3. **Verify Installation**:
   - The plugin should appear in the plugins list as "Add-on URL Tab"
   - Status should show as "Loaded"

## Configuration

### Default Configuration

The plugin includes a default configuration supporting common cloud provider dashboards:

**Azure**:
- Azure Security (Defender) Overview
- Azure Advisor Recommendations  
- Azure Resource Graph Explorer
- Azure Cost Management (requires `{subscriptionId}`)

**AWS**:
- AWS Security Hub Dashboard (requires `{region}`)
- AWS CloudWatch Dashboard (requires `{region}`)
- AWS EC2 Console (requires `{region}`)
- AWS Cost Explorer (requires `{region}`)

**GCP**:
- GCP Security Command Center (requires `{projectId}`)
- GCP Cloud Console Dashboard (requires `{projectId}`)
- GCP Compute Engine (requires `{projectId}`)

### Custom Configuration

To override the default configuration:

1. Navigate to **Administration → Integrations → Plugins**
2. Click **"Edit"** on the "Add-on URL Tab" plugin
3. In the **"Cloud URL Mappings (JSON)"** field, paste your custom JSON configuration

#### Configuration Schema

```json
{
  "providers": {
    "azure": [
      {
        "label": "Display Name",
        "url": "https://portal.azure.com/..."
      }
    ],
    "amazon": [
      {
        "label": "AWS Service",
        "url": "https://{region}.console.aws.amazon.com/..."
      }
    ],
    "googlecloud": [
      {
        "label": "GCP Service", 
        "url": "https://console.cloud.google.com/...?project={projectId}"
      }
    ]
  }
}
```

#### Supported Placeholders

**Azure**:
- `{subscriptionId}`: Resolved from cloud configuration, externalId, or linkedAccountId

**AWS**:
- `{region}`: Resolved from instance config, cloudRegion, or plugin settings
- `{accountId}`: Resolved from linkedAccountId, externalId, or instance config

**GCP**:
- `{projectId}`: Resolved from cloud config, instance config, or externalId

## Usage

### Tab Visibility

The "Add-on URL" tab appears on instance detail pages when:

1. The instance has a recognized cloud provider (`azure`, `amazon`, `googlecloud`)
2. The provider is configured in the JSON settings
3. At least one URL can be fully resolved (all required placeholders have values)

### Tab Content

When visible, the tab displays:
- A list of clickable buttons for each configured URL
- Buttons open in new tabs with security attributes (`target="_blank"`, `rel="noopener noreferrer"`)
- Informational text about external link behavior
- Warning messages if URLs cannot be resolved

## Development

### Project Structure

```
morpheus-addon-url-tab/
├── build.gradle                   # Build configuration
├── gradle.properties              # Version properties
├── settings.gradle                # Project settings
├── gradlew, gradlew.bat           # Gradle wrapper
├── src/main/
│   ├── groovy/com/example/addonurl/
│   │   ├── AddonUrlPlugin.groovy              # Main plugin class
│   │   ├── AddonUrlInstanceTabProvider.groovy # Tab provider implementation
│   │   ├── ConfigService.groovy               # JSON configuration manager
│   │   └── UrlTemplateService.groovy          # Placeholder resolution
│   └── resources/
│       ├── renderer/hbs/
│       │   ├── addonUrl.hbs                   # Main tab template
│       │   └── addonUrlEmpty.hbs              # Empty state template
│       ├── config/
│       │   └── addon-urls.json                # Default configuration
│       └── assets/images/
│           └── addon-url.svg                  # Tab icon
└── README.md
```

### Building

```bash
# Clean and build
./gradlew clean shadowJar

# Build only
./gradlew shadowJar

# Continuous build during development
./gradlew build --continuous
```

### Key Implementation Details

1. **AbstractInstanceTabProvider**: Implements Morpheus instance tab pattern
2. **Defensive Resolution**: Multiple fallback strategies for cloud-specific values
3. **Template Processing**: Only displays buttons for URLs that can be fully resolved
4. **Error Handling**: Graceful degradation with informative error messages
5. **Performance**: Minimal blocking operations in `show()` method

## Testing

### Manual Testing

1. **Create Test Instances**:
   - AWS instance in different regions
   - Azure instance with subscription ID configured
   - GCP instance with project ID configured

2. **Verify Tab Behavior**:
   - Tab appears for supported cloud types
   - Tab hidden for unsupported cloud types (VMware, etc.)
   - Buttons work and open correct URLs
   - Placeholder substitution works correctly

3. **Test Configuration Override**:
   - Modify plugin settings with custom JSON
   - Verify new configuration takes effect immediately
   - Test invalid JSON handling (should fall back to defaults)

### Cloud Provider Mapping

| Cloud Type | Morpheus Code | Status |
|------------|---------------|--------|
| AWS        | `amazon`      | ✅ Verified |
| Azure      | `azure`       | ✅ Verified |
| GCP        | `googlecloud` | ✅ Verified |
| VMware     | `vmware`      | ❌ Not supported |
| Standard   | `standard`    | ❌ Not supported |

## Troubleshooting

### Tab Not Appearing

1. **Check Instance Cloud Type**:
   ```groovy
   // In Morpheus logs, look for:
   log.debug("Instance ${instance.id} provider: ${instance.cloud.zoneType.code}")
   ```

2. **Verify Configuration**:
   - Ensure the cloud provider is in your JSON configuration
   - Check that at least one URL can be resolved

3. **Check Plugin Settings**:
   - Invalid JSON in settings will fall back to bundled configuration
   - Check Morpheus logs for JSON parsing errors

### URLs Not Resolving

1. **Azure Subscription ID Missing**:
   - Verify cloud configuration has `subscriptionId`
   - Check `instance.cloud.externalId` or `linkedAccountId`

2. **AWS Region Missing**:
   - Verify instance has region configured
   - Consider setting `defaultAwsRegion` plugin setting

3. **GCP Project ID Missing**:
   - Verify cloud configuration has `projectId`
   - Check `instance.cloud.externalId`

### Logs

Enable debug logging to see detailed resolution information:

```
com.example.addonurl: DEBUG
```

Look for log entries showing:
- Provider detection
- Placeholder resolution
- Tab visibility decisions
- Configuration loading

## License

This plugin is provided as-is for use with HPE Morpheus environments.

## Support

For issues or questions:
1. Check the Morpheus logs for detailed error information
2. Verify cloud configuration completeness
3. Test with default configuration before using custom JSON
4. Consult Morpheus Plugin API documentation for advanced customization