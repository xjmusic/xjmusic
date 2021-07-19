// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentMeme;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreException;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.SegmentDAO;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 The SegmentRetrospective is a delegate to look back on previous segments, read-only
 */
class SegmentRetrospectiveImpl implements SegmentRetrospective {
  private final EntityStore store;
  private Segment previousSegment;

  @Inject
  public SegmentRetrospectiveImpl(
    @Assisted("access") HubClientAccess access,
    @Assisted("segment") Segment segment,
    @Assisted("sourceMaterial") HubContent sourceMaterial,
    SegmentDAO segmentDAO,
    EntityStore entityStore
  ) throws NexusException {
    this.store = entityStore;
    try {
      // begin by getting the previous segment
      // only can build retrospective if there is at least one previous segment
      // the previous segment is the first one cached here. we may cache even further back segments below if found
      if (segment.getOffset() <= 0) return;
      previousSegment = store.put(segmentDAO.readOneAtChainOffset(access,
        segment.getChainId(), segment.getOffset() - 1));
      store.putAll(segmentDAO.readManySubEntities(access, ImmutableList.of(previousSegment.getId()), true));

      // previous segment must have a main choice to continue past here.
      SegmentChoice previousSegmentMainChoice = store.getAll(SegmentChoice.class).stream()
        .filter(segmentChoice -> Program.Type.Main.equals(segmentChoice.getProgramType()))
        .findFirst()
        .orElseThrow(() -> new NexusException("No main choice!"));

      // if relevant populate the retrospective with the previous segments with the same main sequence as this one
      long sequenceBindingOffset = sourceMaterial
        .getProgramSequenceBinding(previousSegmentMainChoice.getProgramSequenceBindingId())
        .orElseThrow(() -> new DAOExistenceException("Cannot find sequence binding"))
        .getOffset();
      if (0 >= sequenceBindingOffset) return;
      long oF = segment.getOffset() - sequenceBindingOffset;
      long oT = segment.getOffset() - 1;
      if (0 > oF || 0 > oT) return;
      Collection<Segment> previousMany = segmentDAO.readManyFromToOffset(access, segment.getChainId(), oF, oT);
      store.putAll(previousMany);
      store.putAll(segmentDAO.readManySubEntities(access, Entities.idsOf(previousMany), true));

    } catch (DAOExistenceException | DAOFatalException | DAOPrivilegeException | EntityStoreException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getSegmentChoiceArrangementPicks(Segment segment) {
    return store.getAll(SegmentChoiceArrangementPick.class).stream().filter(e ->
      e.getSegmentId().equals(segment.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentChoice> getSegmentChoices(Segment segment) {
    return store.getAll(SegmentChoice.class).stream().filter(e ->
      e.getSegmentId().equals(segment.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentChoiceArrangement> getSegmentChoiceArrangements(Segment segment) {
    return store.getAll(SegmentChoiceArrangement.class).stream().filter(e ->
      e.getSegmentId().equals(segment.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes(Segment segment) {
    return store.getAll(SegmentMeme.class).stream().filter(e ->
      e.getSegmentId().equals(segment.getId()))
      .collect(Collectors.toList());

  }

  @Override
  public Collection<Segment> getSegments() {
    return store.getAll(Segment.class);

  }

  @Override
  public Optional<SegmentChoice> getChoiceOfType(Segment segment, Program.Type type) {
    return
      store.getAll(SegmentChoice.class).stream().filter(c ->
        c.getSegmentId().equals(segment.getId()) &&
          c.getProgramType().equals(type)).findFirst();

  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice segmentChoice) {
    return store.getAll(SegmentChoiceArrangement.class).stream().filter(a ->
      a.getSegmentChoiceId().equals(segmentChoice.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Optional<Segment> getPreviousSegment() {
    return Optional.ofNullable(previousSegment);
  }

  @Override
  public Optional<SegmentChoice> getPreviousSegmentChoiceOfType(Program.Type type) {
    Optional<Segment> seg = getPreviousSegment();
    if (seg.isEmpty()) return Optional.empty();
    return getChoiceOfType(seg.get(), type);
  }

  @Override
  public <N> N add(N entity) throws NexusException {
    try {
      return store.put(entity);
    } catch (EntityStoreException e) {
      throw new NexusException(e);
    }
  }

}
