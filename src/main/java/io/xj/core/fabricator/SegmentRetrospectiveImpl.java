// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.Access;
import io.xj.core.cache.EntityCache;
import io.xj.core.dao.SegmentChoiceDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.ingest.Ingest;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoiceArrangement;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChoiceArrangementPick;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.SegmentMeme;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 The SegmentRetrospective is a delegate to look back on previous segments, read-only
 */
class SegmentRetrospectiveImpl implements SegmentRetrospective {
  private final EntityCache<Segment> segments;
  private final EntityCache<SegmentChoiceArrangementPick> segmentPicks;
  private final EntityCache<SegmentChoiceArrangement> segmentArrangements;
  private final EntityCache<SegmentChoice> segmentChoices;
  private final EntityCache<SegmentChord> segmentChords;
  private final EntityCache<SegmentMeme> segmentMemes;
  private Segment previousSegment;

  @Inject
  public SegmentRetrospectiveImpl(
    @Assisted("access") Access access,
    @Assisted("currentSegment") Segment segment,
    @Assisted("sourceMaterial") Ingest sourceMaterial,
    SegmentDAO segmentDAO,
    SegmentChoiceDAO segmentChoiceDAO
  ) throws CoreException {
    segments = new EntityCache<>();
    segmentPicks = new EntityCache<>();
    segmentArrangements = new EntityCache<>();
    segmentChoices = new EntityCache<>();
    segmentChords = new EntityCache<>();
    segmentMemes = new EntityCache<>();

    // begin by getting the previous segment
    // only can build retrospective if there is at least one previous segment
    if (segment.getOffset() <= 0) return;
    try {
      previousSegment = segmentDAO.readOneAtChainOffset(access, segment.getChainId(), segment.getOffset() - 1);
    } catch (Exception ignored) {
      return;
    }

    // the previous segment is the first one cached here. we may cache even further back segments below if found
    segments.add(previousSegment);

    // previous segment must have a main choice to continue past here.
    SegmentChoice mainChoice;
    try {
      mainChoice = segmentChoiceDAO.readOneOfTypeForSegment(access, previousSegment, ProgramType.Main);
    } catch (CoreException ignored) {
      // no previous segment main choice
      return;
    }

    // populate the retrospective with the previous segments with the same main sequence as this one
    Long sequenceBindingOffset = sourceMaterial.getProgramSequenceBinding(mainChoice.getProgramSequenceBindingId()).getOffset();
    if (0 < sequenceBindingOffset) {
      long oF = segment.getOffset() - sequenceBindingOffset;
      long oT = segment.getOffset() - 1;
      if (0 <= oF && 0 <= oT) {
        segments.addAll(segmentDAO.readAllFromToOffset(access, segment.getChainId(), oF, oT));
      }
    }

    // fetch all sub entities of all segments and store the results in the corresponding entity cache
    segmentDAO.readAllSubEntities(access, segments.getAll().stream().map(Entity::getId).collect(Collectors.toList()), true)
      .forEach(entity -> {
        switch (entity.getClass().getSimpleName()) {
          case "SegmentChoiceArrangementPick":
            segmentPicks.add((SegmentChoiceArrangementPick) entity);
            break;
          case "SegmentChoiceArrangement":
            segmentArrangements.add((SegmentChoiceArrangement) entity);
            break;
          case "SegmentChoice":
            segmentChoices.add((SegmentChoice) entity);
            break;
          case "SegmentChord":
            segmentChords.add((SegmentChord) entity);
            break;
          case "SegmentMeme":
            segmentMemes.add((SegmentMeme) entity);
            break;
        }
      });

  }


  @Override
  public Collection<SegmentChoiceArrangementPick> getSegmentPicks(Segment segment) {
    return segmentPicks.getAll().stream().filter(e ->
      e.getSegmentId().equals(segment.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentChoiceArrangement> getSegmentArrangements(SegmentChoice choice) {
    return segmentArrangements.getAll().stream().filter(e ->
      e.getSegmentChoiceId().equals(choice.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentChoice> getSegmentChoices(Segment segment) {
    return segmentChoices.getAll().stream().filter(e ->
      e.getSegmentId().equals(segment.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentChord> getSegmentChords(Segment segment) {
    return segmentChords.getAll().stream().filter(e ->
      e.getSegmentId().equals(segment.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes(Segment segment) {
    return segmentMemes.getAll().stream().filter(e ->
      e.getSegmentId().equals(segment.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<Segment> getSegments() {
    return segments.getAll();
  }

  @Override
  public SegmentChoice getChoiceOfType(Segment segment, ProgramType type) throws CoreException {
    Optional<SegmentChoice> choice =
      segmentChoices.getAll().stream().filter(c ->
        c.getSegmentId().equals(segment.getId()) &&
          c.getType().equals(type)).findFirst();
    if (choice.isEmpty())
      throw new CoreException(String.format("No %s-type choice in retrospective %s", type, segment));
    return choice.get();
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice segmentChoice) {
    return segmentArrangements.getAll().stream().filter(a ->
      a.getSegmentChoiceId().equals(segmentChoice.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Optional<Segment> getPreviousSegment() {
    return Optional.ofNullable(previousSegment);
  }

  @Override
  public SegmentChoice getPreviousChoiceOfType(ProgramType type) throws CoreException {
    Optional<Segment> seg = getPreviousSegment();
    if (seg.isEmpty()) throw new CoreException("Cannot get previous segment to get choice create");
    return getChoiceOfType(seg.get(), type);
  }

}
