// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;

public class InstrumentTest  {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Instrument()
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setType("Percussive")
      .setName("TR-808")
      .setState("Published")
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Type is required");

    new Instrument()
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setName("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutState() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("State is required");

    new Instrument()
      .setTypeEnum(InstrumentType.Percussive)
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setName("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutLibraryID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Library ID is required");

    new Instrument()
      .setUserId(UUID.randomUUID())
      .setType("Percussive")
      .setName("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutUserID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("User ID is required");

    new Instrument()
      .setLibraryId(UUID.randomUUID())
      .setType("Percussive")
      .setName("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new Instrument()
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setState("Published")
      .setType("Percussive")
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("'butt' is not a valid type");

    new Instrument()
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setStateEnum(InstrumentState.Published)
      .setType("butt")
      .setName("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithInvalidState() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("'butt' is not a valid state");

    new Instrument()
      .setTypeEnum(InstrumentType.Percussive)
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setState("butt")
      .setName("TR-808")
      .validate();
  }


  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("state", "name", "type", "density"), new Instrument().getResourceAttributeNames());
  }

  /*

  // FUTURE adapt any of these legacy tests that seem relevant; delete definitely useless ones

  @Test
  public void of_cannotAddAudioBeforeCreatingRecord() {
    Instrument subject = new Instrument()
      .setLibraryId(UUID.randomUUID())
      .setName("shimmy")
      .setState("Published")
      .setType("Percussive");

    subject.add(of("Test audio", "audio5.wav", 0, 2, 120, 300, 0.42));

    assertEquals(CoreException.class, subject.getErrors().iterator().next().getClass());
    assertEquals("Instrument must have id before adding Audio", subject.getErrors().iterator().next().getMessage());
  }

  @Test
  public void of() throws Exception {
    Access access = of(user2, ImmutableList.of(account1), "Artist");
    InstrumentMeme inputData = new InstrumentMeme()
      .setInstrumentId(UUID.randomUUID())
      .setName("  !!2gnarLY    ");

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutInstrumentID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    InstrumentMeme inputData = new InstrumentMeme()
      .setName("gnarly");

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutName() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    InstrumentMeme inputData = new InstrumentMeme()
      .setInstrumentId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    InstrumentMeme result = testDAO.readOne(access, BigInteger.valueOf(1001L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getInstrumentId());
    assertEquals("Mold", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1001L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<InstrumentMeme> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");

    Collection<InstrumentMeme> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    testDAO.destroy(access, BigInteger.valueOf(1000L));

    assertNotExist(testDAO, BigInteger.valueOf(1000L));
  }


   */


  /*

   @Test
  public void getFirstEventsOfAudiosOfInstrument() throws Exception {
    insertAudioEvent(402, 0, 1, "PING", "G", 0.1, 0.8, Instant.now());
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    assertEquals(2, ingest.getFirstEventsOfAudiosOfInstrument(BigInteger.valueOf(201)).size());
  }

  @Test
  public void getAllInstrumentMemes() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    assertEquals(3, ingest.getAllInstrumentMemes().size());
  }

  @Test
  public void getMemesOfInstrument() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    assertEquals(2, ingest.getMemesOfInstrument(BigInteger.valueOf(201)).size());
    assertEquals(1, ingest.getMemesOfInstrument(BigInteger.valueOf(202)).size());
  }


  @Test
  public void getAllAudios() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    assertEquals(2, ingest.getAllAudios().size());
  }

  @Test
  public void getAudio() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    assertEquals("Beat", ingest.getAudio(BigInteger.valueOf(401)).getName());
  }

  @Test
  public void getAudioMap() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    Map<BigInteger, Audio> result = ingest.getAudioMap();

    assertEquals(2, result.size());
    assertEquals("Beat", result.get(BigInteger.valueOf(401)).getName());
    assertEquals("Chords Cm to D", result.get(BigInteger.valueOf(402)).getName());
  }


   */

}
