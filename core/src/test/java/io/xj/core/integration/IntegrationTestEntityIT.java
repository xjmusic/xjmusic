//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.integration;

import io.xj.core.exception.CoreException;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.user_auth.UserAuth;
import io.xj.core.model.user_role.UserRole;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntegrationTestEntityIT {

  @Test
  public void nextUniqueId() throws CoreException {
    IntegrationTestEntity.reset();
    // generate four ids for an entity (UserAuth)
    assertEquals(999000, IntegrationTestEntity.nextUniqueId(UserAuth.class, 999));
    assertEquals(999001, IntegrationTestEntity.nextUniqueId(UserAuth.class, 999));
    assertEquals(999002, IntegrationTestEntity.nextUniqueId(UserAuth.class, 999));
    assertEquals(999003, IntegrationTestEntity.nextUniqueId(UserAuth.class, 999));
    // generate one id for a different entity (UserRole)- expect to reset to suffix 0
    assertEquals(999000, IntegrationTestEntity.nextUniqueId(UserRole.class, 999));
    // generate four ids for an entirely different entity (SegmentMeme)- expect to reset from suffix 0
    assertEquals(999023000, IntegrationTestEntity.nextUniqueId(SegmentMeme.class, 999, 23));
    assertEquals(999023001, IntegrationTestEntity.nextUniqueId(SegmentMeme.class, 999, 23));
    assertEquals(999023002, IntegrationTestEntity.nextUniqueId(SegmentMeme.class, 999, 23));
    assertEquals(999023003, IntegrationTestEntity.nextUniqueId(SegmentMeme.class, 999, 23));
  }

  @Test
  public void nextUniqueId_doesReset() throws CoreException {
    IntegrationTestEntity.reset();
    assertEquals(999000, IntegrationTestEntity.nextUniqueId(UserAuth.class, 999));
    assertEquals(999001, IntegrationTestEntity.nextUniqueId(UserAuth.class, 999));
    assertEquals(999002, IntegrationTestEntity.nextUniqueId(UserAuth.class, 999));
    assertEquals(999003, IntegrationTestEntity.nextUniqueId(UserAuth.class, 999));
    // expect same results after a reset
    IntegrationTestEntity.reset();
    assertEquals(999000, IntegrationTestEntity.nextUniqueId(UserAuth.class, 999));
    assertEquals(999001, IntegrationTestEntity.nextUniqueId(UserAuth.class, 999));
    assertEquals(999002, IntegrationTestEntity.nextUniqueId(UserAuth.class, 999));
    assertEquals(999003, IntegrationTestEntity.nextUniqueId(UserAuth.class, 999));
  }

}
