//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment;

import com.google.inject.assistedinject.Assisted;
import io.xj.core.model.entity.EntityFactory;

import java.math.BigInteger;

/**
 Segment segment = segmentFactory.newSegment();
 or
 Segment segment = segmentFactory.newSegment(id);
 */
public interface SegmentFactory extends EntityFactory<Segment> {

  /**
   Create a new Segment model

   @param id of new Segment
   @return ChainWorkMaster
   */
  Segment newSegment(
    @Assisted("id") BigInteger id
  );

  /**
   Create a new Segment model

   @return ChainWorkMaster
   */
  Segment newSegment();

}
