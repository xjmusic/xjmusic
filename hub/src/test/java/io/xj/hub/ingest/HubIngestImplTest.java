// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.ingest;


import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.manager.InstrumentManager;
import io.xj.hub.manager.ProgramManager;
import io.xj.hub.manager.TemplateBindingManager;
import io.xj.hub.manager.TemplateManager;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreImpl;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
public class HubIngestImplTest {
  @Mock
  EntityFactory entityFactory;
  @Mock
  InstrumentManager instrumentManager;
  @Mock
  ProgramManager programManager;
  @Mock
  TemplateManager templateManager;
  @Mock
  TemplateBindingManager templateBindingManager;
  @Mock
  EntityStore entityStore;

  @Test
  public void instantiateWithUUIDs() throws Exception {
    Template template = buildTemplate(buildAccount("Test"), TemplateType.Preview, "Test", "key123");
    when(templateManager.readOne(any(), eq(template.getId())))
      .thenReturn(template);
    when(templateBindingManager.readMany(any(), eq(List.of(template.getId()))))
      .thenReturn(List.of());
    when(entityFactory.createEntityStore()).thenReturn(new EntityStoreImpl());

    JsonProvider jsonProvider = new JsonProviderImpl();
    HubIngest subject = new HubIngestFactoryImpl(
      entityFactory,
      jsonProvider,
      instrumentManager,
      programManager,
      templateManager,
      templateBindingManager
    ).ingest(HubAccess.internal(), template.getId());

    Assertions.assertNotNull(subject);
  }

}
