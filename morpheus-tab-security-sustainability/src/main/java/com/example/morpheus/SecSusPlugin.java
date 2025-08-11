package com.example.morpheus;

import com.morpheusdata.core.Plugin;
import com.morpheusdata.core.MorpheusContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecSusPlugin extends Plugin {
  private static final Logger log = LoggerFactory.getLogger(SecSusPlugin.class);

  @Override
  public String getCode() { return "sec-sus-tab"; }

  @Override
  public String getName() { return "Security & Sustainability Tab"; }

  @Override
  public void initialize() {
    MorpheusContext morpheus = this.morpheus;
    this.registerProvider(new SecSusTabProvider(this, morpheus));
    log.info("SecSusPlugin initialized");
  }
}