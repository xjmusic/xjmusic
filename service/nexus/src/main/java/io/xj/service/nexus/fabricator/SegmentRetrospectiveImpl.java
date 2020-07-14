// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityCache;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
import io.xj.service.hub.entity.ProgramType;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.entity.SegmentChoice;
import io.xj.service.nexus.entity.SegmentChoiceArrangement;
import io.xj.service.nexus.entity.SegmentChoiceArrangementPick;
import io.xj.service.nexus.entity.SegmentChord;
import io.xj.service.nexus.entity.SegmentMeme;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 The SegmentRetrospective is a delegate to look back on previous segments, read-only
 */
class SegmentRetrospectiveImpl implements SegmentRetrospective {
  private final EntityCache<Segment> segments = new EntityCache<>();
  private final EntityCache<SegmentChoiceArrangementPick> previousSegmentPicks = new EntityCache<>();
  private final EntityCache<SegmentChoiceArrangement> previousSegmentArrangements = new EntityCache<>();
  private final EntityCache<SegmentChoice> previousSegmentChoices = new EntityCache<>();
  private final EntityCache<SegmentChord> previousSegmentChords = new EntityCache<>();
  private final EntityCache<SegmentMeme> previousSegmentMemes = new EntityCache<>();
  private Segment previousSegment;

  @Inject
  public SegmentRetrospectiveImpl(
    @Assisted("access") HubClientAccess access,
    @Assisted("currentSegment") Segment segment,
    @Assisted("sourceMaterial") HubContent sourceMaterial,
    SegmentDAO segmentDAO
  ) throws FabricationException {
    try {
      // begin by getting the previous segment
      // only can build retrospective if there is at least one previous segment
      // the previous segment is the first one cached here. we may cache even further back segments below if found
      if (segment.getOffset() <= 0) return;
      previousSegment = stash(segmentDAO.readOneAtChainOffset(access,
        segment.getChainId(), segment.getOffset() - 1));
      stashAll(segmentDAO.readAllSubEntities(access, ImmutableList.of(previousSegment.getId()), true));

      // previous segment must have a main choice to continue past here.
      SegmentChoice previousSegmentMainChoice = previousSegmentChoices.getAll().stream()
        .filter(segmentChoice -> ProgramType.Main.equals(segmentChoice.getType()))
        .findFirst().orElseThrow(() -> new FabricationException("No main choice!"));

      // if relevant populate the retrospective with the previous segments with the same main sequence as this one
      Long sequenceBindingOffset = sourceMaterial.getProgramSequenceBinding(previousSegmentMainChoice.getProgramSequenceBindingId()).getOffset();
      if (0 >= sequenceBindingOffset) return;
      long oF = segment.getOffset() - sequenceBindingOffset;
      long oT = segment.getOffset() - 1;
      if (0 > oF || 0 > oT) return;
      Collection<Segment> previousMany = segmentDAO.readAllFromToOffset(access, segment.getChainId(), oF, oT);
      stashAll(previousMany);
      stashAll(segmentDAO.readAllSubEntities(access,
        previousMany.stream().map(Entity::getId).collect(Collectors.toList()), true));

    } catch (DAOExistenceException | DAOFatalException | DAOPrivilegeException e) {
      throw new FabricationException(e);
    }
  }

  /**
   Create and stash an instance in right class's array

   @param instance to stash
   */
  private <N extends Entity> N stash(N instance) {
    if (instance.getClass().isAssignableFrom(SegmentChoiceArrangementPick.class))
      previousSegmentPicks.add((SegmentChoiceArrangementPick) instance);
    else if (instance.getClass().isAssignableFrom(SegmentChoiceArrangement.class))
      previousSegmentArrangements.add((SegmentChoiceArrangement) instance);
    else if (instance.getClass().isAssignableFrom(SegmentChoice.class))
      previousSegmentChoices.add((SegmentChoice) instance);
    else if (instance.getClass().isAssignableFrom(SegmentChord.class))
      previousSegmentChords.add((SegmentChord) instance);
    else if (instance.getClass().isAssignableFrom(SegmentMeme.class))
      previousSegmentMemes.add((SegmentMeme) instance);
    else if (instance.getClass().isAssignableFrom(Segment.class))
      segments.add((Segment) instance);
    return instance;
  }

  /**
   Stash all instances

   @param instances to stash
   @param <N>       type of instances
   */
  private <N extends Entity> void stashAll(Collection<N> instances) {
    for (N instance : instances) stash(instance);
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getSegmentPicks(Segment segment) {
    return previousSegmentPicks.getAll().stream().filter(e ->
      e.getSegmentId().equals(segment.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentChoiceArrangement> getSegmentArrangements(SegmentChoice choice) {
    return previousSegmentArrangements.getAll().stream().filter(e ->
      e.getSegmentChoiceId().equals(choice.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentChoice> getSegmentChoices(Segment segment) {
    return previousSegmentChoices.getAll().stream().filter(e ->
      e.getSegmentId().equals(segment.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentChord> getSegmentChords(Segment segment) {
    return previousSegmentChords.getAll().stream().filter(e ->
      e.getSegmentId().equals(segment.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes(Segment segment) {
    return previousSegmentMemes.getAll().stream().filter(e ->
      e.getSegmentId().equals(segment.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<Segment> getSegments() {
    return segments.getAll();
  }

  @Override
  public SegmentChoice getChoiceOfType(Segment segment, ProgramType type) throws FabricationException {
    Optional<SegmentChoice> choice =
      previousSegmentChoices.getAll().stream().filter(c ->
        c.getSegmentId().equals(segment.getId()) &&
          c.getType().equals(type)).findFirst();
    if (choice.isEmpty())
      throw new FabricationException(String.format("No %s-type choice in retrospective %s", type, segment));
    return choice.get();
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice segmentChoice) {
    return previousSegmentArrangements.getAll().stream().filter(a ->
      a.getSegmentChoiceId().equals(segmentChoice.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Optional<Segment> getPreviousSegment() {
    return Optional.ofNullable(previousSegment);
  }

  @Override
  public SegmentChoice getPreviousChoiceOfType(ProgramType type) throws FabricationException {
    Optional<Segment> seg = getPreviousSegment();
    if (seg.isEmpty()) throw new FabricationException("Cannot get previous segment to get choice create");
    return getChoiceOfType(seg.get(), type);
  }

}
