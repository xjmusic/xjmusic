// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft;

import io.xj.hub.music.Accidental;
import io.xj.hub.music.Note;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 XJ has a serviceable voicing algorithm https://www.pivotaltracker.com/story/show/176696738
 */
@ExtendWith(MockitoExtension.class)
public abstract class YamlTest {
  protected Set<String> failures;

  @BeforeEach
  public void prepareFailureCache() {
    failures = new HashSet<>();
  }

  /**
   The cache of failures is a Set in order to de-dupe between multiple runs of the same test
   */
  @AfterEach
  public void reportFailures() {
    if (failures.isEmpty()) {
      System.out.println("\nAll assertions OK");
      return;
    }
    System.out.println("\nFAILED");
    failures.stream().sorted().forEach(System.out::println);
    assertEquals(0, failures.size());
  }

  protected Map<?, ?> loadYaml(String prefix, String filename) {
    Yaml yaml = new Yaml();
    Map<?, ?> data = yaml.load(getClass()
      .getResourceAsStream(String.format("%s%s", prefix, filename)));
    assertNotNull(data);
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
  protected Double getDouble(Map<?, ?> obj) {
    if (Objects.isNull(obj.get("position"))) return null;
    return Double.valueOf(String.valueOf(obj.get("position")));
  }

  @Nullable
  protected Boolean getBool(Map<?, ?> obj, String key) {
    if (Objects.isNull(obj.get(key))) return null;
    return Boolean.parseBoolean(String.valueOf(obj.get(key)));
  }
}
