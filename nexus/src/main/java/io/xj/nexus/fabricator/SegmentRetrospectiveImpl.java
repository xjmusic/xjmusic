// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.fabricator;

import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.*;
import io.xj.nexus.persistence.*;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 The SegmentRetrospective is a delegate to look back on previous segments, read-only
 */
class SegmentRetrospectiveImpl implements SegmentRetrospective {
  final Logger LOG = LoggerFactory.getLogger(SegmentRetrospectiveImpl.class);
  final List<List<SegmentChord>> segmentChords = new ArrayList<>();
  private final SegmentManager segmentManager;
  final List<Segment> retroSegments;
  final List<Integer> previousSegmentIds;

  @Nullable
  final Segment previousSegment;

  public SegmentRetrospectiveImpl(
    Segment segment,
    SegmentManager segmentManager
  ) throws NexusException, FabricationFatalException {
    this.segmentManager = segmentManager;

    // begin by getting the previous segment
    // only can build retrospective if there is at least one previous segment
    // the previous segment is the first one cached here. we may cache even further back segments below if found
    if (segment.getId() <= 0) {
      retroSegments = List.of();
      previousSegmentIds = List.of();
      previousSegment = null;
      return;
    }
    try {
      // begin by getting the previous segment
      // the previous segment is the first one cached here. we may cache even further back segments below if found
      previousSegment = segmentManager.readOne(segment.getId() - 1);

      // previous segment must have a main choice to continue past here.
      SegmentChoice previousSegmentMainChoice = segmentManager.readChoice(previousSegment.getId(), ProgramType.Main).stream()
        .filter(segmentChoice -> ProgramType.Main.equals(segmentChoice.getProgramType()))
        .findFirst()
        .orElseThrow(() -> new FabricationFatalException("Retrospective sees no main choice!"));

      retroSegments = segmentManager.readAll().stream()
        .filter(s -> {
          try {
            return segmentManager.readChoice(s.getId(), ProgramType.Main)
              .map(c -> previousSegmentMainChoice.getProgramId().equals(c.getProgramId()))
              .orElse(false);

          } catch (ManagerFatalException e) {
            LOG.warn("Failed to read choice for Segment[{}]!", SegmentUtils.getIdentifier(segment));
            return false;
          }
        })
        .collect(Collectors.toList());
      previousSegmentIds = retroSegments.stream().map(Segment::getId).collect(Collectors.toList());

    } catch (ManagerExistenceException | ManagerFatalException | ManagerPrivilegeException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public Optional<SegmentChoice> getPreviousChoiceOfType(Segment segment, ProgramType programType) {
    try {
      return
        segmentManager.readChoice(segment.getId(), programType).stream()
          .filter(c -> programType.equals(c.getProgramType()))
          .findFirst();
    } catch (ManagerFatalException e) {
      LOG.warn("Failed to read choice for Segment[{}]!", SegmentUtils.getIdentifier(segment));
      return Optional.empty();
    }
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks() {
    // return new ArrayList<>(retroStore.getAll(SegmentChoiceArrangementPick.class));
    return segmentManager.readManySubEntitiesOfType(previousSegmentIds, SegmentChoiceArrangementPick.class);
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
    try {
      if (Objects.isNull(previousSegment)) return List.of();
      return segmentManager.readManySubEntitiesOfType(previousSegment.getId(), SegmentChoice.class).stream()
        .filter(c -> Objects.nonNull(c.getInstrumentMode())
          && c.getInstrumentMode().equals(instrumentMode))
        .collect(Collectors.toList());
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.warn("Failed to read choices for Segment[{}]!", SegmentUtils.getIdentifier(previousSegment));
      return List.of();
    }
  }

  @Override
  public List<SegmentChoice> getPreviousChoicesOfTypeMode(InstrumentType instrumentType, InstrumentMode instrumentMode) {
    try {
      if (Objects.isNull(previousSegment)) return List.of();
      return segmentManager.readManySubEntitiesOfType(previousSegment.getId(), SegmentChoice.class).stream()
        .filter(c -> Objects.nonNull(c.getInstrumentType())
          && c.getInstrumentType().equals(instrumentType)
          && Objects.nonNull(c.getInstrumentMode())
          && c.getInstrumentMode().equals(instrumentMode))
        .collect(Collectors.toList());
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      LOG.warn("Failed to read choices for Segment[{}]!", SegmentUtils.getIdentifier(previousSegment));
      return List.of();
    }
  }

  @Override
  public Collection<Segment> getSegments() {
    return retroSegments;
  }

  @Override
  public Collection<SegmentChoice> getChoices() {
    return segmentManager.readManySubEntitiesOfType(previousSegmentIds, SegmentChoice.class);
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
    return choice.map(segmentChoice -> segmentManager.readManySubEntitiesOfType(previousSegmentIds, SegmentChoiceArrangement.class).stream()
      .filter(c -> c.getSegmentChoiceId().equals(segmentChoice.getId()))
      .collect(Collectors.toList())).orElseGet(List::of);
  }

  @Override
  public List<SegmentChoiceArrangementPick> getPreviousPicksForInstrument(UUID instrumentId) {
    var arr = getPreviousArrangementsForInstrument(instrumentId).stream()
      .map(SegmentChoiceArrangement::getId)
      .collect(Collectors.toSet());
    if (arr.isEmpty()) return List.of();
    return segmentManager.readManySubEntitiesOfType(previousSegmentIds, SegmentChoiceArrangementPick.class).stream()
      .filter(c -> arr.contains(c.getSegmentChoiceArrangementId()))
      .collect(Collectors.toList());
  }

  @Override
  public InstrumentType getInstrumentType(SegmentChoiceArrangementPick pick) throws NexusException {
    return getChoice(getArrangement(pick)).getInstrumentType();
  }

  @Override
  public Optional<SegmentMeta> getPreviousMeta(String key) {
    return segmentManager.readManySubEntitiesOfType(previousSegmentIds, SegmentMeta.class).stream()
      .filter(m -> Objects.equals(key, m.getKey()))
      .findAny();
  }

  @Override
  public SegmentChoiceArrangement getArrangement(SegmentChoiceArrangementPick pick) throws NexusException {
    try {
      return segmentManager.readManySubEntitiesOfType(pick.getSegmentId(), SegmentChoiceArrangement.class)
        .stream()
        .filter(arrangement -> Objects.equals(arrangement.getId(), pick.getSegmentChoiceArrangementId()))
        .findFirst()
        .orElseThrow(() -> new NexusException(String.format("Failed to get arrangement for SegmentChoiceArrangementPick[%s]", pick.getId())));
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public SegmentChoice getChoice(SegmentChoiceArrangement arrangement) throws NexusException {
    try {
      return segmentManager.readManySubEntitiesOfType(arrangement.getSegmentId(), SegmentChoice.class)
        .stream()
        .filter(choice -> Objects.equals(arrangement.getSegmentChoiceId(), choice.getId()))
        .findFirst()
        .orElseThrow(() -> new NexusException(String.format("Failed to get arrangement for SegmentChoiceArrangement[%s]", arrangement.getId())));
    } catch (ManagerPrivilegeException | ManagerFatalException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public List<SegmentChord> getSegmentChords(int segmentId) {
    if (segmentChords.size() <= segmentId) {
      try {
        segmentChords.set(segmentId,
          segmentManager.readManySubEntitiesOfType(segmentId, SegmentChord.class)
            .stream()
            .sorted(Comparator.comparing((SegmentChord::getPosition)))
            .collect(Collectors.toList())
        );
      } catch (ManagerPrivilegeException | ManagerFatalException e) {
        LOG.warn("Failed to read chords for Segment[{}]!", segmentId);
        return List.of();
      }
    }

    return segmentChords.get(segmentId);
  }
}
