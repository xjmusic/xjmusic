// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.craft.macro_main;

import io.xj.hub.HubTopology;
import io.xj.nexus.hub_client.HubClient;
import io.xj.hub.ingest.HubContent;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.tables.pojos.User;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.notification.NotificationProvider;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManagerImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildAccount;
import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildAccountUser;
import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildBinding;
import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildLibrary;
import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildMeme;
import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildProgram;
import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildSequence;
import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildTemplate;
import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildTemplateBinding;
import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildUser;
import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static org.junit.Assert.assertEquals;

/**
 * Choose next Macro program based on the memes of the last sequence from the previous Macro program https://www.pivotaltracker.com/story/show/176728582
 */
@SuppressWarnings("ALL")
@RunWith(MockitoJUnitRunner.class)
public class MacroFromOverlappingMemeSequencesTest {
  static final int REPEAT_TIMES = 100;
  @Mock
  public HubClient hubClient;
  @Mock
  public ApiUrlProvider apiUrlProvider;
  @Mock
  public NotificationProvider notificationProvider;
  MacroMainCraftImpl subject;
  Program macro2a;

  @Before
  public void setUp() throws Exception {
    var jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    var store = new NexusEntityStoreImpl(entityFactory);
    var segmentManager = new SegmentManagerImpl(entityFactory, store);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    EntityStore entityStore = new EntityStoreImpl();
    var fabricatorFactory = new FabricatorFactoryImpl(
      segmentManager,
      jsonapiPayloadFactory,
      jsonProvider
    );
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    // Account "bananas"
    Account account1 = buildAccount("bananas");
    Library library2 = buildLibrary(account1, "house");
    var template1 = buildTemplate(account1, "Test Template 1", "test1");
    TemplateBinding templateBinding1 = buildTemplateBinding(template1, library2);
    User user2 = buildUser("john", "john@email.com", "https://pictures.com/john.gif", "User");
    User user3 = buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User");
    AccountUser accountUser1a = buildAccountUser(account1, user3);

    // Macro Program already chosen for previous segment
    var macro1 = buildProgram(library2, ProgramType.Macro, ProgramState.Published, "Chosen Macro", "C", 120.0f, 0.6f);
    var macro1_meme = buildMeme(macro1, "Tropical");
    var macro1_sequenceA = buildSequence(macro1, 0, "Start Wild", 0.6f, "C");
    var macro1_sequenceA_binding = buildBinding(macro1_sequenceA, 0);
    var macro1_sequenceA_bindingMeme = buildMeme(macro1_sequenceA_binding, "Red");
    ProgramSequence macro1_sequenceB = buildSequence(macro1, 0, "Intermediate", 0.4f, "Bb minor");
    var macro1_sequenceB_binding = buildBinding(macro1_sequenceB, 1);
    var macro1_sequenceB_bindingMeme = buildMeme(macro1_sequenceB_binding, "Green");

    // Main Program already chosen for previous segment
    var main5 = buildProgram(library2, ProgramType.Main, ProgramState.Published, "Chosen Main", "C", 120.0f, 0.6f);
    var main5_meme = buildMeme(main5, "Tropical");
    var main5_sequenceA = buildSequence(main5, 0, "Start Wild", 0.6f, "C");
    ProgramSequenceBinding main5_sequenceA_binding = buildBinding(main5_sequenceA, 0);

    // Macro Program will be chosen because of matching meme
    macro2a = buildProgram(library2, ProgramType.Macro, ProgramState.Published, "Always Chosen", "C", 120.0f, 0.6f);
    var macro2a_meme = buildMeme(macro2a, "Tropical");
    var macro2a_sequenceA = buildSequence(macro2a, 0, "Start Wild", 0.6f, "C");
    var macro2a_sequenceA_binding = buildBinding(macro2a_sequenceA, 0);
    var macro2a_sequenceA_bindingMeme = buildMeme(macro2a_sequenceA_binding, "Green");

    // Macro Program will NEVER be chosen because of non-matching meme
    var macro2b = buildProgram(library2, ProgramType.Macro, ProgramState.Published, "Never Chosen", "C", 120.0f, 0.6f);
    var macro2b_meme = buildMeme(macro2a, "Tropical");
    var macro2b_sequenceA = buildSequence(macro2a, 0, "Start Wild", 0.6f, "C");
    var macro2b_sequenceA_binding = buildBinding(macro2b_sequenceA, 0);
    var macro2b_sequenceA_bindingMeme = buildMeme(macro2b_sequenceA_binding, "Purple");

    HubContent sourceMaterial = new HubContent(List.of(
      account1,
      library2,
      user2,
      user3,
      accountUser1a,
      macro1,
      macro1_meme,
      macro1_sequenceA,
      macro1_sequenceA_binding,
      macro1_sequenceA_bindingMeme,
      macro1_sequenceB,
      macro1_sequenceB_binding,
      macro1_sequenceB_bindingMeme,
      main5,
      main5_meme,
      main5_sequenceA,
      main5_sequenceA_binding,
      macro2a,
      macro2a_meme,
      macro2a_sequenceA,
      macro2a_sequenceA_binding,
      macro2a_sequenceA_bindingMeme,
      macro2b,
      macro2b_meme,
      macro2b_sequenceA,
      macro2b_sequenceA_binding,
      macro2b_sequenceA_bindingMeme,
      template1,
      templateBinding1
    ));

    // Chain "Test Print #1" has 5 total segments
    Chain chain1 = store.put(buildChain(account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, template1, null));
    Segment segment1 = store.put(buildSegment(
      chain1,
      0,
      SegmentState.CRAFTED,
      "D major",
      64,
      0.73,
      120.0,
      "chains-1-segments-9f7s89d8a7892"
    ));
    store.put(buildSegmentChoice(segment1, ProgramType.Macro, macro1_sequenceA_binding));
    store.put(buildSegmentChoice(segment1, ProgramType.Main, main5_sequenceA_binding));

    Segment segment2 = store.put(buildSegment(
      chain1,
      1,
      SegmentState.CRAFTING,
      "Db minor",
      64,
      0.85,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav"
    ));

    subject = new MacroMainCraftImpl(fabricatorFactory.fabricate(sourceMaterial, segment2), apiUrlProvider);
  }

  @Test
  public void chooseNextMacroProgram_alwaysBasedOnOverlappingMemes() throws NexusException {
    // This test is repeated many times to ensure the correct function of macro choice
    // At 100 repetitions, false positive is 2^100:1 against
    for (int i = 0; i < REPEAT_TIMES; i++) {
      var result = subject.chooseNextMacroProgram().orElseThrow();
      assertEquals(String.format("Run #%s OK", i), macro2a.getId(), result.getId());
    }
  }
}
