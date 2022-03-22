package io.xj.hub.analysis;

import com.google.api.client.util.Maps;
import com.google.api.client.util.Sets;
import io.xj.hub.client.HubContent;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.lib.app.Environment;
import io.xj.lib.util.Text;

import java.util.*;
import java.util.stream.Collectors;

/**
 Template content Analysis #161199945
 */
public class ReportMemes extends Report {
  private final MemeHistogram memes;

  public ReportMemes(HubContent content, Environment env) {
    super(content, env);

    memes = new MemeHistogram();
    content.getInstrumentMemes().forEach(meme -> memes.addInstrumentId(meme.getName(), meme.getInstrumentId()));
    content.getProgramMemes().forEach(meme -> memes.addProgramId(meme.getName(), meme.getProgramId()));
    content.getProgramSequenceBindingMemes().forEach(meme -> memes.addProgramId(meme.getName(), meme.getProgramId()));
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public String renderContentHTML() {
    return TABLE(TR(TD("Total"), TD("Name"), TD("Programs"), TD("Instruments")),
      memes.histogram.entrySet().stream()
        .sorted((c1, c2) -> c2.getValue().total.compareTo(c1.getValue().total))
        .map(e -> TR(
          TD(e.getValue().total.toString()),
          TD(e.getKey()),
          TD(e.getValue().programIds.stream()
            .map(content::getProgram)
            .map(Optional::orElseThrow)
            .sorted(Comparator.comparing(Program::getName))
            .map(this::programRef)
            .collect(Collectors.joining("\n"))),
          TD(e.getValue().instrumentIds.stream()
            .map(content::getInstrument)
            .map(Optional::orElseThrow)
            .sorted(Comparator.comparing(Instrument::getName))
            .map(this::instrumentRef)
            .collect(Collectors.joining("\n")))
        ))
        .collect(Collectors.joining()));
  }

  @Override
  public Type getType() {
    return Type.Memes;
  }

  /**
   Representation of the construction of a histogram of usage of all memes
   */
  private static class MemeHistogram {
    Map<String, MemeCount> histogram;

    public MemeHistogram() {
      histogram = Maps.newHashMap();
    }

    public void addInstrumentId(String raw, UUID instrumentId) {
      var name = Text.toMeme(raw);
      if (!histogram.containsKey(name)) histogram.put(name, new MemeCount());
      histogram.get(name).addInstrumentId(instrumentId);
    }

    public void addProgramId(String raw, UUID programId) {
      var name = Text.toMeme(raw);
      if (!histogram.containsKey(name)) histogram.put(name, new MemeCount());
      histogram.get(name).addProgramId(programId);
    }
  }

  /**
   Representation of the count of usages for one meme
   */
  private static class MemeCount {
    Set<UUID> programIds;
    Set<UUID> instrumentIds;
    Integer total;

    public MemeCount() {
      total = 0;
      programIds = Sets.newHashSet();
      instrumentIds = Sets.newHashSet();
    }

    public void addInstrumentId(UUID instrumentId) {
      instrumentIds.add(instrumentId);
      total++;
    }

    public void addProgramId(UUID programId) {
      programIds.add(programId);
      total++;
    }
  }

}
