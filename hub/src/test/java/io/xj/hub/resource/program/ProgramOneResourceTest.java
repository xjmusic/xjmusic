//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.resource.program;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.CoreTest;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.library.Library;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.payload.PayloadObject;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.ProgramMeme;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.program.sub.SequenceBindingMeme;
import io.xj.core.model.program.sub.Voice;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;
import java.util.UUID;

import static io.xj.core.access.impl.Access.CONTEXT_KEY;
import static io.xj.core.testing.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProgramOneResourceTest extends CoreTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  ProgramDAO programDAO;
  private Access access;
  private ProgramOneResource subject;

  @Before
  public void setUp() {
    injector = Guice.createInjector(Modules.override(new CoreModule(), new CraftModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ProgramDAO.class).toInstance(programDAO);
        }
      }));
    access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "programs", "1"
    ));
    subject = new ProgramOneResource();
    subject.setInjector(injector);
  }

  @Test
  public void readOne() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Program program1 = newProgram(1, 101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now());
    Sequence sequence1 = program1.add(newSequence(1, "Ants", 0.583, "D minor", 120.0));
    SequenceBinding binding1 = program1.add(newSequenceBinding(sequence1, 0));
    SequenceBindingMeme sequenceBindingMeme1 = program1.add(newSequenceBindingMeme(binding1, "leafy"));
    SequenceBindingMeme sequenceBindingMeme2 = program1.add(newSequenceBindingMeme(binding1, "smooth"));
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1)))).thenReturn(program1);
    subject.id = "1";

    Response result = subject.readOne(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload resultPayload = deserializePayload(result.getEntity());
    assertPayload(resultPayload)
      .hasDataOne("programs", "1")
      .hasMany(Sequence.class, ImmutableList.of(sequence1))
      .hasMany(SequenceBinding.class, ImmutableList.of(binding1));
    assertPayload(resultPayload)
      .hasIncluded("sequence-binding-memes", ImmutableList.of(sequenceBindingMeme1, sequenceBindingMeme2))
      .hasIncluded("sequences", ImmutableList.of(sequence1))
      .hasIncluded("sequence-bindings", ImmutableList.of(binding1));
  }

  @Test
  public void readOne_complexMacroProgram() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Program program = newProgram(12, 101, 3, ProgramType.Main, ProgramState.Published, "Earth to Fire", "Ebm", 121.0, now());
    ProgramMeme programMeme = program.add(newProgramMeme("Large"));
    Sequence sequenceA = program.add(newSequence(0, "Passion Volcano", 0.6, "Ebm", 121.0));
    Sequence sequenceB = program.add(newSequence(0, "Exploding", 0.6, "B", 121.0));
    SequenceBinding sequenceBinding0 = program.add(newSequenceBinding(sequenceA, 0));
    SequenceBinding sequenceBinding1 = program.add(newSequenceBinding(sequenceB, 1));
    SequenceBinding sequenceBinding2 = program.add(newSequenceBinding(sequenceA, 2));
    SequenceBindingMeme sequenceBindingMeme0 = program.add(newSequenceBindingMeme(sequenceBinding0, "Earth"));
    SequenceBindingMeme sequenceBindingMeme1 = program.add(newSequenceBindingMeme(sequenceBinding1, "Fire"));
    SequenceBindingMeme sequenceBindingMeme2 = program.add(newSequenceBindingMeme(sequenceBinding2, "Fire"));
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1)))).thenReturn(program);
    subject.id = "1";

    Response result = subject.readOne(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload resultPayload = deserializePayload(result.getEntity());
    assertPayload(resultPayload)
      .hasDataOne("programs", "12")
      .hasMany(ProgramMeme.class, ImmutableList.of(programMeme))
      .hasMany(Sequence.class, ImmutableList.of(sequenceA, sequenceB))
      .hasMany(SequenceBinding.class, ImmutableList.of(sequenceBinding0, sequenceBinding1, sequenceBinding2))
      .hasMany(SequenceBindingMeme.class, ImmutableList.of(sequenceBindingMeme0, sequenceBindingMeme1, sequenceBindingMeme2));
    assertPayload(resultPayload).hasIncluded(programMeme).belongsTo(program);
    assertPayload(resultPayload).hasIncluded(sequenceBindingMeme0).belongsTo(program).belongsTo(sequenceBinding0);
    assertPayload(resultPayload).hasIncluded(sequenceBindingMeme1).belongsTo(program).belongsTo(sequenceBinding1);
    assertPayload(resultPayload).hasIncluded(sequenceBindingMeme2).belongsTo(program).belongsTo(sequenceBinding2);
    assertPayload(resultPayload).hasIncluded(sequenceBinding0).hasMany(SequenceBindingMeme.class, ImmutableList.of(sequenceBindingMeme0));
    assertPayload(resultPayload).hasIncluded(sequenceBinding1).hasMany(SequenceBindingMeme.class, ImmutableList.of(sequenceBindingMeme1));
    assertPayload(resultPayload).hasIncluded(sequenceBinding2).hasMany(SequenceBindingMeme.class, ImmutableList.of(sequenceBindingMeme2));
  }

  @Test
  public void update() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1))))
      .thenReturn(newProgram(1, 101, 1, ProgramType.Main, ProgramState.Draft, "fonds", "C#", 120.0, now()));
    Program updated = newProgram(1, 101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now());
    Payload payload = new Payload().setDataEntity(updated);
    subject.id = "1";

    Response result = subject.update(payload, crc);

    verify(programDAO).update(same(access), eq(BigInteger.valueOf(1)), any());
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("programs", "1")
      .belongsTo(Library.class, "1");
  }

  @Test
  public void update_embeddedEntityPreservesIdFromPayload() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1))))
      .thenReturn(newProgram(1, 101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now()));
    Sequence sequence1 = newSequence(1, "Ants", 0.583, "D minor", 120.0);
    Program updated = newProgram(1, 101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now());
    updated.add(sequence1);
    Payload payload = new Payload().setDataEntity(updated);
    subject.id = "1";

    Response result = subject.update(payload, crc);

    ArgumentCaptor<Program> captor = ArgumentCaptor.forClass(Program.class);
    verify(programDAO).update(same(access), eq(BigInteger.valueOf(1)), captor.capture());
    assertNotNull(captor.getValue().getSequence(sequence1.getId()));
    //
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("programs", "1")
      .hasMany(Sequence.class, ImmutableList.of(sequence1))
      .belongsTo(Library.class, "1");
  }

  @Test
  public void update_addFirstEmbeddedEntity() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1))))
      .thenReturn(newProgram(1, 101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now()));
    Program updated = newProgram(1, 101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now());
    Sequence sequence1 = updated.add(newSequence(1, "Ants", 0.583, "D minor", 120.0));
    Payload payload = new Payload().setDataEntity(updated);
    subject.id = "1";

    Response result = subject.update(payload, crc);

    ArgumentCaptor<Program> captor = ArgumentCaptor.forClass(Program.class);
    verify(programDAO).update(same(access), eq(BigInteger.valueOf(1)), captor.capture());
    Program resultProgram = captor.getValue();
    assertEquals("fonds", resultProgram.getName());
    assertEquals(1, resultProgram.getSequences().size());
    assertNotNull(captor.getValue().getSequence(sequence1.getId()));
    //
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("programs", "1")
      .hasMany(Sequence.class, ImmutableList.of(sequence1))
      .belongsTo(Library.class, "1");
  }

  @Test
  public void update_addSecondEmbeddedEntity() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Program programBefore = newProgram(1, 101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now());
    Sequence sequence1 = programBefore.add(newSequence(1, "Ants", 0.583, "D minor", 120.0));
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1))))
      .thenReturn(programBefore);
    Program programAfter = newProgram(1, 101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now());
    programAfter.add(sequence1);
    Sequence config2 = programAfter.add(newSequence(1, "Log", 0.583, "D minor", 120.0));
    Payload payload = new Payload().setDataEntity(programAfter);
    subject.id = "1";

    Response result = subject.update(payload, crc);

    ArgumentCaptor<Program> programArgumentCaptor = ArgumentCaptor.forClass(Program.class);
    verify(programDAO).update(same(access), eq(BigInteger.valueOf(1)), programArgumentCaptor.capture());
    Program resultProgram = programArgumentCaptor.getValue();
    assertEquals("fonds", resultProgram.getName());
    assertEquals(2, resultProgram.getSequences().size());
    //
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("programs", "1")
      .hasMany(Sequence.class, ImmutableList.of(sequence1, config2))
      .belongsTo(Library.class, "1");
  }

  @Test
  public void update_removeSecondEmbeddedEntity() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Program programBefore = newProgram(1, 101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now());
    Sequence sequence1 = programBefore.add(newSequence(1, "Ants", 0.583, "D minor", 120.0));
    programBefore.add(newSequence(1, "Log", 0.583, "D minor", 120.0));
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1))))
      .thenReturn(programBefore);
    Program programAfter = newProgram(1, 101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now());
    programAfter.add(sequence1);
    Payload payload = new Payload().setDataEntity(programAfter);
    subject.id = "1";

    Response result = subject.update(payload, crc);

    ArgumentCaptor<Program> programArgumentCaptor = ArgumentCaptor.forClass(Program.class);
    verify(programDAO).update(same(access), eq(BigInteger.valueOf(1)), programArgumentCaptor.capture());
    Program resultProgram = programArgumentCaptor.getValue();
    assertEquals("fonds", resultProgram.getName());
    assertEquals(1, resultProgram.getSequences().size());
    assertEquals(sequence1.getId(), resultProgram.getSequences().iterator().next().getId());
    //
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("programs", "1")
      .hasMany(Sequence.class, ImmutableList.of(sequence1))
      .belongsTo(Library.class, "1");
  }

  @Test
  public void update_addFirstEmbeddedEntity_invalidEntity() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1))))
      .thenReturn(newProgram(1, 101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now()));
    PayloadObject updated = newProgram(1, 101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now()).toPayloadObject();
    Voice badVoice = new Voice()
      .setType("Not a real voice type");
    badVoice.setId(UUID.randomUUID());
    updated.add("voices", new Payload().setDataMany(ImmutableList.of(badVoice.toPayloadObject())));
    Payload payload = new Payload()
      .setDataOne(updated)
      .addIncluded(badVoice.toPayloadObject());
    subject.id = "1";

    Response result = subject.update(payload, crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasErrorCount(1)
      .hasDataOne("programs", "1")
      .hasMany(Sequence.class, ImmutableList.of())
      .belongsTo(Library.class, "1");
  }

}
