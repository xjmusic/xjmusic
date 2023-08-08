// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.fabricator;


import io.xj.hub.HubContent;
import io.xj.hub.util.ValueException;
import io.xj.nexus.NexusException;
import io.xj.nexus.hub_client.HubClientException;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.ManagerFatalException;

/**
 * Fabricator content = contentFactory.fabricate(segment);
 * ... do things with this content, like craft or dub ...
 * content.putReport();
 * <p>
 * Unify factory with explicit methods to construct components from Fabricator https://www.pivotaltracker.com/story/show/176625033
 */
public interface FabricatorFactory {
  /**
   * Create a fabricator to fabricate a segment
   *
   * @param sourceMaterial from which to fabricate
   * @param segment        segment to be fabricated
   * @return Fabricator
   * @throws NexusException            on retry-able network or service failure
   * @throws FabricationFatalException on failure requiring a chain restart https://www.pivotaltracker.com/story/show/182131722
   */
  Fabricator fabricate(
    HubContent sourceMaterial,
    Segment segment
  ) throws NexusException, FabricationFatalException, ManagerFatalException, ValueException, HubClientException;

  /**
   * Create a retrospective to fabricate a particular segment
   * <p>
   * Fabricator content = contentFactory.workOn(segment);
   * ... do things with this content, like craft or dub ...
   * content.putReport();
   *
   * @param segment        Segment that's on the workbench
   * @param sourceMaterial to get answers about the segment content
   * @return SegmentRetrospective
   * @throws NexusException            on retry-able network or service failure
   * @throws FabricationFatalException on failure requiring a chain restart https://www.pivotaltracker.com/story/show/182131722
   */
  SegmentRetrospective loadRetrospective(
    Segment segment,
    HubContent sourceMaterial
  ) throws NexusException, FabricationFatalException;

  /**
   * Create a workbench to fabricate a particular segment
   *
   * @param segment Segment to be worked on
   * @return SegmentWorkbench
   * @throws NexusException on failure
   */
  SegmentWorkbench setupWorkbench(
    Chain chain,
    Segment segment
  ) throws NexusException;
}
