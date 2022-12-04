// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft;

import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.InstrumentType;
import io.xj.lib.music.Accidental;
import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import io.xj.lib.util.CSV;
import io.xj.lib.util.ValueException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 https://www.pivotaltracker.com/story/show/176696738 XJ has a serviceable voicing algorithm
 */
@RunWith(MockitoJUnitRunner.class)
public class NotePickerTests extends YamlTest {
  private static final int REPEAT_EACH_TEST_TIMES = 7;
  private final TemplateConfig templateConfig;
  private NotePicker subject;

  public NotePickerTests() throws ValueException {
    templateConfig = new TemplateConfig();
  }

  @Test
  public void notePicker4a() {
    loadAndRunTest("arrangement_4_5_notes_one.yaml");
  }

  @Test
  public void notePicker4b() {
    loadAndRunTest("arrangement_4_5_notes_two.yaml");
  }

  @Test
  public void notePicker4c() {
    loadAndRunTest("arrangement_4_5_notes_three.yaml");
  }

  @Test
  public void notePicker6a() {
    loadAndRunTest("arrangement_6_notes.yaml");
  }

  /**
   Load the specified test YAML file and run it repeatedly.

   @param filename of test YAML file
   */
  private void loadAndRunTest(String filename) {
    for (int i = 0; i < REPEAT_EACH_TEST_TIMES; i++)
      try {
        // Load YAML and parse
        var data = loadYaml(filename);

        // Read inputs from the test YAML and instantiate the subject
        loadSubject(data);

        // Execute note picking
        subject.pick();

        // assert final picks
        loadAndPerformAssertions(data);

      } catch (Exception e) {
        failures.add(String.format("[%s] Exception: %s", filename, e.getMessage()));
      }
  }

  /**
   Load the input section of the test YAML file@param data YAML file wrapper
   */
  private void loadSubject(Map<?, ?> data) throws Exception {
    Map<?, ?> obj = (Map<?, ?>) data.get("input");
    if (Objects.isNull(obj)) throw new Exception("Input is required!");

    var range = getOptionalNoteRange(obj);

    var instrumentType =
      InstrumentType.valueOf(Objects.requireNonNull(getStr(obj, "instrumentType")));

    var voicingNotes = CSV.split(Objects.requireNonNull(getStr(obj, "voicingNotes"))).stream()
      .map(Note::of).collect(Collectors.toSet());

    var eventNotes = CSV.split(Objects.requireNonNull(getStr(obj, "eventNotes"))).stream()
      .map(Note::of).collect(Collectors.toSet());

    subject = new NotePicker(range, voicingNotes, eventNotes,
      templateConfig.getInstrumentTypesForInversionSeeking().contains(instrumentType));
  }

  private NoteRange getOptionalNoteRange(Map<?, ?> obj) {
    Map<?, ?> rObj = (Map<?, ?>) obj.get("range");
    if (Objects.isNull(rObj)) return NoteRange.empty();
    return NoteRange.from(getStr(rObj, "from"), getStr(rObj, "to"));
  }

  private void loadAndPerformAssertions(Map<?, ?> data) throws Exception {
    @Nullable
    Map<?, ?> obj = (Map<?, ?>) data.get("assertion");
    if (Objects.isNull(obj)) throw new Exception("Assertion is required!");

    var range = getOptionalNoteRange(obj);

    range.getLow().ifPresent(note ->
      assertSame("Range Low-end", note, subject.getTargetRange().getLow().orElseThrow()));

    range.getHigh().ifPresent(note ->
      assertSame("Range High-end", note, subject.getTargetRange().getHigh().orElseThrow()));

    var picked = subject.getPickedNotes().stream()
      .map(n -> n.toString(Accidental.Sharp))
      .collect(Collectors.toSet());

    Optional.ofNullable(getStr(obj, "picks"))
      .ifPresent(picks -> assertSame("Picks",
        new HashSet<>(CSV.split(picks)),
        picked));

    Optional.ofNullable(getInt(obj, "count"))
      .ifPresent(count -> assertEquals((int) count, picked.size()));
  }

}
