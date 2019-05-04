//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment;

import io.xj.core.model.segment.impl.SegmentContent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class SegmentContentTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  SegmentContent subject;

  @Before
  public void setUp() throws Exception {
    subject = new SegmentContent();
  }

// TODO test JSON serialize and deserialize

}
