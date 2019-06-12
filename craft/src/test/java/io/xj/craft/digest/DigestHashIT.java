// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.ingest.IngestFactory;
import io.xj.core.model.chain.sub.ChainBinding;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.craft.CraftModule;
import io.xj.craft.digest.hash.DigestHash;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestHashIT extends FixtureIT {
  private IngestFactory ingestFactory;
  private DigestFactory digestFactory;

  @Before
  public void setUp() throws Exception {
    reset();
    insertFixtureA();

    injector = Guice.createInjector(new CoreModule(), new CraftModule());
    ingestFactory = injector.getInstance(IngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @Test
  public void readHash_ofLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Collection<ChainBinding> entities = ImmutableList.of(newChainBinding("Library", 10000001));

    DigestHash result = digestFactory.hashOf(ingestFactory.ingest(access, entities));

    assertNotNull(result);
    assertEquals("Instrument-201=1407845823,Instrument-202=1407845823,Library-10000001=1407845823,Program-701=1407845823,Program-702=1407845823,Program-703=1407845823", result.toString());
    assertEquals("3dd4d8457482ddf0434e8818c7ce25e7130beb77e4f0f6835b8c5e6b159306cb", result.sha256());
  }

  @Test
  public void readHash_ofLibrary_afterUpdateEntity() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Collection<ChainBinding> entities = ImmutableList.of(newChainBinding("Library", 10000001));
    injector.getInstance(ProgramDAO.class).update(Access.internal(), BigInteger.valueOf(703),
      programFactory.newProgram(BigInteger.valueOf(703))
        .setUserId(BigInteger.valueOf(101))
        .setLibraryId(BigInteger.valueOf(10000001))
        .setKey("G")
        .setStateEnum(ProgramState.Published)
        .setTypeEnum(ProgramType.Rhythm)
        .setName("new description")
        .setTempo(150.0));

    DigestHash result = digestFactory.hashOf(ingestFactory.ingest(access, entities));

    assertNotNull(result);
    Program updatedProgram = injector.getInstance(ProgramDAO.class).readOne(Access.internal(), BigInteger.valueOf(703));
    assertNotNull(updatedProgram);

    /*
     NOTE!! the following injects new updatedEventSeconds value at %d via String.format(..)
     */
    assertEquals(String.format("Instrument-201=1407845823,Instrument-202=1407845823,Library-10000001=1407845823,Program-701=1407845823,Program-702=1407845823,Program-703=%s", Objects.requireNonNull(updatedProgram.getUpdatedAt()).getEpochSecond()), result.toString());
  }

  @Test
  public void readHash_ofLibrary_afterDestroyEntity() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Collection<ChainBinding> entities = ImmutableList.of(newChainBinding("Library", 10000001));
    injector.getInstance(ProgramDAO.class).destroy(Access.internal(), BigInteger.valueOf(703));

    DigestHash result = digestFactory.hashOf(ingestFactory.ingest(access, entities));

    assertNotNull(result);
    assertEquals("Instrument-201=1407845823,Instrument-202=1407845823,Library-10000001=1407845823,Program-701=1407845823,Program-702=1407845823", result.toString());
    assertEquals("c1231972e4994bdeac84c6b1b76e1c026e096ad13149d5f9598a4cc3315cf53a", result.sha256());
  }

}
