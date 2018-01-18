package io.xj.core.cache;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

import io.xj.core.access.impl.Access;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.library.Library;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.phase.Phase;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CacheKeyTest {

  @Test
  public void ofAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "accounts", "101",
      "roles", "user,engineer"
    ));
    assertEquals("Account-101|Role-Engineer|Role-User|User-1|UserAuth-1", CacheKey.of(access));
  }

  @Test
  public void ofAccess_okayWithoutRoles() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "accounts", "101"
    ));
    assertEquals("Account-101|User-1|UserAuth-1", CacheKey.of(access));
  }

  @Test
  public void ofEntities() throws Exception {
    assertEquals("Audio-35|Audio-77|Instrument-2|Library-15|Library-27|Pattern-2120|Pattern-764|Phase-122|Phase-7874", CacheKey.of(ImmutableList.of(
      new Audio(35),
      new Audio(77),
      new Instrument(2),
      new Library(15),
      new Library(27),
      new Pattern(2120),
      new Pattern(764),
      new Phase(122),
      new Phase(7874)
    )));
  }

  @Test
  public void ofAccessEntities() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "accounts", "101,109",
      "roles", "user,engineer"
    ));
    assertEquals("Account-101|Account-109|Role-Engineer|Role-User|User-1|UserAuth-1[]Audio-35|Audio-77|Instrument-2|Library-15|Library-27|Pattern-2120|Pattern-764|Phase-122|Phase-7874", CacheKey.of(access, ImmutableList.of(
      new Audio(35),
      new Audio(77),
      new Instrument(2),
      new Library(15),
      new Library(27),
      new Pattern(2120),
      new Pattern(764),
      new Phase(122),
      new Phase(7874)
    )));
  }

}
