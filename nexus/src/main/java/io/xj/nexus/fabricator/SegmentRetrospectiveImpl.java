// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.fabricator;

import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentMeta;
import io.xj.nexus.persistence.NexusEntityStore;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 The SegmentRetrospective is a delegate to look back on previous segments, read-only
 */
class SegmentRetrospectiveImpl implements SegmentRetrospective {
  final List<List<SegmentChord>> segmentChords = new ArrayList<>();
  private final NexusEntityStore entityStore;
  final List<Segment> retroSegments;
  final List<Integer> previousSegmentIds;

  @Nullable
  final Segment previousSegment;

  public SegmentRetrospectiveImpl(
      NexusEntityStore entityStore,
      Integer segmentId
  ) throws NexusException, FabricationFatalException {
    this.entityStore = entityStore;

    // NOTE: the segment retrospective is empty for segments of type Initial, NextMain, and NextMacro--
    // Only segments of type Continue have a retrospective

    // begin by getting the previous segment
    // only can build retrospective if there is at least one previous segment
    // the previous segment is the first one cached here. we may cache even further back segments below if found
    if (segmentId <= 0) {
      retroSegments = List.of();
      previousSegmentIds = List.of();
      previousSegment = null;
      return;
    }
    // begin by getting the previous segment
    // the previous segment is the first one cached here. we may cache even further back segments below if found
    previousSegment = entityStore.readSegment(segmentId - 1)
        .orElseThrow(() -> new FabricationFatalException("Retrospective sees no previous segment!"));

    // previous segment must have a main choice to continue past here.
    SegmentChoice previousSegmentMainChoice = entityStore.readChoice(previousSegment.getId(), ProgramType.Main).stream()
        .filter(segmentChoice -> ProgramType.Main.equals(segmentChoice.getProgramType()))
        .findFirst()
        .orElseThrow(() -> new FabricationFatalException("Retrospective sees no main choice!"));

    retroSegments = entityStore.readAllSegments().stream()
        .filter(s -> entityStore.readChoice(s.getId(), ProgramType.Main)
            .map(c -> previousSegmentMainChoice.getProgramId().equals(c.getProgramId()))
            .orElse(false))
        .collect(Collectors.toList());
    previousSegmentIds = retroSegments.stream().map(Segment::getId).collect(Collectors.toList());

  }

  @Override
  public Optional<SegmentChoice> getPreviousChoiceOfType(Segment segment, ProgramType programType) {
    return
        entityStore.readChoice(segment.getId(), programType).stream()
            .filter(c -> programType.equals(c.getProgramType()))
            .findFirst();
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks() {
    // return new ArrayList<>(retroStore.getAll(SegmentChoiceArrangementPick.class));
    return entityStore.readManySubEntitiesOfType(previousSegmentIds, SegmentChoiceArrangementPick.class);
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
    if (Objects.isNull(previousSegment)) return List.of();
    return entityStore.readManySubEntitiesOfType(previousSegment.getId(), SegmentChoice.class).stream()
        .filter(c -> Objects.nonNull(c.getInstrumentMode())
            && c.getInstrumentMode().equals(instrumentMode))
        .collect(Collectors.toList());
  }

  @Override
  public List<SegmentChoice> getPreviousChoicesOfTypeMode(InstrumentType instrumentType, InstrumentMode instrumentMode) {
    if (Objects.isNull(previousSegment)) return List.of();
    return entityStore.readManySubEntitiesOfType(previousSegment.getId(), SegmentChoice.class).stream()
        .filter(c -> Objects.nonNull(c.getInstrumentType())
            && c.getInstrumentType().equals(instrumentType)
            && Objects.nonNull(c.getInstrumentMode())
            && c.getInstrumentMode().equals(instrumentMode))
        .collect(Collectors.toList());
  }

  @Override
  public Optional<SegmentChoice> getPreviousChoiceOfType(InstrumentType instrumentType) {
    if (Objects.isNull(previousSegment)) return Optional.empty();
    return entityStore.readManySubEntitiesOfType(previousSegment.getId(), SegmentChoice.class).stream()
        .filter(c -> Objects.nonNull(c.getInstrumentType())
            && c.getInstrumentType().equals(instrumentType))
        .findFirst();
  }

  @Override
  public Collection<Segment> getSegments() {
    return retroSegments;
  }

  @Override
  public Collection<SegmentChoice> getChoices() {
    return entityStore.readManySubEntitiesOfType(previousSegmentIds, SegmentChoice.class);
  }

  @Override
  public Optional<SegmentChoice> getPreviousChoiceForInstrument(UUID instrumentId) {
    return getChoices().stream()
        .filter(c -> Objects.nonNull(c.getInstrumentId())
            && instrumentId.equals(c.getInstrumentId()))
        .findFirst();
  }

  @Override
  public List<SegmentChoiceArrangement> getPreviousArrangementsForInstrument(UUID instrumentId) {
    var choice = getPreviousChoiceForInstrument(instrumentId);
    return choice.map(segmentChoice -> entityStore.readManySubEntitiesOfType(previousSegmentIds, SegmentChoiceArrangement.class).stream()
        .filter(c -> c.getSegmentChoiceId().equals(segmentChoice.getId()))
        .collect(Collectors.toList())).orElseGet(List::of);
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPreviousPicksForInstrument(UUID instrumentId) {
    var arr = getPreviousArrangementsForInstrument(instrumentId).stream()
        .map(SegmentChoiceArrangement::getId)
        .collect(Collectors.toSet());
    if (arr.isEmpty()) return List.of();
    return entityStore.readManySubEntitiesOfType(previousSegmentIds, SegmentChoiceArrangementPick.class).stream()
        .filter(c -> arr.contains(c.getSegmentChoiceArrangementId()))
        .collect(Collectors.toSet());
  }

  @Override
  public InstrumentType getInstrumentType(SegmentChoiceArrangementPick pick) throws NexusException {
    return getChoice(getArrangement(pick)).getInstrumentType();
  }

  @Override
  public Optional<SegmentMeta> getPreviousMeta(String key) {
    return entityStore.readManySubEntitiesOfType(previousSegmentIds, SegmentMeta.class).stream()
        .filter(m -> Objects.equals(key, m.getKey()))
        .findAny();
  }

  @Override
  public SegmentChoiceArrangement getArrangement(SegmentChoiceArrangementPick pick) throws NexusException {
    return entityStore.readManySubEntitiesOfType(pick.getSegmentId(), SegmentChoiceArrangement.class)
        .stream()
        .filter(arrangement -> Objects.equals(arrangement.getId(), pick.getSegmentChoiceArrangementId()))
        .findFirst()
        .orElseThrow(() -> new NexusException(String.format("Failed to get arrangement for SegmentChoiceArrangementPick[%s]", pick.getId())));
  }

  @Override
  public SegmentChoice getChoice(SegmentChoiceArrangement arrangement) throws NexusException {
    return entityStore.readManySubEntitiesOfType(arrangement.getSegmentId(), SegmentChoice.class)
        .stream()
        .filter(choice -> Objects.equals(arrangement.getSegmentChoiceId(), choice.getId()))
        .findFirst()
        .orElseThrow(() -> new NexusException(String.format("Failed to get arrangement for SegmentChoiceArrangement[%s]", arrangement.getId())));
  }

  @Override
  public List<SegmentChord> getSegmentChords(int segmentId) {
    if (segmentChords.size() <= segmentId) {
      segmentChords.set(segmentId,
          entityStore.readManySubEntitiesOfType(segmentId, SegmentChord.class)
              .stream()
              .sorted(Comparator.comparing((SegmentChord::getPosition)))
              .collect(Collectors.toList())
      );
    }

    return segmentChords.get(segmentId);
  }
}
