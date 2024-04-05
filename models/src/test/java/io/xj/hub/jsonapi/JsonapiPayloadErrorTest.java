// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.jsonapi;

import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.util.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonapiPayloadErrorTest {
  JsonapiPayloadFactory jsonapiPayloadFactory;
  PayloadError subject;

  @BeforeEach
  public void setUp() {
    var jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    entityFactory.register(Widget.class);
    subject = jsonapiPayloadFactory.newPayloadError();
  }

  @Test
  public void of() {
    subject = PayloadError.of(new Exception("Quarantine!"));

    assertEquals("Quarantine!", subject.getTitle());
  }

  @Test
  public void get_set_links() {
    assertEquals(Map.of("about", "https://about.com/"), subject.setAboutLink("https://about.com/").getLinks());
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
