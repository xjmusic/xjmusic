// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.arrangement;

import com.google.common.collect.Sets;
import io.xj.lib.music.AdjSymbol;
import io.xj.lib.music.Note;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 [#176696738] XJ has a serviceable voicing algorithm
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class YamlTest {
  private static final String TEST_PATH_PREFIX = "/arrangements/";
  protected Set<String> failures;

  @Before
  public void prepareFailureCache() {
    failures = Sets.newHashSet();
  }

  /**
   The cache of failures is a Set in order to de-dupe between multiple runs of the same test
   */
  @After
  public void reportFailures() {
    if (0 == failures.size()) {
      System.out.println("\nAll assertions OK");
      return;
    }
    System.out.println("\nFAILED");
    failures.stream().sorted().forEach(System.out::println);
    assertEquals("There should be zero assertion failures", 0, failures.size());
  }

  protected Map<?, ?> loadYaml(String filename) {
    Yaml yaml = new Yaml();
    Map<?, ?> data = (Map<?, ?>) yaml.load(getClass()
      .getResourceAsStream(String.format("%s%s", TEST_PATH_PREFIX, filename)));
    assertNotNull("Read Test YAML file", data);
    return data;
  }

  protected void assertSame(String description, Integer expected, Integer actual) {
    if (!Objects.equals(expected, actual))
      failures.add(String.format("%s — Expected: %s — Actual: %s", description, expected, actual));
  }

  protected void assertSame(String description, Note expected, Note actual) {
    if (!expected.sameAs(actual))
      failures.add(String.format("%s — Expected: %s — Actual: %s", description,
        expected.toString(AdjSymbol.Sharp),
        actual.toString(AdjSymbol.Sharp)
      ));
  }

  protected void assertSame(String description, Set<String> expected, Set<String> actual) {
    if (!Objects.equals(expected, actual)) {
      failures.add(String.format("%s — Expected: %s — Actual: %s", description,
        expected.stream()
          .map(Note::of)
          .sorted(Note::compareTo)
          .map(n -> n.toString(AdjSymbol.Sharp))
          .collect(Collectors.toList()),
        actual.stream()
          .map(Note::of)
          .sorted(Note::compareTo)
          .map(n -> n.toString(AdjSymbol.Sharp))
          .collect(Collectors.toList())));
    }
  }

  @Nullable
  protected String getStr(Map<?, ?> obj, String key) {
    if (Objects.isNull(obj.get(key))) return null;
    return String.valueOf(obj.get(key));
  }

  @Nullable
  protected Integer getInt(Map<?, ?> obj, String key) {
    if (Objects.isNull(obj.get(key))) return null;
    return Integer.valueOf(String.valueOf(obj.get(key)));
  }

  @Nullable
  protected Float getFloat(Map<?, ?> obj, String key) {
    if (Objects.isNull(obj.get(key))) return null;
    return Float.valueOf(String.valueOf(obj.get(key)));
  }

  @Nullable
  protected Double getDouble(Map<?, ?> obj, String key) {
    if (Objects.isNull(obj.get(key))) return null;
    return Double.valueOf(String.valueOf(obj.get(key)));
  }

  @Nullable
  protected Boolean getBool(Map<?, ?> obj, String key) {
    if (Objects.isNull(obj.get(key))) return null;
    return Boolean.parseBoolean(String.valueOf(obj.get(key)));
  }
}
