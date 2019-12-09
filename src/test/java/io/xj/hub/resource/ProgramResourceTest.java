// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.resource;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.Library;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.model.User;
import io.xj.core.payload.Payload;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.InternalResources;
import io.xj.craft.CraftModule;
import io.xj.hub.resource.ProgramResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;

import static io.xj.core.access.Access.CONTEXT_KEY;
import static io.xj.core.testing.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProgramResourceTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  ProgramDAO programDAO;
  private Access access;
  private ProgramResource subject;
  private User user101;
  private Library library25;
  private Library library1;

  @Before
  public void setUp() {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of((Modules.override(new CoreModule(), new CraftModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ProgramDAO.class).toInstance(programDAO);
        }
      }))));
    Account account1 = Account.create();
    access = Access.create(ImmutableList.of(account1), "User,Artist");
    user101 = User.create();
    library25 = Library.create();
    library1 = Library.create();
    subject = new ProgramResource(injector);
    injector.injectMembers(subject);
  }

  @Test
  public void readAll() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Program program1 = Program.create(user101, library25, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
    Program program2 = Program.create(user101, library25, ProgramType.Main, ProgramState.Published, "trunk", "B", 120.0, 0.6);
    Collection<Program> programs = ImmutableList.of(program1, program2);
    when(programDAO.readMany(same(access), eq(ImmutableList.of(library25.getId()))))
      .thenReturn(programs);

    Response result = subject.readAll(crc, null, library25.getId().toString());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(InternalResources.deserializePayload(result.getEntity()))
      .hasDataMany("programs", ImmutableList.of(program1.getId().toString(), program2.getId().toString()));
  }

  @Test
  public void readOne() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Program program1 = Program.create(user101, library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
    when(programDAO.readOne(same(access), eq(program1.getId()))).thenReturn(program1);

    Response result = subject.readOne(crc, program1.getId().toString());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload resultPayload = InternalResources.deserializePayload(result.getEntity());
    assertPayload(resultPayload)
      .hasDataOne("programs", program1.getId().toString());
  }



  /*

  FUTURE: implement these tests with ?include=entity,entity type parameter



  @Test
  public void readAll_notOverwhelmingAmountOfSubEntities() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    //
    Program program701 = Program.create(101, 25, ProgramType.Main, ProgramState.Published, "leaves", "C#", 120.4, 0.6);
    program701.add(ProgramMeme.create("Ants"));
    ProgramSequence sequence902 = program701.add(ProgramSequence.create(16, "decay", 0.25, "F#", 110.3));
    program701.add(ProgramSequenceChord.create(sequence902, 0.0, "G minor"));
    program701.add(ProgramSequenceChord.create(sequence902, 4.0, "C major"));
    program701.add(ProgramSequenceChord.create(sequence902, 8.0, "F7"));
    program701.add(ProgramSequenceChord.create(sequence902, 12.0, "G7"));
    program701.add(ProgramSequenceChord.create(sequence902, 16.0, "F minor"));
    program701.add(ProgramSequenceChord.create(sequence902, 20.0, "Bb major"));
    ProgramSequenceBinding binding902_0 = program701.add(ProgramSequenceBinding.create(sequence902, 0));
    ProgramSequenceBinding binding902_1 = program701.add(ProgramSequenceBinding.create(sequence902, 1));
    ProgramSequenceBinding binding902_2 = program701.add(ProgramSequenceBinding.create(sequence902, 2));
    ProgramSequenceBinding binding902_3 = program701.add(ProgramSequenceBinding.create(sequence902, 3));
    ProgramSequenceBinding binding902_4 = program701.add(ProgramSequenceBinding.create(sequence902, 4));
    program701.add(ProgramSequenceBinding.create(sequence902, 5));
    program701.add(ProgramSequenceBindingMeme.create(binding902_0, "Gravel"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_1, "Gravel"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_2, "Gravel"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_3, "Rocks"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_1, "Fuzz"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_2, "Fuzz"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_3, "Fuzz"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_4, "Noise"));
    //
    Program program35 = Program.create(101, 25, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121, 0.6);
    insert(ProgramMeme.create("Basic"));
    ProgramVoice voiceDrums = insert(ProgramVoice.create(InstrumentType.Percussive, "Drums"));
    ProgramSequence sequence35a = insert(ProgramSequence.create(16, "Base", 0.5, "C", 110.3));
    ProgramSequencePattern pattern35a1 = insert(ProgramSequencePattern.create(sequence35a, voiceDrums, ProgramPatternType.Loop, 4, "Drop"));
    insert(ProgramSequencePatternEvent.create(pattern35a1, insert(ProgramVoiceTrack.create(voiceDrums, "CLOCK")), 0.0, 1.0, "C2", 1.0));
    insert(ProgramSequencePatternEvent.create(pattern35a1, insert(ProgramVoiceTrack.create(voiceDrums, "SNORT")), 1.0, 1.0, "G5", 0.8));
    insert(ProgramSequencePatternEvent.create(pattern35a1, insert(ProgramVoiceTrack.create(voiceDrums, "KICK")), 2.5, 1.0, "C2", 0.6));
    insert(ProgramSequencePatternEvent.create(pattern35a1, insert(ProgramVoiceTrack.create(voiceDrums, "SNARL")), 3.0, 1.0, "G5", 0.9));
    ProgramSequencePattern pattern35a2 = insert(ProgramSequencePattern.create(sequence35a, voiceDrums, ProgramPatternType.Loop, 4, "Drop Alt"));
    insert(ProgramSequencePatternEvent.create(pattern35a2, insert(ProgramVoiceTrack.create(voiceDrums, "CLACK")), 0.0, 1.0, "B5", 0.9));
    insert(ProgramSequencePatternEvent.create(pattern35a2, insert(ProgramVoiceTrack.create(voiceDrums, "SNARN")), 1.0, 1.0, "D2", 1.0));
    insert(ProgramSequencePatternEvent.create(pattern35a2, insert(ProgramVoiceTrack.create(voiceDrums, "CLICK")), 2.5, 1.0, "E4", 0.7));
    insert(ProgramSequencePatternEvent.create(pattern35a2, insert(ProgramVoiceTrack.create(voiceDrums, "SNAP")), 3.0, 1.0, "c3", 0.5));
    //
    Collection<Program> programs = ImmutableList.of(
      program701,
      program35
    );
    when(programDAO.readMany(same(access), eq(ImmutableList.of(BigInteger.valueOf(25)))))
      .thenReturn(programs);
    subject.libraryId = "25";

    Response result = subject.readAll(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataMany("programs", ImmutableList.of("701", "35"));
  }

   */

    /*

  FUTURE: implement these tests with ?include=entity,entity type parameter



  @Test
  public void readOne() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Program program1 = Program.create(101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
    ProgramSequence sequence1 = program1.add(ProgramSequence.create(1, "Ants", 0.583, "D minor", 120.0));
    ProgramSequenceBinding binding1 = program1.add(ProgramSequenceBinding.create(sequence1, 0));
    ProgramSequenceBindingMeme sequenceBindingMeme1 = program1.add(ProgramSequenceBindingMeme.create(binding1, "leafy"));
    ProgramSequenceBindingMeme sequenceBindingMeme2 = program1.add(ProgramSequenceBindingMeme.create(binding1, "smooth"));
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1)))).thenReturn(program1);
    subject.id = "1";

    Response result = subject.readOne(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload resultPayload = deserializePayload(result.getEntity());
    assertPayload(resultPayload)
      .hasDataOne("programs", "1")
      .hasMany(ProgramSequence.class, ImmutableList.of(sequence1))
      .hasMany(ProgramSequenceBinding.class, ImmutableList.of(binding1));
    assertPayload(resultPayload)
      .hasIncluded("sequence-binding-memes", ImmutableList.of(sequenceBindingMeme1, sequenceBindingMeme2))
      .hasIncluded("sequences", ImmutableList.of(sequence1))
      .hasIncluded("sequence-bindings", ImmutableList.of(binding1));
  }

  @Test
  public void readOne_complexMacroProgram() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Program program = Program.create(101, 3, ProgramType.Main, ProgramState.Published, "Earth to Fire", "Ebm", 121.0, 0.6);
    ProgramMeme programMeme = program.add(ProgramMeme.create("Large"));
    ProgramSequence sequenceA = program.add(ProgramSequence.create(0, "Passion Volcano", 0.6, "Ebm", 121.0));
    ProgramSequence sequenceB = program.add(ProgramSequence.create(0, "Exploding", 0.6, "B", 121.0));
    ProgramSequenceBinding sequenceBinding0 = program.add(ProgramSequenceBinding.create(sequenceA, 0));
    ProgramSequenceBinding sequenceBinding1 = program.add(ProgramSequenceBinding.create(sequenceB, 1));
    ProgramSequenceBinding sequenceBinding2 = program.add(ProgramSequenceBinding.create(sequenceA, 2));
    ProgramSequenceBindingMeme sequenceBindingMeme0 = program.add(ProgramSequenceBindingMeme.create(sequenceBinding0, "Earth"));
    ProgramSequenceBindingMeme sequenceBindingMeme1 = program.add(ProgramSequenceBindingMeme.create(sequenceBinding1, "Fire"));
    ProgramSequenceBindingMeme sequenceBindingMeme2 = program.add(ProgramSequenceBindingMeme.create(sequenceBinding2, "Fire"));
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1)))).thenReturn(program);
    subject.id = "1";

    Response result = subject.readOne(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload resultPayload = deserializePayload(result.getEntity());
    assertPayload(resultPayload)
      .hasDataOne("programs", "12")
      .hasMany(ProgramMeme.class, ImmutableList.of(programMeme))
      .hasMany(ProgramSequence.class, ImmutableList.of(sequenceA, sequenceB))
      .hasMany(ProgramSequenceBinding.class, ImmutableList.of(sequenceBinding0, sequenceBinding1, sequenceBinding2))
      .hasMany(ProgramSequenceBindingMeme.class, ImmutableList.of(sequenceBindingMeme0, sequenceBindingMeme1, sequenceBindingMeme2));
    assertPayload(resultPayload).hasIncluded(programMeme).belongsTo(program);
    assertPayload(resultPayload).hasIncluded(sequenceBindingMeme0).belongsTo(program).belongsTo(sequenceBinding0);
    assertPayload(resultPayload).hasIncluded(sequenceBindingMeme1).belongsTo(program).belongsTo(sequenceBinding1);
    assertPayload(resultPayload).hasIncluded(sequenceBindingMeme2).belongsTo(program).belongsTo(sequenceBinding2);
    assertPayload(resultPayload).hasIncluded(sequenceBinding0).hasMany(ProgramSequenceBindingMeme.class, ImmutableList.of(sequenceBindingMeme0));
    assertPayload(resultPayload).hasIncluded(sequenceBinding1).hasMany(ProgramSequenceBindingMeme.class, ImmutableList.of(sequenceBindingMeme1));
    assertPayload(resultPayload).hasIncluded(sequenceBinding2).hasMany(ProgramSequenceBindingMeme.class, ImmutableList.of(sequenceBindingMeme2));
  }

  @Test
  public void update() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1))))
      .thenReturn(Program.create(101, 1, ProgramType.Main, ProgramState.Draft, "fonds", "C#", 120.0, 0.6));
    Program updated = Program.create(101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
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
      .thenReturn(Program.create(101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));
    ProgramSequence sequence1 = ProgramSequence.create(1, "Ants", 0.583, "D minor", 120.0);
    Program updated = Program.create(101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
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
      .hasMany(ProgramSequence.class, ImmutableList.of(sequence1))
      .belongsTo(Library.class, "1");
  }

  @Test
  public void update_addFirstEmbeddedEntity() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1))))
      .thenReturn(Program.create(101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));
    Program updated = Program.create(101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
    ProgramSequence sequence1 = updated.add(ProgramSequence.create(1, "Ants", 0.583, "D minor", 120.0));
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
      .hasMany(ProgramSequence.class, ImmutableList.of(sequence1))
      .belongsTo(Library.class, "1");
  }

  @Test
  public void update_addSecondEmbeddedEntity() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Program programBefore = Program.create(101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
    ProgramSequence sequence1 = programBefore.add(ProgramSequence.create(1, "Ants", 0.583, "D minor", 120.0));
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1))))
      .thenReturn(programBefore);
    Program programAfter = Program.create(101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
    programAfter.add(sequence1);
    ProgramSequence config2 = programAfter.add(ProgramSequence.create(1, "Log", 0.583, "D minor", 120.0));
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
      .hasMany(ProgramSequence.class, ImmutableList.of(sequence1, config2))
      .belongsTo(Library.class, "1");
  }

  @Test
  public void update_removeSecondEmbeddedEntity() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Program programBefore = Program.create(101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
    ProgramSequence sequence1 = programBefore.add(ProgramSequence.create(1, "Ants", 0.583, "D minor", 120.0));
    programBefore.add(ProgramSequence.create(1, "Log", 0.583, "D minor", 120.0));
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1))))
      .thenReturn(programBefore);
    Program programAfter = Program.create(101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
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
      .hasMany(ProgramSequence.class, ImmutableList.of(sequence1))
      .belongsTo(Library.class, "1");
  }

  @Test
  public void update_addFirstEmbeddedEntity_invalidEntity() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    when(programDAO.readOne(same(access), eq(BigInteger.valueOf(1))))
      .thenReturn(Program.create(101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));
    PayloadObject updated = Program.create(101, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6).toPayloadObject();
    ProgramVoice badVoice = new ProgramVoice()
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
      .hasMany(ProgramSequence.class, ImmutableList.of())
      .belongsTo(Library.class, "1");
  }


   */
}
