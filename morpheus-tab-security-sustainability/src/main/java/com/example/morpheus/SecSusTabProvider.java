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

public class SecSusTabProvider extends AbstractInstanceTabProvider {
  private static final Logger log = LoggerFactory.getLogger(SecSusTabProvider.class);

  protected Plugin plugin;
  protected MorpheusContext morpheus;

  protected String code = "sec-sus-tab";
  protected String name = "Security & Sustainability";

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
    try {
      ViewModel<Instance> model = new ViewModel<>();
      model.object = instance;
      return getRenderer().renderTemplate("views/sec-sus.html", model);
    } catch (Exception ex) {
      log.warn("Failed to render sec-sus template, falling back: {}", ex.getMessage());
      return HTMLResponse.success("<div class=\"sec-sus-tab\"><h2>" + name + "</h2></div>");
    }
  }

  @Override
  public Boolean show(Instance instance, User user, Account account) {
    return true;
  }
}