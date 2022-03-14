package io.xj.hub.analysis;

import com.google.api.client.util.Maps;
import com.google.api.client.util.Sets;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.app.Environment;
import io.xj.lib.util.Text;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Template content Analysis #161199945
 */
public class AnalyzeDrumInstrumentEvents extends Analyze {
  private final EventHistogram events;

  public AnalyzeDrumInstrumentEvents(HubContent content, Environment env) {
    super(content, env);

    events = new EventHistogram();
    content.getInstrumentAudios(InstrumentType.Drum).forEach(audio -> events.addInstrumentId(audio.getEvent(), audio.getInstrumentId()));
    content.getProgramVoiceTracks(ProgramType.Beat).forEach(track -> events.addProgramId(track.getName(), track.getProgramId()));
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  String toHTML() {
    return TABLE(TR(TD("Total"), TD("Event"), TD("Programs"), TD("Instruments")),
      events.histogram.entrySet().stream()
        .sorted((c1, c2) -> c2.getValue().total.compareTo(c1.getValue().total))
        .map(e -> TR(
          TD(e.getValue().total.toString()),
          TD(e.getKey()),
          TD(e.getValue().programIds.stream().map(this::renderHtmlLinkToProgram).collect(Collectors.joining("\n"))),
          TD(e.getValue().instrumentIds.stream().map(this::renderHtmlLinkToInstrument).collect(Collectors.joining("\n")))
        ))
        .collect(Collectors.joining()));
  }

  @Override
  Type getType() {
    return Type.DrumInstrumentEvents;
  }

  /**
   Representation of the construction of a histogram of usage of all events
   */
  private static class EventHistogram {
    Map<String, EventCount> histogram;

    public EventHistogram() {
      histogram = Maps.newHashMap();
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
  private static class EventCount {
    Set<UUID> programIds;
    Set<UUID> instrumentIds;
    Integer total;

    public EventCount() {
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
