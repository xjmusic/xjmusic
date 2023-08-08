// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft;

import io.xj.hub.music.Accidental;
import io.xj.hub.music.Note;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.yaml.snakeyaml.Yaml;

import org.jetbrains.annotations.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * XJ has a serviceable voicing algorithm https://www.pivotaltracker.com/story/show/176696738
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class YamlTest {
  protected Set<String> failures;

  @Before
  public void prepareFailureCache() {
    failures = new HashSet<>();
  }

  /**
   * The cache of failures is a Set in order to de-dupe between multiple runs of the same test
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

  protected Map<?, ?> loadYaml(String prefix, String filename) {
    Yaml yaml = new Yaml();
    Map<?, ?> data = yaml.load(getClass()
      .getResourceAsStream(String.format("%s%s", prefix, filename)));
    assertNotNull("Read Test YAML file", data);
    return data;
  }

  protected void assertSame(String description, Integer expected, Integer actual) {
    if (!Objects.equals(expected, actual))
      failures.add(String.format("%s — Expected: %s — Actual: %s", description, expected, actual));
  }

  protected void assertSameNote(String description, Note expected, Note actual) {
    if (!expected.sameAs(actual))
      failures.add(String.format("%s — Expected: %s — Actual: %s", description,
        expected.toString(Accidental.Sharp),
        actual.toString(Accidental.Sharp)
      ));
  }

  protected void assertSameNotes(String description, Set<String> expected, Set<String> actual) {
    List<Note> expectedNotes = expected.stream()
      .map(Note::of)
      .sorted(Note::compareTo).toList();
    List<Note> actualNotes = actual.stream()
      .map(Note::of)
      .sorted(Note::compareTo).toList();

    // iterate through all notes and compare
    for (int i = 0; i < expectedNotes.size(); i++) {
      if (!expectedNotes.get(i).sameAs(actualNotes.get(i))) {
        failures.add(String.format("%s — Expected: %s — Actual: %s", description,
          expectedNotes.stream()
            .map(n -> n.toString(Accidental.Sharp))
            .collect(Collectors.toList()),
          actualNotes.stream()
            .map(n -> n.toString(Accidental.Sharp))
            .collect(Collectors.toList())));
        return;
      }
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
