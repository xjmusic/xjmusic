// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.Program;
import io.xj.lib.entity.EntityFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonapiPayloadErrorTest {
  JsonapiPayloadFactory jsonapiPayloadFactory;
  PayloadError subject;

  @Before
  public void setUp() {
    var injector = Guice.createInjector(new JsonApiModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(ConfigFactory.empty());
      }
    });
    jsonapiPayloadFactory = injector.getInstance(JsonapiPayloadFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    entityFactory.register(Program.class);
    subject = jsonapiPayloadFactory.newPayloadError();
  }

  @Test
  public void of() {
    subject = PayloadError.of(new Exception("Quarantine!"));

    assertEquals("Quarantine!", subject.getTitle());
  }

  @Test
  public void get_set_links() {
    assertEquals(ImmutableMap.of("about", "https://about.com/"), subject.setAboutLink("https://about.com/").getLinks());
  }

  @Test
  public void get_set_code() {
    assertEquals("2020", subject.setCode("2020").getCode());
  }

  @Test
  public void get_set_detail() {
    assertEquals("Shindig", subject.setDetail("Shindig").getDetail());
  }

  @Test
  public void get_set_id() {
    assertEquals("ErrorParty", subject.setId("ErrorParty").getId());
  }

  @Test
  public void get_set_title() {
    assertEquals("Here be errors", subject.setTitle("Here be errors").getTitle());
  }

}
