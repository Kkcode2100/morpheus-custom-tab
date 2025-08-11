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
  public String getCode() { return "sec-sus-tab"; }

  @Override
  public String getName() { return "Security & Sustainability Tab"; }

  @Override
  public void initialize() {
    MorpheusContext morpheus = this.morpheus;
    SecSusTabProvider tabProvider = new SecSusTabProvider(this, morpheus);
    this.registerProvider(tabProvider);
    this.setRenderer(new HandlebarsRenderer(this.getClass().getClassLoader()));
    log.info("SecSusPlugin initialized");
  }

  @Override
  public void onDestroy() {
    log.info("SecSusPlugin destroyed");
  }

  @Override
  public List<Permission> getPermissions() {
    Permission permission = new Permission("Security & Sustainability Tab", "secSusTab", Collections.singletonList(Permission.AccessType.full));
    return Collections.singletonList(permission);
  }
}