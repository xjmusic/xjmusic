//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.cache;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.core.access.impl.Access;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.library.Library;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.sequence.Sequence;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CacheKeyTest {

  @Test
  public void ofAccess() {
    Access access = new Access(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "accounts", "101",
      "roles", "user,engineer"
    ));
    assertEquals("Account-101|Role-Engineer|Role-User|User-1|UserAuth-1", CacheKey.of(access));
  }

  @Test
  public void ofAccess_okayWithoutRoles() {
    Access access = new Access(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "accounts", "101"
    ));
    assertEquals("Account-101|User-1|UserAuth-1", CacheKey.of(access));
  }

  @Test
  public void ofEntities() {
    assertEquals("Audio-35|Audio-77|Instrument-2|Library-15|Library-27|Pattern-122|Pattern-7874|Sequence-2120|Sequence-764", CacheKey.of(ImmutableList.of(
      new Audio(35),
      new Audio(77),
      new Instrument(2),
      new Library(15L),
      new Library(27L),
      new Sequence(2120),
      new Sequence(764),
      new Pattern(122),
      new Pattern(7874)
    )));
  }

  @Test
  public void ofAccessEntities() {
    Access access = new Access(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "accounts", "101,109",
      "roles", "user,engineer"
    ));
    assertEquals("Account-101|Account-109|Role-Engineer|Role-User|User-1|UserAuth-1[]Audio-35|Audio-77|Instrument-2|Library-15|Library-27|Pattern-122|Pattern-7874|Sequence-2120|Sequence-764", CacheKey.of(access, ImmutableList.of(
      new Audio(35),
      new Audio(77),
      new Instrument(2),
      new Library(15L),
      new Library(27L),
      new Sequence(2120),
      new Sequence(764),
      new Pattern(122),
      new Pattern(7874)
    )));
  }

}
