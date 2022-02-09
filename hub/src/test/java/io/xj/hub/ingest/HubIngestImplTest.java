// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.ingest;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.*;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.tables.pojos.Template;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HubIngestImplTest {
  @Mock
  InstrumentManager instrumentManager;
  @Mock
  ProgramManager programManager;
  @Mock
  TemplateManager templateManager;
  @Mock
  TemplateBindingManager templateBindingManager;
  @Mock
  TemplatePlaybackManager templatePlaybackManager;

  @Test
  public void instantiateWithUUIDs() throws Exception {
    var injector = Guice.createInjector(new HubIngestModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(InstrumentManager.class).toInstance(instrumentManager);
        bind(ProgramManager.class).toInstance(programManager);
        bind(TemplateManager.class).toInstance(templateManager);
        bind(TemplateBindingManager.class).toInstance(templateBindingManager);
        bind(TemplatePlaybackManager.class).toInstance(templatePlaybackManager);
      }
    });
    Template template = buildTemplate(buildAccount("Test"), TemplateType.Preview, "Test", "key123");
    when(templateManager.readOne(any(), eq(template.getId())))
      .thenReturn(template);
    when(templateBindingManager.readMany(any(), eq(List.of(template.getId()))))
      .thenReturn(List.of());

    HubIngest subject = injector.getInstance(HubIngestFactory.class).ingest(HubAccess.internal(), template.getId());

    assertNotNull(subject);
  }

}
