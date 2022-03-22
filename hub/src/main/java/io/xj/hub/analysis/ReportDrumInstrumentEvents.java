package io.xj.hub.analysis;

import com.google.api.client.util.Maps;
import com.google.api.client.util.Sets;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.lib.app.Environment;
import io.xj.lib.util.Text;

import java.util.*;
import java.util.stream.Collectors;

/**
 Template content Analysis #161199945
 */
public class ReportDrumInstrumentEvents extends Report {
  private final EventHistogram eventHistogram;

  public ReportDrumInstrumentEvents(HubContent content, Environment env) {
    super(content, env);

    eventHistogram = new EventHistogram();
    content.getInstrumentAudios(InstrumentType.Drum).forEach(audio -> eventHistogram.addInstrumentId(audio.getEvent(), audio.getInstrumentId()));
    content.getProgramVoiceTracks(ProgramType.Beat).forEach(track -> eventHistogram.addProgramId(track.getName(), track.getProgramId()));
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public String renderContentHTML() {
    return TABLE(TR(TD("Total"), TD("Event"), TD("Programs"), TD("Instruments")),
      eventHistogram.histogram.entrySet().stream()
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
    return Type.DrumInstrumentEvents;
  }

  /**
   Get the histogram

   @return event histogram
   */
  public EventHistogram getEventHistogram() {
    return eventHistogram;
  }

  /**
   Representation of the construction of a histogram of usage of all events
   */
  public static class EventHistogram {
    Map<String, EventCount> histogram;

    public EventHistogram() {
      histogram = Maps.newHashMap();
    }

    public EventCount get(String raw) {
      var name = Text.toEvent(raw);
      if (!histogram.containsKey(name)) throw new RuntimeException(String.format("Unknown event: %s", name));
      return histogram.get(name);
    }

    public int size() {
      return histogram.size();
    }

    public void addInstrumentId(String raw, UUID instrumentId) {
      var name = Text.toEvent(raw);
      if (!histogram.containsKey(name)) histogram.put(name, new EventCount());
      histogram.get(name).addInstrumentId(instrumentId);
    }

    public void addProgramId(String raw, UUID programId) {
      var name = Text.toEvent(raw);
      if (!histogram.containsKey(name)) histogram.put(name, new EventCount());
      histogram.get(name).addProgramId(programId);
    }
  }

  /**
   Representation of the count of usages for one event
   */
  public static class EventCount {
    Set<UUID> programIds;
    Set<UUID> instrumentIds;
    Integer total;

    public EventCount() {
      total = 0;
      programIds = Sets.newHashSet();
      instrumentIds = Sets.newHashSet();
    }

    public Set<UUID> getProgramIds() {
      return programIds;
    }

    public Set<UUID> getInstrumentIds() {
      return instrumentIds;
    }

    public Integer getTotal() {
      return total;
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
