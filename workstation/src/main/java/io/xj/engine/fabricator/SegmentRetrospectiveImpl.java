// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.fabricator;

import io.xj.model.enums.InstrumentMode;
import io.xj.model.enums.InstrumentType;
import io.xj.model.enums.ProgramType;
import io.xj.engine.FabricationException;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChoiceArrangement;
import io.xj.model.pojos.SegmentChoiceArrangementPick;
import io.xj.model.pojos.SegmentChord;
import io.xj.model.pojos.SegmentMeta;
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
  private final SegmentEntityStore entityStore;
  final List<Segment> retroSegments;
  final List<Integer> previousSegmentIds;

  @Nullable
  final Segment previousSegment;

  public SegmentRetrospectiveImpl(
    SegmentEntityStore entityStore,
    Integer segmentId
  ) throws FabricationException, FabricationFatalException {
    this.entityStore = entityStore;

    // NOTE: the segment retrospective is empty for segments of type Initial, NextMain, and NextMacro--
    // Only Continue-type segments have a retrospective

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
    return entityStore.readAll(previousSegmentIds, SegmentChoiceArrangementPick.class);
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
    return entityStore.readAll(previousSegment.getId(), SegmentChoice.class).stream()
      .filter(c -> Objects.nonNull(c.getInstrumentMode())
        && c.getInstrumentMode().equals(instrumentMode))
      .collect(Collectors.toList());
  }

  @Override
  public List<SegmentChoice> getPreviousChoicesOfTypeMode(InstrumentType instrumentType, InstrumentMode instrumentMode) {
    if (Objects.isNull(previousSegment)) return List.of();
    return entityStore.readAll(previousSegment.getId(), SegmentChoice.class).stream()
      .filter(c -> Objects.nonNull(c.getInstrumentType())
        && c.getInstrumentType().equals(instrumentType)
        && Objects.nonNull(c.getInstrumentMode())
        && c.getInstrumentMode().equals(instrumentMode))
      .collect(Collectors.toList());
  }

  @Override
  public Optional<SegmentChoice> getPreviousChoiceOfType(InstrumentType instrumentType) {
    if (Objects.isNull(previousSegment)) return Optional.empty();
    return entityStore.readAll(previousSegment.getId(), SegmentChoice.class).stream()
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
    return entityStore.readAll(previousSegmentIds, SegmentChoice.class);
  }

  @Override
  public Collection<SegmentChoice> getPreviousChoicesForInstrument(UUID instrumentId) {
    return getChoices().stream()
      .filter(c -> Objects.nonNull(c.getInstrumentId())
        && instrumentId.equals(c.getInstrumentId()))
      .collect(Collectors.toSet());
  }

  @Override
  public Collection<SegmentChoiceArrangement> getPreviousArrangementsForInstrument(UUID instrumentId) {
    return getPreviousChoicesForInstrument(instrumentId).stream().flatMap(
      segmentChoice ->
        entityStore.readAll(previousSegmentIds, SegmentChoiceArrangement.class).stream()
          .filter(c -> c.getSegmentChoiceId().equals(segmentChoice.getId()))
    ).collect(Collectors.toSet());
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPreviousPicksForInstrument(UUID instrumentId) {
    return getPreviousArrangementsForInstrument(instrumentId).stream().flatMap(
      segmentChoiceArrangement ->
        entityStore.readAll(previousSegmentIds, SegmentChoiceArrangementPick.class).stream()
          .filter(c -> c.getSegmentChoiceArrangementId().equals(segmentChoiceArrangement.getId()))
    ).collect(Collectors.toSet());
  }

  @Override
  public InstrumentType getInstrumentType(SegmentChoiceArrangementPick pick) throws FabricationException {
    return getChoice(getArrangement(pick)).getInstrumentType();
  }

  @Override
  public Optional<SegmentMeta> getPreviousMeta(String key) {
    return entityStore.readAll(previousSegmentIds, SegmentMeta.class).stream()
      .filter(m -> Objects.equals(key, m.getKey()))
      .findAny();
  }

  @Override
  public SegmentChoiceArrangement getArrangement(SegmentChoiceArrangementPick pick) throws FabricationException {
    return entityStore.readAll(pick.getSegmentId(), SegmentChoiceArrangement.class)
      .stream()
      .filter(arrangement -> Objects.equals(arrangement.getId(), pick.getSegmentChoiceArrangementId()))
      .findFirst()
      .orElseThrow(() -> new FabricationException(String.format("Failed to get arrangement for SegmentChoiceArrangementPick[%s]", pick.getId())));
  }

  @Override
  public SegmentChoice getChoice(SegmentChoiceArrangement arrangement) throws FabricationException {
    return entityStore.readAll(arrangement.getSegmentId(), SegmentChoice.class)
      .stream()
      .filter(choice -> Objects.equals(arrangement.getSegmentChoiceId(), choice.getId()))
      .findFirst()
      .orElseThrow(() -> new FabricationException(String.format("Failed to get arrangement for SegmentChoiceArrangement[%s]", arrangement.getId())));
  }

  @Override
  public List<SegmentChord> getSegmentChords(int segmentId) {
    if (segmentChords.size() <= segmentId) {
      segmentChords.set(segmentId,
        entityStore.readAll(segmentId, SegmentChord.class)
          .stream()
          .sorted(Comparator.comparing((SegmentChord::getPosition)))
          .collect(Collectors.toList())
      );
    }

    return segmentChords.get(segmentId);
  }
}
