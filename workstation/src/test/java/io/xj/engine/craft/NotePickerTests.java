// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft;

import io.xj.model.TemplateConfig;
import io.xj.model.enums.InstrumentType;
import io.xj.model.music.Accidental;
import io.xj.model.music.Note;
import io.xj.model.music.NoteRange;
import io.xj.model.util.CsvUtils;
import io.xj.model.util.ValueException;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 XJ has a serviceable voicing algorithm https://github.com/xjmusic/xjmusic/issues/221
 */
@ExtendWith(MockitoExtension.class)
public class NotePickerTests extends YamlTest {
  static final int REPEAT_EACH_TEST_TIMES = 7;
  static final String TEST_PATH_PREFIX = "/picking/";
  final TemplateConfig templateConfig;
  NotePicker subject;

  public NotePickerTests() throws ValueException {
    templateConfig = new TemplateConfig();
  }

  @Test
  public void picking_4_5_notes_one() {
    loadAndRunTest("picking_4_5_notes_one.yaml");
  }

  @Test
  public void picking_4_5_notes_two() {
    loadAndRunTest("picking_4_5_notes_two.yaml");
  }

  @Test
  public void picking_4_5_notes_three() {
    loadAndRunTest("picking_4_5_notes_three.yaml");
  }

  @Test
  public void picking_6_notes() {
    loadAndRunTest("picking_6_notes.yaml");
  }

  /**
   Load the specified test YAML file and run it repeatedly.

   @param filename of test YAML file
   */
  void loadAndRunTest(String filename) {
    for (int i = 0; i < REPEAT_EACH_TEST_TIMES; i++)
      try {
        // Load YAML and parse
        var data = loadYaml(TEST_PATH_PREFIX, filename);

        // Read inputs from the test YAML and instantiate the subject
        var eventNotes = loadSubject(data);

        // Execute note picking
        List<Note> picked = new ArrayList<>();
        for (var note : eventNotes) {
          picked.add(subject.pick(note));
        }

        // assert final picks
        loadAndPerformAssertions(picked, data);

      } catch (Exception e) {
        failures.add(String.format("[%s] Exception: %s", filename, e.getMessage()));
      }
  }

  /**
   Load the input section of the test YAML file

   @param data YAML file wrapper
   */
  List<Note> loadSubject(Map<?, ?> data) throws Exception {
    Map<?, ?> obj = (Map<?, ?>) data.get("input");
    if (Objects.isNull(obj)) throw new Exception("Input is required!");

    var range = getOptionalNoteRange(obj);

    var instrumentType =
      InstrumentType.valueOf(Objects.requireNonNull(getStr(obj, "instrumentType")));

    var voicingNotes = CsvUtils.split(Objects.requireNonNull(getStr(obj, "voicingNotes"))).stream()
      .map(Note::of).collect(Collectors.toSet());


    subject = new NotePicker(range, voicingNotes, templateConfig.getInstrumentTypesForInversionSeeking().contains(instrumentType));

    return CsvUtils.split(Objects.requireNonNull(getStr(obj, "eventNotes"))).stream()
      .map(Note::of).collect(Collectors.toList());
  }

  NoteRange getOptionalNoteRange(Map<?, ?> obj) {
    Map<?, ?> rObj = (Map<?, ?>) obj.get("range");
    if (Objects.isNull(rObj)) return NoteRange.empty();
    return NoteRange.from(getStr(rObj, "from"), getStr(rObj, "to"));
  }

  void loadAndPerformAssertions(List<Note> pickedNotes, Map<?, ?> data) throws Exception {
    @Nullable
    Map<?, ?> obj = (Map<?, ?>) data.get("assertion");
    if (Objects.isNull(obj)) throw new Exception("Assertion is required!");

    var range = getOptionalNoteRange(obj);

    range.getLow().ifPresent(note ->
      assertSameNote("Range Low-end", note, subject.getTargetRange().getLow().orElseThrow()));

    range.getHigh().ifPresent(note ->
      assertSameNote("Range High-end", note, subject.getTargetRange().getHigh().orElseThrow()));

    var picked = pickedNotes.stream()
      .map(n -> n.toString(Accidental.Sharp))
      .collect(Collectors.toSet());

    Optional.ofNullable(getStr(obj, "picks"))
      .ifPresent(picks -> assertSameNotes("Picks",
        new HashSet<>(CsvUtils.split(picks)),
        picked));

    Optional.ofNullable(getInt(obj, "count"))
      .ifPresent(count -> assertEquals((int) count, picked.size()));
  }

}
