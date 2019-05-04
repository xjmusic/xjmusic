//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment.impl;

import com.google.gson.InstanceCreator;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;

import java.lang.reflect.Type;

public class SegmentInstanceCreator implements InstanceCreator<Segment> {

  private final SegmentFactory segmentFactory;

  /**
   Construct a Gson Segment instance creator that implements the Segment factory

   @param segmentFactory to use for Segment instance creation
   */
  public SegmentInstanceCreator(SegmentFactory segmentFactory) {
    this.segmentFactory = segmentFactory;
  }

  @Override
  public Segment createInstance(Type type) {
    return segmentFactory.newSegment();
  }
}

