//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.cache;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.core.CoreTest;
import io.xj.core.access.impl.Access;
import io.xj.core.model.instrument.InstrumentState;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CacheKeyTest extends CoreTest {

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
  public void ofAccess_okayWithoutUserAuth() {
    Access access = new Access(ImmutableMap.of(
      "userId", "1",
      "accounts", "101"
    ));
    assertEquals("Account-101|User-1", CacheKey.of(access));
  }

  @Test
  public void ofEntities() {
    assertEquals("Instrument-2|Library-15|Library-27|Program-2120|Program-764", CacheKey.of(ImmutableList.of(
      newLibrary(15L, 12L, "Apples", now()),
      newLibrary(27L, 10L, "Bananas", now()),
      newInstrument(2L, 101, 15L, InstrumentType.Harmonic, InstrumentState.Published, "Mango", now()),
      newProgram(2120, 101, 15L, ProgramType.Macro, ProgramState.Published, "Shims", "D", 120, now()),
      newProgram(764, 101, 15L, ProgramType.Main, ProgramState.Published, "Mill", "G", 120, now())
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
    assertEquals("Account-101|Account-109|Role-Engineer|Role-User|User-1|UserAuth-1[]Instrument-2|Library-15|Library-27|Program-2120|Program-764", CacheKey.of(access, ImmutableList.of(
      newLibrary(15L, 12L, "Apples", now()),
      newLibrary(27L, 10L, "Bananas", now()),
      newInstrument(2L, 101, 15L, InstrumentType.Harmonic, InstrumentState.Published, "Mango", now()),
      newProgram(2120, 101, 15L, ProgramType.Macro, ProgramState.Published, "Shims", "D", 120, now()),
      newProgram(764, 101, 15L, ProgramType.Main, ProgramState.Published, "Mill", "G", 120, now())
      )));
  }

}
