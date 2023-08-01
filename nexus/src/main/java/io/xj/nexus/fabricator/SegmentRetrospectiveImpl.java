// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.entity.EntityStoreImpl;
import io.xj.lib.util.MarbleBag;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentMeta;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.Segments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The SegmentRetrospective is a delegate to look back on previous segments, read-only
 */
class SegmentRetrospectiveImpl implements SegmentRetrospective {
  final Logger LOG = LoggerFactory.getLogger(SegmentRetrospectiveImpl.class);
  final EntityStore retroStore;
  final Map<UUID, List<SegmentChord>> segmentChords;
  Segment previousSegment;

  public SegmentRetrospectiveImpl(
    Segment segment,
    SegmentManager segmentManager
  ) throws NexusException, FabricationFatalException {
    this.retroStore = new EntityStoreImpl();

    segmentChords = Maps.newHashMap();

    // begin by getting the previous segment
    // only can build retrospective if there is at least one previous segment
    // the previous segment is the first one cached here. we may cache even further back segments below if found
    if (segment.getOffset() <= 0) return;
    try {
      // begin by getting the previous segment
      // the previous segment is the first one cached here. we may cache even further back segments below if found
      previousSegment = retroStore.put(segmentManager.readOneAtChainOffset(segment.getChainId(), segment.getOffset() - 1)
        .orElseThrow(() -> new ManagerExistenceException("No previous segment!")));
      retroStore.putAll(segmentManager.readManySubEntities(ImmutableList.of(previousSegment.getId()), true));

      // previous segment must have a main choice to continue past here.
      SegmentChoice previousSegmentMainChoice = retroStore.getAll(SegmentChoice.class).stream()
        .filter(segmentChoice -> ProgramType.Main.equals(segmentChoice.getProgramType()))
        .findFirst()
        .orElseThrow(() -> new FabricationFatalException("Retrospective sees no main choice!"));

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

      retroStore.putAll(previousMany);
      retroStore.putAll(segmentManager.readManySubEntities(Entities.idsOf(previousMany), true));

    } catch (ManagerExistenceException | ManagerFatalException | ManagerPrivilegeException | EntityStoreException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public Optional<SegmentChoice> getPreviousChoiceOfType(Segment segment, ProgramType programType) {
    return
      retroStore.getAll(SegmentChoice.class).stream()
        .filter(c -> c.getSegmentId().equals(segment.getId())
          && programType.equals(c.getProgramType()))
        .findFirst();
  }

  @Override
  public Optional<SegmentChoice> getPreviousChoiceOfType(Segment segment, UUID programVoiceId) {
    return
      retroStore.getAll(SegmentChoice.class).stream()
        .filter(c -> Objects.nonNull(c.getProgramVoiceId())
          && c.getSegmentId().equals(segment.getId())
          && programVoiceId.equals(c.getProgramVoiceId()))
        .findFirst();
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks() {
    return new ArrayList<>(retroStore.getAll(SegmentChoiceArrangementPick.class));
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
  public List<SegmentChoice> getPreviousChoicesOfMode(InstrumentMode instrumentMode) {
    Optional<Segment> seg = getPreviousSegment();
    return seg.map(segment -> retroStore.getAll(SegmentChoice.class).stream()
      .filter(c -> c.getSegmentId().equals(segment.getId())
        && Objects.nonNull(c.getInstrumentMode())
        && c.getInstrumentMode().equals(instrumentMode))
      .collect(Collectors.toList())).orElseGet(List::of);
  }

  @Override
  public List<SegmentChoice> getPreviousChoicesOfTypeMode(InstrumentType instrumentType, InstrumentMode instrumentMode) {
    Optional<Segment> seg = getPreviousSegment();
    return seg.map(segment -> retroStore.getAll(SegmentChoice.class).stream()
      .filter(c -> c.getSegmentId().equals(segment.getId())
        && Objects.nonNull(c.getInstrumentType())
        && c.getInstrumentType().equals(instrumentType)
        && c.getInstrumentMode().equals(instrumentMode))
      .collect(Collectors.toList())).orElseGet(List::of);
  }

  @Override
  public Collection<Segment> getSegments() {
    return retroStore.getAll(Segment.class);
  }

  @Override
  public Optional<Segment> getSegment(UUID id) {
    return MarbleBag.quickPick(retroStore.getAll(Segment.class).stream().filter(s -> Objects.equals(id, s.getId())).collect(Collectors.toList()));
  }

  @Override
  public Collection<SegmentChoice> getChoices() {
    return retroStore.getAll(SegmentChoice.class);
  }

  @Override
  public Optional<SegmentChoice> getPreviousChoiceForInstrument(UUID instrumentId) {
    Optional<Segment> seg = getPreviousSegment();
    return seg.flatMap(segment -> retroStore.getAll(SegmentChoice.class).stream()
      .filter(c -> c.getSegmentId().equals(segment.getId())
        && Objects.nonNull(c.getInstrumentId())
        && instrumentId.equals(c.getInstrumentId()))
      .findFirst());
  }

  @Override
  public List<SegmentChoiceArrangement> getPreviousArrangementsForInstrument(UUID instrumentId) {
    var choice = getPreviousChoiceForInstrument(instrumentId);
    return choice.map(segmentChoice -> retroStore.getAll(SegmentChoiceArrangement.class).stream()
      .filter(c -> c.getSegmentChoiceId().equals(segmentChoice.getId()))
      .collect(Collectors.toList())).orElseGet(List::of);
  }

  @Override
  public List<SegmentChoiceArrangementPick> getPreviousPicksForInstrument(UUID instrumentId) {
    var arr = getPreviousArrangementsForInstrument(instrumentId).stream()
      .map(SegmentChoiceArrangement::getId)
      .collect(Collectors.toSet());
    if (arr.isEmpty()) return List.of();
    return
      retroStore.getAll(SegmentChoiceArrangementPick.class).stream()
        .filter(c -> arr.contains(c.getSegmentChoiceArrangementId()))
        .collect(Collectors.toList());
  }

  @Override
  public InstrumentType getInstrumentType(SegmentChoiceArrangementPick pick) throws NexusException {
    return getChoice(getArrangement(pick)).getInstrumentType();
  }

  @Override
  public Optional<SegmentMeta> getPreviousMeta(String key) {
    return retroStore.getAll(SegmentMeta.class)
      .stream().filter(meta -> Objects.equals(key, meta.getKey()))
      .findAny();
  }

  @Override
  public SegmentChoiceArrangement getArrangement(SegmentChoiceArrangementPick pick) throws NexusException {
    return retroStore.get(SegmentChoiceArrangement.class, pick.getSegmentChoiceArrangementId())
      .orElseThrow(() -> new NexusException(String.format("Failed to get arrangement for SegmentChoiceArrangementPick[%s]", pick.getId())));
  }

  @Override
  public SegmentChoice getChoice(SegmentChoiceArrangement arrangement) throws NexusException {
    return retroStore.get(SegmentChoice.class, arrangement.getSegmentChoiceId())
      .orElseThrow(() -> new NexusException(String.format("Failed to get choice for SegmentChoiceArrangement[%s]", arrangement.getId())));
  }

  @Override
  public List<SegmentChord> getSegmentChords(UUID segmentId) {
    if (!segmentChords.containsKey(segmentId)) {
      segmentChords.put(segmentId,
        retroStore.getAll(SegmentChord.class)
          .stream()
          .filter(chord -> Objects.equals(segmentId, chord.getSegmentId()))
          .sorted(Comparator.comparing((SegmentChord::getPosition)))
          .collect(Collectors.toList()));
    }

    return segmentChords.get(segmentId);
  }
}
