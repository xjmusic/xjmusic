// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.analysis;

import io.xj.hub.HubContent;
import io.xj.hub.music.Chord;
import io.xj.hub.tables.pojos.Program;
import io.xj.nexus.analysis.util.ChordCount;

import java.util.*;
import java.util.stream.Collectors;

/**
 Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
public class ReportMainProgramChords extends Report {
  final ChordHistogram mainProgramChords;

  public ReportMainProgramChords(HubContent content) {
    super(content);

    mainProgramChords = new ChordHistogram();
    content.getProgramSequenceChords().forEach(chord -> mainProgramChords.addProgramId(chord.getName(), chord.getProgramId()));
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public List<Section> computeSections() {
    return List.of(
      new Section("chords", "Main Chord Summary",
        mainProgramChords.histogram.entrySet().parallelStream()
          .sorted((c1, c2) -> c2.getValue().getTotal().compareTo(c1.getValue().getTotal()))
          .map(e -> List.of(
            e.getValue().getTotal().toString(),
            e.getKey(),
            e.getValue().getProgramIds().parallelStream()
              .map(content::getProgram)
              .map(Optional::orElseThrow)
              .sorted(Comparator.comparing(Program::getName))
              .map(this::programRef)
              .collect(Collectors.joining("\n"))
          )).toList(), List.of("Total", "Name", "Programs"),
        List.of())
    );
  }

  @Override
  public Type getType() {
    return Type.MainProgramChords;
  }

  /**
   Representation of the construction of a histogram of usage of all mainProgramChords
   */
  static class ChordHistogram {
    Map<String, ChordCount> histogram;

    public ChordHistogram() {
      histogram = new HashMap<>();
    }

    public void addProgramId(String raw, UUID programId) {
      var name = Chord.of(raw).toString();
      if (!histogram.containsKey(name)) histogram.put(name, new ChordCount());
      histogram.get(name).addProgramId(programId);
    }
  }

}
