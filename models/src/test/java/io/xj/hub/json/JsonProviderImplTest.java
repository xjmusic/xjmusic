package io.xj.hub.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.xj.hub.util.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonProviderImplTest {

  private JsonProviderImpl subject;

  @BeforeEach
  void setUp() {
    subject = new JsonProviderImpl();
  }

  /**
   JSON deserialization should not fail when encountering attribute in JSON not found in class - for backwards compatibility
   */
  @Test
  void deserialize_doesnt_fail_on_unknown_fields() throws JsonProcessingException {
    String json = "{\"name\": \"testing1\", \"foo\": \"bar\"}";

    Widget result = subject.getMapper().readValue(json, Widget.class);

    assertEquals("testing1", result.getName());
  }
}
