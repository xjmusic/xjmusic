// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;

public class InstrumentImplTest extends CoreTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    instrumentFactory.newInstrument()
      .setUserId(BigInteger.valueOf(27437L))
      .setLibraryId(BigInteger.valueOf(907834L))
      .setType("Percussive")
      .setDescription("TR-808")
      .setState("Published")
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Type is required");

    instrumentFactory.newInstrument()
      .setUserId(BigInteger.valueOf(27437L))
      .setLibraryId(BigInteger.valueOf(907834L))
      .setDescription("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutState() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("State is required");

    instrumentFactory.newInstrument()
      .setTypeEnum(InstrumentType.Percussive)
      .setUserId(BigInteger.valueOf(27437L))
      .setLibraryId(BigInteger.valueOf(907834L))
      .setDescription("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutLibraryID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Library ID is required");

    instrumentFactory.newInstrument()
      .setUserId(BigInteger.valueOf(27437L))
      .setType("Percussive")
      .setDescription("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutUserID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("User ID is required");

    instrumentFactory.newInstrument()
      .setLibraryId(BigInteger.valueOf(5))
      .setType("Percussive")
      .setDescription("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutDescription() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Description is required");

    instrumentFactory.newInstrument()
      .setUserId(BigInteger.valueOf(27437L))
      .setLibraryId(BigInteger.valueOf(907834L))
      .setState("Published")
      .setType("Percussive")
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("'butt' is not a valid type");

    instrumentFactory.newInstrument()
      .setUserId(BigInteger.valueOf(27437L))
      .setLibraryId(BigInteger.valueOf(907834L))
      .setStateEnum(InstrumentState.Published)
      .setType("butt")
      .setDescription("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithInvalidState() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("'butt' is not a valid state");

    instrumentFactory.newInstrument()
      .setTypeEnum(InstrumentType.Percussive)
      .setUserId(BigInteger.valueOf(27437L))
      .setLibraryId(BigInteger.valueOf(907834L))
      .setState("butt")
      .setDescription("TR-808")
      .validate();
  }

  @Test
  public void create_cannotAddAudioBeforeCreatingRecord() {
    Instrument subject = instrumentFactory.newInstrument()
      .setLibraryId(BigInteger.valueOf(1L))
      .setDescription("shimmy")
      .setState("Published")
      .setType("Percussive");

    subject.add(newAudio("Test audio", "audio5.wav", 0, 2, 120, 300, 0.42));

    assertEquals(CoreException.class, subject.getErrors().iterator().next().getClass());
    assertEquals("Instrument must have id before adding Audio", subject.getErrors().iterator().next().getMessage());
  }


  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("createdAt", "updatedAt", "state", "description", "type", "density"), instrumentFactory.newInstrument().getResourceAttributeNames());
  }

  /*

  // FUTURE adapt these Instrument unit tests (from legacy integration tests)

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    InstrumentMeme inputData = new InstrumentMeme()
      .setInstrumentId(BigInteger.valueOf(1L))
      .setName("  !!2gnarLY    ");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutInstrumentID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    InstrumentMeme inputData = new InstrumentMeme()
      .setName("gnarly");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    InstrumentMeme inputData = new InstrumentMeme()
      .setInstrumentId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    InstrumentMeme result = testDAO.readOne(access, BigInteger.valueOf(1001L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getInstrumentId());
    assertEquals("Mold", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1001L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<InstrumentMeme> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<InstrumentMeme> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    testDAO.destroy(access, BigInteger.valueOf(1000L));

    assertNotExist(testDAO, BigInteger.valueOf(1000L));
  }


   */


  /*
  // FUTURE implement these tests (adapted from legacy ingest tests)

   @Test
  public void getFirstEventsOfAudiosOfInstrument() throws Exception {
    // FUTURE refactor this to use audio events already inserted
    insertAudioEvent(402, 0, 1, "PING", "G", 0.1, 0.8, Instant.now());
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(2, ingest.getFirstEventsOfAudiosOfInstrument(BigInteger.valueOf(201)).size());
  }

  @Test
  public void getAllInstrumentMemes() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(3, ingest.getAllInstrumentMemes().size());
  }

  @Test
  public void getMemesOfInstrument() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(2, ingest.getMemesOfInstrument(BigInteger.valueOf(201)).size());
    assertEquals(1, ingest.getMemesOfInstrument(BigInteger.valueOf(202)).size());
  }


  @Test
  public void getAllAudios() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(2, ingest.getAllAudios().size());
  }

  @Test
  public void getAudio() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals("Beat", ingest.getAudio(BigInteger.valueOf(401)).getName());
  }

  @Test
  public void getAudioMap() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    Map<BigInteger, Audio> result = ingest.getAudioMap();

    assertEquals(2, result.size());
    assertEquals("Beat", result.get(BigInteger.valueOf(401)).getName());
    assertEquals("Chords Cm to D", result.get(BigInteger.valueOf(402)).getName());
  }


   */

}
