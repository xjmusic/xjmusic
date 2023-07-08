package io.xj.hub.ingest;

import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.InstrumentManager;
import io.xj.hub.manager.ProgramManager;
import io.xj.hub.manager.TemplateBindingManager;
import io.xj.hub.manager.TemplateManager;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.json.JsonProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HubIngestFactoryImpl implements HubIngestFactory {
  final InstrumentManager instrumentManager;
  final JsonProvider jsonProvider;
  final ProgramManager programManager;
  final TemplateManager templateManager;
  final TemplateBindingManager templateBindingManager;
  final EntityFactory entityFactory;

  @Autowired
  public HubIngestFactoryImpl(
    EntityFactory entityFactory,
    JsonProvider jsonProvider,
    InstrumentManager instrumentManager,
    ProgramManager programManager,
    TemplateManager templateManager,
    TemplateBindingManager templateBindingManager
  ) {
    this.entityFactory = entityFactory;
    this.instrumentManager = instrumentManager;
    this.jsonProvider = jsonProvider;
    this.programManager = programManager;
    this.templateManager = templateManager;
    this.templateBindingManager = templateBindingManager;
  }

  @Override
  public HubIngest ingest(HubAccess access, UUID templateId) throws HubIngestException {
    return new HubIngestImpl(entityFactory, jsonProvider, access, templateId, instrumentManager, programManager, templateManager, templateBindingManager);
  }
}
