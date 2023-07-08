package io.xj.hub.analysis;

import com.google.api.client.util.Maps;
import com.google.api.client.util.Sets;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.lib.util.Text;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
public class ReportEvents extends Report {
  final EventHistogram eventHistogram;

  public ReportEvents(HubContent content) {
    super(content);

    eventHistogram = new EventHistogram();

    Arrays.stream(InstrumentType.values()).forEach(type -> {
      content.getInstrumentAudios(List.of(type), List.of()).forEach(audio -> eventHistogram.addInstrumentId(type.toString(), audio.getEvent(), audio.getInstrumentId()));
      switch (type) {
        case Bass, Hook, Noise, Pad, Percussion, Stab, Sticky, Stripe, Sweep ->
          content.getProgramVoiceTracks(ProgramType.Detail).forEach(track -> eventHistogram.addProgramId(type.toString(), track.getName(), track.getProgramId(), false));
        case Drum ->
          content.getProgramVoiceTracks(ProgramType.Beat).forEach(track -> eventHistogram.addProgramId(type.toString(), track.getName(), track.getProgramId(), true));
      }
    });
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public List<Section> computeSections() {
    return Arrays.stream(InstrumentType.values()).map(type ->
        new Section(String.format("%s_events", type.toString().toLowerCase(Locale.ROOT)), String.format("%s Events", type),
          eventHistogram.histogram.containsKey(type.toString()) ?
            eventHistogram.histogram.get(type.toString()).entrySet().parallelStream()
              .sorted((c1, c2) -> c2.getValue().total.compareTo(c1.getValue().total))
              .map(e -> List.of(
                e.getValue().total.toString(),
                e.getKey(),
                e.getValue().programIds.parallelStream()
                  .map(content::getProgram)
                  .map(Optional::orElseThrow)
                  .sorted(Comparator.comparing(Program::getName))
                  .map(this::programRef)
                  .collect(Collectors.joining("\n")),
                e.getValue().instrumentIds.parallelStream()
                  .map(content::getInstrument)
                  .map(Optional::orElseThrow)
                  .sorted(Comparator.comparing(Instrument::getName))
                  .map(this::instrumentRef)
                  .collect(Collectors.joining("\n"))
              ))
              .toList()
            : List.of(), List.of("Total", "Event", "Programs", "Instruments"),
          List.of()))
      .toList();
  }

  @Override
  public Type getType() {
    return Type.Events;
  }

  /**
   * Get the histogram
   *
   * @return event histogram
   */
  public EventHistogram getEventHistogram() {
    return eventHistogram;
  }

  /**
   * Representation of the construction of a histogram of usage of all events
   */
  public static class EventHistogram {
    Map<String, Map<String, EventCount>> histogram;

    public EventHistogram() {
      histogram = Maps.newHashMap();
    }

    public EventCount get(String type, String raw) {
      var name = Text.toEvent(raw);
      if (!histogram.containsKey(type)) throw new RuntimeException(String.format("Unknown instrument type: %s", name));
      if (!histogram.get(type).containsKey(name)) throw new RuntimeException(String.format("Unknown event: %s", name));
      return histogram.get(type).get(name);
    }

    public int size() {
      return histogram.values().parallelStream().map(Map::size).mapToInt((v -> v)).sum();
    }

    public void addInstrumentId(String type, String raw, UUID instrumentId) {
      var name = Text.toEvent(raw);
      if (!histogram.containsKey(type)) histogram.put(type, Maps.newHashMap());
      if (!histogram.get(type).containsKey(name)) histogram.get(type).put(name, new EventCount());
      histogram.get(type).get(name).addInstrumentId(instrumentId);
    }

    public void addProgramId(String type, String raw, UUID programId, boolean addEventsOnlyInProgram) {
      var name = Text.toEvent(raw);
      if (!histogram.containsKey(type)) histogram.put(type, Maps.newHashMap());
      if (!histogram.get(type).containsKey(name)) {
        if (!addEventsOnlyInProgram) return;
        histogram.get(type).put(name, new EventCount());
      }
      histogram.get(type).get(name).addProgramId(programId);
    }
  }

  /**
   * Representation of the count of usages for one event
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
