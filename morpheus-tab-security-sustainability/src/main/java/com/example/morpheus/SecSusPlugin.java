package com.example.morpheus;

import com.morpheusdata.core.Plugin;
import com.morpheusdata.core.MorpheusContext;
import com.morpheusdata.views.HandlebarsRenderer;
import com.morpheusdata.model.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class SecSusPlugin extends Plugin {
  private static final Logger log = LoggerFactory.getLogger(SecSusPlugin.class);

  @Override
  public String getCode() { return "addon-url-plugin-v3"; }

  @Override
  public String getName() { return "Add-on URL"; }

  @Override
  public void initialize(MorpheusContext morpheus) {
    this.morpheus = morpheus;
    this.setRenderer(new HandlebarsRenderer(SecSusPlugin.class.getClassLoader()));
    SecSusTabProvider tab = new SecSusTabProvider(this, morpheus);
    this.pluginProviders.put(tab.getCode(), tab);
    log.info("Registered provider code={} (INSTANCE_TAB)", tab.getCode());
  }

  @Override
  public void onDestroy() {
    log.info("SecSusPlugin destroyed");
  }

  @Override
  public List<Permission> getPermissions() {
    Permission permission = new Permission("Add-on URL", Collections.singletonList(Permission.AccessType.full));
    return Collections.singletonList(permission);
  }
}