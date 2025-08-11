(function(){
  function getPluginSettings() {
    try { return (window.morpheus && window.morpheus.pluginSettings) || {}; } catch(e) { return {}; }
  }
  function getInstanceContext() {
    try { return (window.morpheus && window.morpheus.instance) || {}; } catch(e) { return {}; }
  }
  function detectProvider(instance) {
    var cloud = instance && (instance.cloud || instance.site || {});
    var code = cloud.providerCode || cloud.cloudProvider || cloud.code || '';
    code = (code || '').toLowerCase();
    if(code.indexOf('amazon') >= 0 || code.indexOf('aws') >= 0) return 'aws';
    if(code.indexOf('azure') >= 0 || code.indexOf('arm') >= 0) return 'azure';
    if(code.indexOf('google') >= 0 || code.indexOf('gcp') >= 0) return 'gcp';
    return '';
  }
  function read(obj, path) {
    try { return path.split('.').reduce(function(a,k){ return (a||{})[k]; }, obj); } catch(e){ return undefined; }
  }
  function resolveIdentifiers(instance, settings, provider) {
    var tags = instance && (instance.tags || instance.metadata || {});
    var region = instance && (instance.region && instance.region.code) || instance.region || read(instance,'cloud.regionCode') || read(instance,'cloud.region') || '';
    var zone = read(instance,'zone.name') || read(instance,'zone') || '';
    var instanceId = instance && (instance.externalId || instance.id || '');
    var instanceName = instance && (instance.name || '');
    var cloud = instance && instance.cloud || {};

    var data = { provider: provider, region: region, zone: zone, instanceId: instanceId, instanceName: instanceName };

    if(provider === 'aws') {
      data.accountId = tags[settings['keys.aws.accountId'] || 'aws:accountId'] || '';
      data.instanceId = tags[settings['keys.aws.instanceId'] || 'aws:instanceId'] || data.instanceId;
    } else if(provider === 'azure') {
      data.subscriptionId = tags[settings['keys.azure.subscriptionId'] || 'azure:subscriptionId'] || '';
      data.resourceGroup = tags[settings['keys.azure.resourceGroup'] || 'azure:resourceGroup'] || '';
      data.vmName = tags[settings['keys.azure.vmName'] || 'azure:vmName'] || instanceName || '';
      data.resourceId = tags['azure:resourceId'] || '';
    } else if(provider === 'gcp') {
      data.projectId = tags[settings['keys.gcp.projectId'] || 'gcp:projectId'] || '';
      data.billingAccountId = tags[settings['keys.gcp.billingAccountId'] || 'gcp:billingAccountId'] || '';
      data.instanceName = tags[settings['keys.gcp.instanceName'] || 'gcp:instanceName'] || instanceName || '';
      data.orgId = tags[settings['keys.gcp.orgId'] || 'gcp:orgId'] || '';
    }

    return data;
  }
  function interpolate(template, ids) {
    if(!template) return '';
    return template.replace(/\{(.*?)\}/g, function(_, key){ return ids[key] || ''; });
  }
  function requiredOk(provider, ids) {
    if(provider === 'aws') return !!ids.region;
    if(provider === 'azure') return true;
    if(provider === 'gcp') return !!ids.projectId || !!ids.billingAccountId;
    return false;
  }
  function setButton(id, url, enabled, tooltip) {
    var el = document.getElementById(id);
    if(!el) return;
    el.disabled = !enabled;
    if(tooltip) el.setAttribute('title', tooltip); else el.removeAttribute('title');
    el.onclick = null; // Clear any existing handlers
    if(enabled && url) {
      el.addEventListener('click', function(){ window.open(url, '_blank', 'noopener'); });
    }
  }
  function populateInfo(ids) {
    var tbody = document.querySelector('#secSusInfoTable tbody');
    if(!tbody) return;
    
    // Add to existing content rather than replacing it
    var keys = ['provider','region','zone','accountId','instanceId','subscriptionId','resourceGroup','vmName','resourceId','projectId','billingAccountId','instanceName','orgId'];
    keys.forEach(function(k){
      if(ids[k] && ids[k] !== '') {
        var tr = document.createElement('tr');
        var td1 = document.createElement('td'); td1.textContent = k;
        var td2 = document.createElement('td'); td2.textContent = ids[k] || '';
        tr.appendChild(td1); tr.appendChild(td2); tbody.appendChild(tr);
      }
    });
  }

  function init() {
    var settings = getPluginSettings();
    var instance = getInstanceContext();
    var provider = detectProvider(instance);
    var ids = resolveIdentifiers(instance, settings, provider);
    
    // Debug logging to help troubleshoot
    console.log('Security & Sustainability Tab Debug:', {
      provider: provider,
      instance: instance,
      settings: settings,
      resolvedIds: ids
    });

    var awsSecTpl = settings.awsSecurityUrlTemplate || 'https://{region}.console.aws.amazon.com/securityhub/home?region={region}';
    var awsSusTpl = settings.awsSustainabilityUrlTemplate || 'https://console.aws.amazon.com/billing/home#/carbon';
    var azSecTpl = settings.azureSecurityUrlTemplate || 'https://portal.azure.com/#view/Microsoft_Azure_Security/SecurityMenuBlade/~/overview';
    var azSusTpl = settings.azureSustainabilityUrlTemplate || 'https://portal.azure.com/#blade/Microsoft_Azure_Sustainability/EmissionsImpactDashboardBlade/Overview';
    var gcpSecTpl = settings.gcpSecurityUrlTemplate || 'https://console.cloud.google.com/security/command-center/findings?project={projectId}&organizationId={orgId}';
    var gcpSusTpl = settings.gcpSustainabilityUrlTemplate || 'https://console.cloud.google.com/billing/{billingAccountId}/carbonfootprint?project={projectId}';

    var securityUrl = '';
    var sustainUrl = '';
    if(provider === 'aws') { securityUrl = interpolate(awsSecTpl, ids); sustainUrl = interpolate(awsSusTpl, ids); }
    if(provider === 'azure') { securityUrl = interpolate(azSecTpl, ids); sustainUrl = interpolate(azSusTpl, ids); }
    if(provider === 'gcp') { securityUrl = interpolate(gcpSecTpl, ids); sustainUrl = interpolate(gcpSusTpl, ids); }

    var ok = requiredOk(provider, ids);
    setButton('secSusSecurityBtn', securityUrl, ok && !!securityUrl, ok ? '' : 'Missing required identifiers for '+provider);
    setButton('secSusSustainabilityBtn', sustainUrl, ok && !!sustainUrl, ok ? '' : 'Missing required identifiers for '+provider);

    if(settings.showResolvedInfo !== false) populateInfo(ids);
  }

  if(document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init); else init();
})();