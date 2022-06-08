// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.*;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreException;
import io.xj.nexus.NexusException;
import io.xj.nexus.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 The SegmentRetrospective is a delegate to look back on previous segments, read-only
 */
class SegmentRetrospectiveImpl implements SegmentRetrospective {
  private final Logger LOG = LoggerFactory.getLogger(SegmentRetrospectiveImpl.class);
  private final EntityStore store;
  private final Map<UUID, Map<Double, Optional<SegmentChord>>> chordAtPosition;
  private final Map<UUID, List<SegmentChord>> segmentChords;
  private final Map<UUID, Integer> segmentDelta;
  private Segment previousSegment;

  @Inject
  public SegmentRetrospectiveImpl(
    @Assisted("segment") Segment segment,
    SegmentManager segmentManager,
    EntityStore entityStore
  ) throws NexusException {
    this.store = entityStore;

    chordAtPosition = Maps.newHashMap();
    segmentChords = Maps.newHashMap();
    segmentDelta = Maps.newHashMap();

    // begin by getting the previous segment
    // only can build retrospective if there is at least one previous segment
    // the previous segment is the first one cached here. we may cache even further back segments below if found
    if (segment.getOffset() <= 0) return;
    try {
      // begin by getting the previous segment
      // the previous segment is the first one cached here. we may cache even further back segments below if found
      previousSegment = store.put(segmentManager.readOneAtChainOffset(segment.getChainId(), segment.getOffset() - 1));
      store.putAll(segmentManager.readManySubEntities(ImmutableList.of(previousSegment.getId()), true));

      // previous segment must have a main choice to continue past here.
      SegmentChoice previousSegmentMainChoice = store.getAll(SegmentChoice.class).stream()
        .filter(segmentChoice -> ProgramType.Main.toString().equals(segmentChoice.getProgramType()))
        .findFirst()
        .orElseThrow(() -> new NexusException("Retrospective sees no main choice!"));

      var previousMany = segmentManager.readMany(List.of(segment.getChainId())).stream()
        .filter(s -> {
          try {
            return segmentManager.readChoice(s.getId(), ProgramType.Main)
              .map(c -> previousSegmentMainChoice.getProgramId().equals(c.getProgramId()))
              .orElse(false);

          } catch (ManagerFatalException e) {
            LOG.warn("Failed to read choice for Segment[{}]!", Segments.getIdentifier(segment));
            return false;
          }
        })
        .collect(Collectors.toList());

      store.putAll(previousMany);
      store.putAll(segmentManager.readManySubEntities(Entities.idsOf(previousMany), true));

    } catch (ManagerExistenceException | ManagerFatalException | ManagerPrivilegeException | EntityStoreException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public Optional<SegmentChoice> getPreviousChoiceOfType(Segment segment, ProgramType programType) {
    return
      store.getAll(SegmentChoice.class).stream()
        .filter(c -> c.getSegmentId().equals(segment.getId())
          && programType.toString().equals(c.getProgramType()))
        .findFirst();
  }

  @Override
  public Optional<SegmentChoice> getPreviousChoiceOfType(Segment segment, UUID programVoiceId) {
    return
      store.getAll(SegmentChoice.class).stream()
        .filter(c -> Objects.nonNull(c.getProgramVoiceId())
          && c.getSegmentId().equals(segment.getId())
          && programVoiceId.equals(c.getProgramVoiceId()))
        .findFirst();
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks() {
    return new ArrayList<>(store.getAll(SegmentChoiceArrangementPick.class));
  }

  @Override
  public Optional<Segment> getPreviousSegment() {
    return Optional.ofNullable(previousSegment);
  }

  @Override
  public Optional<SegmentChoice> getPreviousChoiceOfType(ProgramType programType) {
    Optional<Segment> seg = getPreviousSegment();
    if (seg.isEmpty()) return Optional.empty();
    return getPreviousChoiceOfType(seg.get(), programType);
  }

  @Override
  public List<SegmentChoice> getPreviousChoicesOfType(InstrumentType instrumentType) {
    Optional<Segment> seg = getPreviousSegment();
    if (seg.isEmpty()) return List.of();
    return
      store.getAll(SegmentChoice.class).stream()
        .filter(c -> c.getSegmentId().equals(seg.get().getId())
          && Objects.nonNull(c.getInstrumentType())
          && c.getInstrumentType().equals(instrumentType.toString()))
        .collect(Collectors.toList());
  }

  @Override
  public List<SegmentChoice> getPreviousChoicesOfMode(InstrumentMode instrumentMode) {
    Optional<Segment> seg = getPreviousSegment();
    if (seg.isEmpty()) return List.of();
    return
      store.getAll(SegmentChoice.class).stream()
        .filter(c -> c.getSegmentId().equals(seg.get().getId())
          && Objects.nonNull(c.getInstrumentMode())
          && c.getInstrumentMode().equals(instrumentMode.toString()))
        .collect(Collectors.toList());
  }

  @Override
  public List<SegmentChoice> getPreviousChoicesOfTypeMode(InstrumentType instrumentType, InstrumentMode instrumentMode) {
    Optional<Segment> seg = getPreviousSegment();
    if (seg.isEmpty()) return List.of();
    return
      store.getAll(SegmentChoice.class).stream()
        .filter(c -> c.getSegmentId().equals(seg.get().getId())
          && Objects.nonNull(c.getInstrumentType())
          && c.getInstrumentType().equals(instrumentType.toString())
          && c.getInstrumentMode().equals(instrumentMode.toString()))
        .collect(Collectors.toList());
  }

  @Override
  public Optional<SegmentChoice> getPreviousChoiceOfVoice(UUID programVoiceId) {
    Optional<Segment> seg = getPreviousSegment();
    if (seg.isEmpty()) return Optional.empty();
    return getPreviousChoiceOfType(seg.get(), programVoiceId);
  }

  @Override
  public Collection<Segment> getSegments() {
    return store.getAll(Segment.class);
  }

  @Override
  public Optional<Segment> getSegment(UUID id) {
    return store.getAll(Segment.class).stream().filter(s->Objects.equals(id, s.getId())).findAny();
  }

  @Override
  public Collection<SegmentChoice> getChoices() {
    return store.getAll(SegmentChoice.class);
  }

  @Override
  public Optional<SegmentChoice> getPreviousChoiceForInstrument(UUID instrumentId) {
    Optional<Segment> seg = getPreviousSegment();
    if (seg.isEmpty()) return Optional.empty();
    return
      store.getAll(SegmentChoice.class).stream()
        .filter(c -> c.getSegmentId().equals(seg.get().getId())
          && Objects.nonNull(c.getInstrumentId())
          && instrumentId.equals(c.getInstrumentId()))
        .findFirst();
  }

  @Override
  public List<SegmentChoiceArrangement> getPreviousArrangementsForInstrument(UUID instrumentId) {
    var choice = getPreviousChoiceForInstrument(instrumentId);
    if (choice.isEmpty()) return List.of();
    return
      store.getAll(SegmentChoiceArrangement.class).stream()
        .filter(c -> c.getSegmentChoiceId().equals(choice.get().getId()))
        .collect(Collectors.toList());
  }

  @Override
  public List<SegmentChoiceArrangementPick> getPreviousPicksForInstrument(UUID instrumentId) {
    var arr = getPreviousArrangementsForInstrument(instrumentId).stream()
      .map(SegmentChoiceArrangement::getId)
      .collect(Collectors.toSet());
    if (arr.isEmpty()) return List.of();
    return
      store.getAll(SegmentChoiceArrangementPick.class).stream()
        .filter(c -> arr.contains(c.getSegmentChoiceArrangementId()))
        .collect(Collectors.toList());
  }

  @Override
  public InstrumentType getInstrumentType(SegmentChoiceArrangementPick pick) throws NexusException {
    return InstrumentType.valueOf(getChoice(getArrangement(pick)).getInstrumentType());
  }

  @Override
  public SegmentChoiceArrangement getArrangement(SegmentChoiceArrangementPick pick) throws NexusException {
    return store.get(SegmentChoiceArrangement.class, pick.getSegmentChoiceArrangementId())
      .orElseThrow(() -> new NexusException(String.format("Failed to get arrangement for SegmentChoiceArrangementPick[%s]", pick.getId())));
  }

  @Override
  public SegmentChoice getChoice(SegmentChoiceArrangement arrangement) throws NexusException {
    return store.get(SegmentChoice.class, arrangement.getSegmentChoiceId())
      .orElseThrow(() -> new NexusException(String.format("Failed to get choice for SegmentChoiceArrangement[%s]", arrangement.getId())));
  }

  @Override
  public Optional<SegmentChord> getChord(SegmentChoiceArrangementPick pick) {
    return getSegmentChord(pick.getSegmentId(), pick.getStart());
  }

  @Override
  public Optional<SegmentChord> getSegmentChord(UUID segmentId, Double position) {
    if (!(chordAtPosition.containsKey(segmentId) && chordAtPosition.get(segmentId).containsKey(position))) {
      if (!chordAtPosition.containsKey(segmentId)) chordAtPosition.put(segmentId, Maps.newHashMap());
      Optional<SegmentChord> foundChord = Optional.empty();
      Double foundPosition = null;

      // we assume that these entities are in order of position ascending
      for (SegmentChord segmentChord : getSegmentChords(segmentId)) {
        // if it's a better match (or no match has yet been found) then use it
        if (Objects.isNull(foundPosition) || (segmentChord.getPosition() > foundPosition && segmentChord.getPosition() <= position)) {
          foundPosition = segmentChord.getPosition();
          foundChord = Optional.of(segmentChord);
        }
      }
      chordAtPosition.get(segmentId).put(position, foundChord);
    }

    return chordAtPosition.get(segmentId).get(position);
  }

  @Override
  public Optional<SegmentChordVoicing> getSegmentChordVoicing(UUID segmentChordId, InstrumentType instrumentType) {
    return store.getAll(SegmentChordVoicing.class).stream()
      .filter(voicing -> segmentChordId.equals(voicing.getSegmentChordId()) && instrumentType.toString().equals(voicing.getType()))
      .findFirst();
  }

  @Override
  public List<SegmentChord> getSegmentChords(UUID segmentId) {
    if (!segmentChords.containsKey(segmentId)) {
      segmentChords.put(segmentId,
        store.getAll(SegmentChord.class)
          .stream()
          .filter(chord -> Objects.equals(segmentId, chord.getSegmentId()))
          .sorted(Comparator.comparing((SegmentChord::getPosition)))
          .collect(Collectors.toList()));
    }

    return segmentChords.get(segmentId);
  }

  @Override
  public Integer getSegmentDelta(UUID segmentId) {
    if (!segmentDelta.containsKey(segmentId)) {
      segmentDelta.put(segmentId, getSegment(segmentId).orElseThrow().getDelta());
    }

    return segmentDelta.get(segmentId);
  }

  @Override
  public Double getAbsolutePosition(SegmentChoiceArrangementPick pick) {
    return getSegmentDelta(pick.getSegmentId()) + pick.getStart();
  }
}
