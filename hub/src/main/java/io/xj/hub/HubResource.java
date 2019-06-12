package io.xj.hub;

import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.app.ApiResource;
import io.xj.craft.CraftModule;

public class HubResource extends ApiResource {
  public HubResource() {
    super();
    setInjector(Guice.createInjector(new CoreModule(), new CraftModule()));
  }
}
