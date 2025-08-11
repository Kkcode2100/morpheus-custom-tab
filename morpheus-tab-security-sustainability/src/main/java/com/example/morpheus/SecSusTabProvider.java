package com.example.morpheus;

import com.morpheusdata.core.AbstractInstanceTabProvider;
import com.morpheusdata.core.MorpheusContext;
import com.morpheusdata.core.Plugin;
import com.morpheusdata.model.Account;
import com.morpheusdata.model.Instance;
import com.morpheusdata.model.User;
import com.morpheusdata.views.HTMLResponse;
import com.morpheusdata.views.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecSusTabProvider extends AbstractInstanceTabProvider {
  private static final Logger log = LoggerFactory.getLogger(SecSusTabProvider.class);

  protected Plugin plugin;
  protected MorpheusContext morpheus;

  protected String code = "addon-url-instance-tab-v3";
  protected String name = "Add-on URL";

  public SecSusTabProvider(Plugin plugin, MorpheusContext context) {
    this.plugin = plugin;
    this.morpheus = context;
  }

  @Override
  public Plugin getPlugin() { return plugin; }

  @Override
  public MorpheusContext getMorpheus() { return morpheus; }

  @Override
  public String getCode() { return code; }

  @Override
  public String getName() { return name; }

  @Override
  public HTMLResponse renderTemplate(Instance instance) {
    Map<String,Object> vm = new HashMap<>();
    
    // Pass instance data to the template for JavaScript access
    vm.put("instance", instance);
    
    // Plugin settings will be automatically available via window.morpheus.pluginSettings
    // No need to manually pass them here as they're handled by the Morpheus framework
    
    ViewModel<Map<String,Object>> model = new ViewModel<>();
    model.object = vm;
    return getRenderer().renderTemplate("hbs/addon-url", model);
  }

  @Override
  public Boolean show(Instance instance, User user, Account account) {
    return true;
  }
}