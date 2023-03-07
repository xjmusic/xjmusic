package io.xj.hub.ingest;

import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.InstrumentManager;
import io.xj.hub.manager.ProgramManager;
import io.xj.hub.manager.TemplateBindingManager;
import io.xj.hub.manager.TemplateManager;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.json.JsonProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HubIngestFactoryImpl implements HubIngestFactory {
  private final EntityStore entityStore;
  private final InstrumentManager instrumentManager;
  private final JsonProvider jsonProvider;
  private final ProgramManager programManager;
  private final TemplateManager templateManager;
  private final TemplateBindingManager templateBindingManager;

  @Autowired
  public HubIngestFactoryImpl(
    JsonProvider jsonProvider, EntityStore entityStore,
    InstrumentManager instrumentManager,
    ProgramManager programManager,
    TemplateManager templateManager,
    TemplateBindingManager templateBindingManager
  ) {
    this.entityStore = entityStore;
    this.instrumentManager = instrumentManager;
    this.jsonProvider = jsonProvider;
    this.programManager = programManager;
    this.templateManager = templateManager;
    this.templateBindingManager = templateBindingManager;
  }

  @Override
  public HubIngest ingest(HubAccess access, UUID templateId) throws HubIngestException {
    return new HubIngestImpl(access, templateId, entityStore, instrumentManager, jsonProvider, programManager, templateManager, templateBindingManager);
  }
}
