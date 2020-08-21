// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
import io.xj.service.hub.entity.Account;
import io.xj.service.hub.entity.Instrument;
import io.xj.service.hub.entity.InstrumentState;
import io.xj.service.hub.entity.InstrumentType;
import io.xj.service.hub.entity.Library;
import io.xj.service.hub.entity.Program;
import io.xj.service.hub.entity.ProgramSequence;
import io.xj.service.hub.entity.ProgramSequenceBinding;
import io.xj.service.hub.entity.ProgramState;
import io.xj.service.hub.entity.ProgramType;
import io.xj.service.hub.entity.User;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.ChainBinding;
import io.xj.service.nexus.entity.ChainBindingType;
import io.xj.service.nexus.entity.ChainConfig;
import io.xj.service.nexus.entity.ChainConfigType;
import io.xj.service.nexus.entity.ChainState;
import io.xj.service.nexus.entity.ChainType;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.entity.SegmentState;
import io.xj.service.nexus.entity.SegmentType;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.service.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChainDAOImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  HubClient hubClient;
  private NexusEntityStore test;
  private ChainDAO subject;
  private Account account1;
  private Library library1;
  private Library library2;
  private Chain chain1;
  private Chain chain2;
  private User user3;
  private HubContent hubContent;
  private ChainConfigDAO chainConfigDAO;
  private ChainBindingDAO chainBindingDAO;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new NexusDAOModule()));
    chainConfigDAO = injector.getInstance(ChainConfigDAO.class);
    chainBindingDAO = injector.getInstance(ChainBindingDAO.class);
    EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Manipulate the underlying entity store
    test = injector.getInstance(NexusEntityStore.class);
    test.deleteAll();

    // hub entities as basis
    account1 = Account.create("fish");
    library1 = Library.create(account1, "test");
    library2 = Library.create(account1, "test");
    User user3 = User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    Program program1 = Program.create(user3, library2, ProgramType.Rhythm, ProgramState.Published, "fonds", "C#", 0.286, 0.6);
    ProgramSequence sequence1 = ProgramSequence.create(program1, 16, "epic beat part 1", 0.342, "C#", 0.286);
    ProgramSequenceBinding binding1_0 = ProgramSequenceBinding.create(sequence1, 0);
    hubContent = new HubContent(ImmutableSet.of(
      program1,
      sequence1,
      binding1_0
    ));

    // Payload comprising Nexus entities
    chain1 = test.put(Chain.create(account1, "school", ChainType.Production, ChainState.Ready,
      Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    chain2 = test.put(Chain.create(account1, "bucket", ChainType.Production, ChainState.Fabricate,
      Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));

    // Instantiate the test subject
    subject = injector.getInstance(ChainDAO.class);
  }

  @Test
  public void create() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    Chain result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt().toString());
  }

  @Test
  public void create_withEmbedKey() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState("Draft")
      .setEmbedKey("my $% favorite THINGS")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    Chain result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("my_favorite_things", result.getEmbedKey());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt().toString());
  }

  @Test
  public void create_withEmbedKey_failsIfEmbedKeyAlreadyExists() throws Exception {
    Chain first = Chain.create(account1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "my_favorite_things");
    Chain second = Chain.create(account1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "my_favorite_things");
    when(hubClient.ingest(any(), eq(ImmutableSet.of(library2.getId())), eq(ImmutableSet.of()), eq(ImmutableSet.of())))
      .thenReturn(hubContent);
    HubClientAccess access = HubClientAccess.create("Admin");
    subject.create(access, first);

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Chain already exists with embed key 'my_favorite_things'");

    subject.create(access, second);
  }

  @Test
  public void create_PreviewType() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState("Draft")
      .setType("Preview")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    Chain result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(28, Objects.requireNonNull(result.getEmbedKey()).length());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Preview, result.getType());
  }


  @Test
  public void create_PreviewType_asArtist() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "Artist");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setStartAt("now")
      .setState("Draft")
      .setType("Preview")
      .setName("coconuts");

    Chain result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Preview, result.getType());
  }

  @Test
  // [#126] Chains are always readMany in DRAFT state
  public void create_createdInDraftState() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setState("Fabricate")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    Chain result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt().toString());
  }

  @Test
  public void create_WithoutStopAt() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setState("Draft")
      .setStartAt("2009-08-12T12:17:02.527142Z");

    Chain result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertNull(result.getStopAt());
  }

  @Test
  public void create_WithEmptyStopAt() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z");

    Chain result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertNull(result.getStopAt());
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setName("coconuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Account ID is required");

    subject.create(access, input);
  }

  @Test
  public void create_FailsWithInvalidState() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setState("nutty state")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    failure.expect(DAOValidationException.class);
    failure.expectMessage("'nutty state' is not a valid state");

    subject.create(access, input);
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(Account.create()), "User");
    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Account access is required");

    subject.readOne(access, chain1.getId());
  }

  @Test
  public void readOne() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User,Engineer");

    Chain result = subject.readOne(access, chain2.getId());

    assertNotNull(result);
    assertEquals(chain2.getId(), result.getId());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("bucket", result.getName());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt().toString());
  }

  /**
   [#150279540] Unauthenticated or specifically-authenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.
   */
  @Test
  public void readOne_byEmbedKey_unauthenticatedOk() throws Exception {
    Chain chain = subject.create(HubClientAccess.internal(), Chain.create(account1, "cats test", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "cats"));
    HubClientAccess access = HubClientAccess.unauthenticated();

    Chain result = subject.readOne(access, "cats");

    assertNotNull(result);
    assertEquals(chain.getId(), result.getId());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("cats test", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt().toString());
    assertEquals("cats", result.getEmbedKey());
  }

  @Test
  public void readOneJSONObject_FailsWhenUserIsNotInAccount() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(Account.create()), "User");
    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Account access is required");

    subject.readOne(access, chain1.getId());
  }

  @Test
  public void readMany() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User,Artist");

    Collection<Chain> result = subject.readMany(access, ImmutableList.of(account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_excludesChainsInFabricateState() throws Exception {
    Chain.create(account1, "sham", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null);
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User");

    Collection<Chain> result = subject.readMany(access, ImmutableList.of(account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readManyInState() throws Exception {
    Collection<Chain> result = subject.readManyInState(HubClientAccess.internal(), ChainState.Fabricate);

    assertNotNull(result);
    assertEquals(1L, result.size());
    Chain result0 = result.iterator().next();

    assertEquals("bucket", result0.getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfAccount() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(Account.create()), "User");

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Account access is required");

    subject.readMany(access, ImmutableList.of(account1.getId()));
  }

  @Test
  public void update() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    subject.update(access, chain2.getId(), input);

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }

  @Test
  public void update_cantChangeEndOfPreviewChain() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain previewChain = test.put(Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType("Preview")
      .setState("Fabricate")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z"));
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType("Preview")
      .setState("Fabricate")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-12T12:17:01.989941Z");

    subject.update(access, previewChain.getId(), input);

    Chain result = subject.readOne(HubClientAccess.internal(), previewChain.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Preview, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }

  @Test
  public void update_addEmbedKey() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setEmbedKey("twenty %$** four HOURS")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    subject.update(access, chain2.getId(), input);

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals("twenty_four_hours", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }

  @Test
  public void update_removeEmbedKey() throws Exception {
    Chain chain3 = test.put(Chain.create(account1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "twenty_four_hours"));
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setEmbedKey("")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    subject.update(access, chain3.getId(), input);

    Chain result = subject.readOne(HubClientAccess.internal(), chain3.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertNull(result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }

  @Test
  public void update_addEmbedKey_failsIfEmbedKeyAlreadyExists() throws Exception {
    test.put(Chain.create(account1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "twenty_four_hours"));
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setEmbedKey("twenty_four_hours")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Chain already exists with embed key 'twenty_four_hours'");

    subject.update(access, chain2.getId(), input);
  }

  @Test
  public void update_okayWithEmbedKey() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain chain = test.put(Chain.create(account1, "school", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setState("Ready")
      .setEmbedKey("jabberwocky")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    subject.update(access, chain.getId(), input);

    Chain result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.Ready, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }


  @Test
  public void update_cannotChangeType() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType("Preview")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Cannot modify Chain Type");

    subject.update(access, chain2.getId(), input);
  }

  @Test
  public void update_failsToChangeStartAt_whenChainsHasSegment() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    test.put(ChainBinding.create(chain2, library2));
    test.put(Segment.create()
      .setChainId(chain2.getId())
      .setOffset(5L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("A major")
      .setTotal(64)
      .setDensity(0.52)
      .setTempo(120.0)
      .setBeginAtInstant(Instant.now())
      .setTypeEnum(SegmentType.NextMain)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("bucket")
      .setType("Production")
      .setState("Fabricate")
      .setStartAt("2015-05-10T12:17:03.527142Z")
      .setStopAt("2015-06-09T12:17:01.047563Z");

    failure.expect(DAOValidationException.class);
    failure.expectMessage("cannot change chain startAt time after it has segments");

    subject.update(access, chain2.getId(), input);
  }

  @Test
  public void update_canChangeName_whenChainsHasSegment() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    test.put(ChainBinding.create(chain2, library2));
    test.put(Segment.create()
      .setChainId(chain1.getId())
      .setOffset(5L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("A major")
      .setTotal(64)
      .setDensity(0.52)
      .setTempo(120.0)
      .setBeginAtInstant(Instant.now())
      .setTypeEnum(SegmentType.NextMain)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2015-05-10T12:18:02.527142Z")
      .setUpdatedAt("2015-05-10T12:18:32.527142Z"));
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState("Fabricate")
      .setType("Production")
      .setStartAt("2015-05-10T12:17:02.527142Z")
      .setStopAt("2015-06-09T12:17:01.047563Z");

    subject.update(access, chain2.getId(), input);

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt().toString());
  }

  @Test
  public void update_RemoveStopAt() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z");

    subject.update(access, chain2.getId(), input);

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertNull(result.getStopAt());
  }

  @Test
  public void update_FailsWithoutAccountID() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setName("coconuts")
      .setState("Fabricate")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Account ID is required");

    subject.update(access, chain2.getId(), input);
  }

  @Test
  public void update_FailsWithoutName() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Name is required");

    subject.update(access, chain2.getId(), input);
  }

  @Test
  public void update_CannotChangeAccount() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain input = Chain.create()
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState("Complete")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    subject.update(access, chain2.getId(), input);

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
  }

  @Test
  public void updateState() throws Exception {
    HubClientAccess access = HubClientAccess.create("Internal");

    subject.updateState(access, chain2.getId(), ChainState.Complete);

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals(ChainState.Complete, result.getState());
  }

  @Test
  public void updateState_WithoutAccountAccess_Fails() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(Account.create()), "User");

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Account access is required");

    subject.updateState(access, chain2.getId(), ChainState.Complete);
  }

  @Test
  public void updateState_WithAccountAccess() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User,Engineer");

    subject.updateState(access, chain2.getId(), ChainState.Complete);

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals(ChainState.Complete, result.getState());
  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutEngineerRole_ForProductionChain() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User");

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Engineer role is required");

    subject.updateState(access, chain2.getId(), ChainState.Complete);
  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutArtistOrEngineerRole_ForPreviewChain() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User");
    Chain chain3 = test.put(Chain.create(account1, "bucket", ChainType.Preview, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Engineer/Artist role is required");

    subject.updateState(access, chain3.getId(), ChainState.Complete);
  }

  @Test
  public void update_outOfDraft_WithoutEntitiesBound_Fails() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User,Artist,Engineer");
    Chain chain3 = test.put(Chain.create(account1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    chain3.setStateEnum(ChainState.Ready);

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Chain must be bound to at least one Library, Sequence, or Instrument");

    subject.update(access, chain3.getId(), chain3);
  }

  @Test
  public void updateState_outOfDraft_WithoutEntitiesBound_Fails() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User,Artist,Engineer");
    Chain chain3 = test.put(Chain.create(account1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Chain must be bound to at least one Library, Sequence, or Instrument");

    subject.updateState(access, chain3.getId(), ChainState.Ready);
  }

  @Test
  public void updateState_outOfDraft_BoundToLibrary() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User,Artist,Engineer");
    Chain chain = test.put(Chain.create(account1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    library2 = Library.create(account1, "pajamas", Instant.now());
    test.put(ChainBinding.create(chain, library2));

    subject.updateState(access, chain.getId(), ChainState.Ready);

    Chain result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(ChainState.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToSequence() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User,Artist,Engineer");
    user3 = User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    library2 = test.put(Library.create(account1, "pajamas", Instant.now()));
    Program program = Program.create(user3, library2, ProgramType.Rhythm, ProgramState.Published, "fonds", "C#", 0.286, 0.6);
    Chain chain = test.put(Chain.create(account1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    test.put(ChainBinding.create(chain, program));

    subject.updateState(access, chain.getId(), ChainState.Ready);

    Chain result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(ChainState.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToMultipleSequences() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User,Artist,Engineer");
    user3 = User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    library2 = Library.create(account1, "pajamas", Instant.now());
    Chain chain = test.put(Chain.create(account1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    test.put(ChainBinding.create(chain, Program.create(user3, library2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6)));
    test.put(ChainBinding.create(chain, Program.create(user3, library2, ProgramType.Macro, ProgramState.Published, "trees A to B", "D#", 120.0, 0.6)));
    test.put(ChainBinding.create(chain, Program.create(user3, library2, ProgramType.Macro, ProgramState.Published, "trees B to A", "F", 120.0, 0.6)));
    test.put(ChainBinding.create(chain, Program.create(user3, library2, ProgramType.Rhythm, ProgramState.Published, "beets", "C", 120.0, 0.6)));

    subject.updateState(access, chain.getId(), ChainState.Ready);

    Chain result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(ChainState.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToInstrument() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User,Artist,Engineer");
    user3 = User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    Chain chain = test.put(Chain.create(account1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    library2 = Library.create(account1, "pajamas", Instant.now());
    test.put(ChainBinding.create(chain, Instrument.create(user3, library2, InstrumentType.Harmonic, InstrumentState.Published, "fonds")));

    subject.updateState(access, chain.getId(), ChainState.Ready);

    Chain result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(ChainState.Ready, result.getState());
  }

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.
   [#170273871] Revived chain should always start now
   */
  @Test
  public void revive() throws Exception {
    Account account2 = Account.create();
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1, account2), "User,Admin,Artist,Engineer");

    Chain chain = test.put(Chain.create(account1, "school", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(ChainBinding.create(chain, Library.create(account1, "pajamas", Instant.now())));

    Chain result = subject.revive(access, chain.getId());

    assertNotNull(result);
    assertTrue(1000 > Instant.now().toEpochMilli() - result.getStartAt().toEpochMilli()); // [#170273871] Revived chain should always start now
    assertEquals("school", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());

    Chain priorChain = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(priorChain);
    assertEquals(ChainState.Failed, priorChain.getState());
    assertNull(priorChain.getEmbedKey());
//  FUTURE assert for real message sent about work  org.junit.Assert.assertEquals(1, injector.getInstance(PlatformMessageDAO.class).readManyPreviousDays(HubClientAccess.internal(), 1).size());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require exists chain of which to revived, throw error if not found.
   */
  @Test
  public void revive_failsIfNotExistPriorChain() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    failure.expect(DAOExistenceException.class);
    failure.expectMessage("does not exist");

    subject.revive(access, UUID.randomUUID());
  }

  /**
   [#160299309] Engineer wants a *revived* action, throw error if trying to revived of chain that is not production in fabricate state
   */
  @Test
  public void revive_failsIfNotFabricateState() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Chain chain = test.put(Chain.create(account1, "school", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(ChainBinding.create(chain, Library.create(account1, "pajamas", Instant.now())));
    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Only a Fabricate-state Chain can be revived.");

    subject.revive(access, chain.getId());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require engineer access or top level
   */
  @Test
  public void revive_okayWithEngineerAccess() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "Engineer");
    Chain chain = test.put(Chain.create(account1, "school", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(ChainBinding.create(chain, Library.create(account1, "pajamas", Instant.now())));

    Chain result = subject.revive(access, chain.getId());

    assertNotNull(result);
    assertEquals("school", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require engineer access or top level
   */
  @Test
  public void revive_failsWithArtistAccess() throws Exception {
    HubClientAccess access = HubClientAccess.create("Artist");
    Chain chain = test.put(Chain.create(account1, "school", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(ChainBinding.create(chain, Library.create(account1, "pajamas", Instant.now())));

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Account access is required");

    subject.revive(access, chain.getId());
  }

  /**
   [#160299309] Engineer wants a *revived* action, duplicates all ChainBindings, including ChainConfig, ChainInstrument, ChainLibrary, and ChainSequence
   */
  @Test
  public void revive_duplicatesAllChainBindings() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "Engineer");
    user3 = User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    Chain chain = test.put(Chain.create(account1, "school", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(ChainConfig.create(chain, ChainConfigType.OutputFrameRate, "1,4,35"));
    test.put(ChainConfig.create(chain, ChainConfigType.OutputChannels, "2,83,4"));
    test.put(ChainBinding.create(chain, Library.create(account1, "pajamas", Instant.now())));
    test.put(ChainBinding.create(chain, Program.create(user3, library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6)));
    test.put(ChainBinding.create(chain, Program.create(user3, library1, ProgramType.Macro, ProgramState.Published, "trees A to B", "D#", 120.0, 0.6)));
    test.put(ChainBinding.create(chain, Program.create(user3, library1, ProgramType.Macro, ProgramState.Published, "trees B to A", "F", 120.0, 0.6)));
    test.put(ChainBinding.create(chain, Program.create(user3, library1, ProgramType.Rhythm, ProgramState.Published, "beets", "C", 120.0, 0.6)));
    test.put(ChainBinding.create(chain, Instrument.create(user3, library1, InstrumentType.Harmonic, InstrumentState.Published, "fonds")));

    Chain result = subject.revive(access, chain.getId());

    assertNotNull(result);
    assertEquals(2, chainConfigDAO.readMany(HubClientAccess.internal(), ImmutableList.of(result.getId())).size());
    Collection<ChainBinding> bindings = chainBindingDAO.readMany(HubClientAccess.internal(), ImmutableList.of(result.getId()));
    assertEquals(1, bindings.stream().filter(binding -> binding.getType().equals(ChainBindingType.Instrument)).count());
    assertEquals(1, bindings.stream().filter(binding -> binding.getType().equals(ChainBindingType.Library)).count());
    assertEquals(4, bindings.stream().filter(binding -> binding.getType().equals(ChainBindingType.Program)).count());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment() throws Exception {
    HubClientAccess access = HubClientAccess.create("Internal");
    chain1 = test.put(Chain.create(account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(ChainBinding.create(chain1, library2));
    test.put(Segment.create()
      .setChainId(chain1.getId())
      .setOffset(5L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTypeEnum(SegmentType.NextMain)
      .setBeginAt("2014-02-14T12:03:40.000001Z")
      .setEndAt("2014-02-14T12:04:10.000001Z")
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T11:53:40.000001Z")).orElseThrow();

    assertEquals(chain1.getId(), result.getChainId());
    assertEquals(Long.valueOf(6), result.getOffset());
    assertEquals("2014-02-14T12:04:10.000001Z", result.getBeginAt().toString());
    assertEquals(SegmentType.Pending, result.getType());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butNotSoLongEnoughToBeComplete() throws Exception {
    HubClientAccess access = HubClientAccess.create("Internal");
    chain1 = test.put(Chain.create(account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(Segment.create()
      .setChainId(chain1.getId())
      .setOffset(5L)
      .setTypeEnum(SegmentType.NextMain)
      .setBeginAt("2014-02-14T12:03:40.000001Z")
      .setEndAt("2014-02-14T12:03:50.000001Z")
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T13:53:50.000001Z")).orElseThrow();

    Chain resultChain = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultChain);
    assertEquals(ChainState.Fabricate, resultChain.getState());
    assertEquals(SegmentType.Pending, result.getType());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butLastSegmentNotDubbedSoChainNotComplete() throws Exception {
    HubClientAccess access = HubClientAccess.create("Internal");
    chain1 = test.put(Chain.create(account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(Segment.create()
      .setChainId(chain1.getId())
      .setOffset(5L)
      .setBeginAtInstant(Instant.now())
      .setTypeEnum(SegmentType.NextMain)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z"));

    assertTrue(result.isEmpty());
    Chain resultFinal = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(ChainState.Fabricate, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_andGetsUpdatedToComplete() throws Exception {
    HubClientAccess access = HubClientAccess.create("Internal");
    chain1 = test.put(Chain.create(account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(ChainBinding.create(chain1, library1));
    test.put(Segment.create()
      .setChainId(chain1.getId())
      .setTypeEnum(SegmentType.NextMain)
      .setBeginAt("2014-02-14T14:03:35.000001Z")
      .setEndAt("2014-02-14T14:03:55.000001Z")
      .setOffset(5L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z"));

    assertTrue(result.isEmpty());
    Chain resultFinal = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(ChainState.Complete, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butCantKnowBecauseBoundsProvidedAreNull() throws Exception {
    HubClientAccess access = HubClientAccess.create("Internal");
    chain1 = test.put(Chain.create(account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    chain1.setCreatedAt("2014-02-14T14:03:15.000001Z").setUpdatedAt("2014-02-14T14:03:45.000001Z");
    test.put(Segment.create()
      .setChainId(chain1.getId())
      .setBeginAt("2014-02-14T14:03:15.000001Z")
      .setEndAt("2014-02-14T14:03:25.000001Z")
      .setOffset(5L)
      .setTypeEnum(SegmentType.NextMain)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z")).orElseThrow();

    Chain resultFinal = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(ChainState.Fabricate, resultFinal.getState());
    assertEquals(SegmentType.Pending, result.getType());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsAlreadyHasNextSegment() throws Exception {
    HubClientAccess access = HubClientAccess.create("Internal");
    chain1 = test.put(Chain.create(account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(Segment.create()
      .setChainId(chain1.getId())
      .setBeginAt("2014-02-14T14:03:15.000001Z")
      .setEndAt("2014-02-14T14:05:40.000001Z") // after chain stop-at, that's what triggers the chain to be completed
      .setOffset(5L)
      .setTypeEnum(SegmentType.NextMain)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z"));

    assertTrue(result.isEmpty());
  }

  @Test
  public void buildNextSegmentOrComplete_chainEndingInCraftedSegment() throws Exception {
    HubClientAccess access = HubClientAccess.create("Internal");
    Chain fromChain = test.put(Chain.create(account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    test.put(Segment.create(fromChain, 5, SegmentState.Crafted, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-08-12T14:03:38.000001Z"), "A major", 64, 0.52, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, fromChain, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z")).orElseThrow();

    assertEquals(fromChain.getId(), result.getChainId());
    assertEquals(Long.valueOf(6), result.getOffset());
    assertEquals("2014-08-12T14:03:38.000001Z", result.getBeginAt().toString());
  }

  @Test
  public void buildNextSegmentOrComplete_newEmptyChain() throws Exception {
    HubClientAccess access = HubClientAccess.create("Internal");
    test.put(Chain.create(account1, "Test Print #2", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    Chain fromChain = test.put(Chain.create()
      .setTypeEnum(ChainType.Production)
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setStopAt(null));

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, fromChain, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z")).orElseThrow();

    assertEquals(fromChain.getId(), result.getChainId());
    assertEquals(Long.valueOf(0), result.getOffset());
    assertEquals("2014-08-12T12:17:02.527142Z", result.getBeginAt().toString());
  }

  @Test
  public void destroy() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");

    subject.destroy(access, chain1.getId());

    try {
      subject.readOne(HubClientAccess.internal(), chain1.getId());
      fail();
    } catch (DAOExistenceException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void destroy_SucceedsEvenWithChildren() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Library.create(account1, "nerds", Instant.now());

    try {
      subject.destroy(access, chain1.getId());

    } catch (Exception e) {
      Chain result = subject.readOne(HubClientAccess.internal(), chain1.getId());
      assertNotNull(result);
      throw e;
    }
  }

}
