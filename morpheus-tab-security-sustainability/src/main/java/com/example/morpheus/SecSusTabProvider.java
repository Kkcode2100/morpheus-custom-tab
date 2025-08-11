package com.example.morpheus;

import com.morpheusdata.core.MorpheusContext;
import com.morpheusdata.core.Plugin;
import com.morpheusdata.core.providers.InstanceTabProvider;
import com.morpheusdata.model.Account;
import com.morpheusdata.model.Instance;
import com.morpheusdata.model.Permission;
import com.morpheusdata.model.User;
import com.morpheusdata.views.HTMLResponse;
import com.morpheusdata.views.TabSection;
import com.morpheusdata.views.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class SecSusTabProvider implements InstanceTabProvider {
  private static final Logger log = LoggerFactory.getLogger(SecSusTabProvider.class);

  protected Plugin plugin;
  protected MorpheusContext morpheus;

  public SecSusTabProvider(Plugin plugin, MorpheusContext context) {
    this.plugin = plugin;
    this.morpheus = context;
  }

  @Override
  public String getCode() { return "sec-sus-tab"; }

  @Override
  public String getName() { return "Security & Sustainability"; }

  @Override
  public String getDescription() { return "Provides security and sustainability dashboards for cloud instances."; }

  @Override
  public TabSection getTabSection() { return TabSection.INSTANCE; }

  @Override
  public List<Permission> getRequiredPermissions() { return Arrays.asList(Permission.INSTANCE_READ); }

  @Override
  public HTMLResponse renderTemplate(Instance instance) {
    ViewModel<Instance> model = new ViewModel<>();
    model.object = instance;
    return plugin.getRenderer().renderTemplate("views/sec-sus.html", model);
  }

  @Override
  public Boolean show(Instance instance, User user, Account account) { return true; }
}