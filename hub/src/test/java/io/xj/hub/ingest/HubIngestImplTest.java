// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.ingest;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.typesafe.config.Config;
import io.xj.hub.HubTestConfiguration;
import io.xj.hub.access.HubAccess;
import io.xj.hub.dao.InstrumentDAO;
import io.xj.hub.dao.ProgramDAO;
import io.xj.hub.dao.TemplateBindingDAO;
import io.xj.hub.dao.TemplateDAO;
import io.xj.hub.dao.TemplatePlaybackDAO;
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
  InstrumentDAO instrumentDAO;
  @Mock
  ProgramDAO programDAO;
  @Mock
  TemplateDAO templateDAO;
  @Mock
  TemplateBindingDAO templateBindingDAO;
  @Mock
  TemplatePlaybackDAO templatePlaybackDAO;

  @Test
  public void instantiateWithUUIDs() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var injector = Guice.createInjector(new HubIngestModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(InstrumentDAO.class).toInstance(instrumentDAO);
        bind(ProgramDAO.class).toInstance(programDAO);
        bind(Config.class).toInstance(config);
        bind(TemplateDAO.class).toInstance(templateDAO);
        bind(TemplateBindingDAO.class).toInstance(templateBindingDAO);
        bind(TemplatePlaybackDAO.class).toInstance(templatePlaybackDAO);
      }
    });
    Template template = buildTemplate(buildAccount("Test"), TemplateType.Preview, "Test", "key123");
    when(templateDAO.readOne(any(), eq(template.getId())))
      .thenReturn(template);
    when(templateBindingDAO.readMany(any(), eq(List.of(template.getId()))))
      .thenReturn(List.of());

    HubIngest subject = injector.getInstance(HubIngestFactory.class).ingest(HubAccess.internal(), template.getId());

    assertNotNull(subject);
  }

}
