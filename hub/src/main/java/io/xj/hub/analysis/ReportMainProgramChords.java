package io.xj.hub.analysis;

import com.google.api.client.util.Maps;
import io.xj.hub.analysis.util.ChordCount;
import io.xj.hub.client.HubContent;
import io.xj.hub.tables.pojos.Program;
import io.xj.lib.app.Environment;
import io.xj.lib.music.Chord;

import java.util.*;
import java.util.stream.Collectors;

/**
 Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
public class ReportMainProgramChords extends Report {
  private final ChordHistogram mainProgramChords;

  public ReportMainProgramChords(HubContent content, Environment env) {
    super(content, env);

    mainProgramChords = new ChordHistogram();
    content.getProgramSequenceChords().forEach(chord -> mainProgramChords.addProgramId(chord.getName(), chord.getProgramId()));
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public String renderContentHTML() {
    return TABLE(TR(true, TD("Total"), TD("Name"), TD("Programs")),
      mainProgramChords.histogram.entrySet().stream()
        .sorted((c1, c2) -> c2.getValue().getTotal().compareTo(c1.getValue().getTotal()))
        .map(e -> TR(
          false, TD(e.getValue().getTotal().toString()),
          TD(e.getKey()),
          TD(e.getValue().getProgramIds().stream()
            .map(content::getProgram)
            .map(Optional::orElseThrow)
            .sorted(Comparator.comparing(Program::getName))
            .map(this::programRef)
            .collect(Collectors.joining("\n")))
        ))
        .collect(Collectors.joining()));
  }

  @Override
  public Type getType() {
    return Type.MainProgramChords;
  }

  /**
   Representation of the construction of a histogram of usage of all mainProgramChords
   */
  private static class ChordHistogram {
    Map<String, ChordCount> histogram;

    public ChordHistogram() {
      histogram = Maps.newHashMap();
    }

    public void addProgramId(String raw, UUID programId) {
      var name = Chord.of(raw).toString();
      if (!histogram.containsKey(name)) histogram.put(name, new ChordCount());
      histogram.get(name).addProgramId(programId);
    }
  }

}
