package io.xj.nexus.craft.macro_main;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.api.Account;
import io.xj.api.AccountUser;
import io.xj.api.Chain;
import io.xj.api.ChainBinding;
import io.xj.api.ChainBindingType;
import io.xj.api.ChainState;
import io.xj.api.ChainType;
import io.xj.api.Library;
import io.xj.api.Program;
import io.xj.api.ProgramSequence;
import io.xj.api.ProgramSequenceBinding;
import io.xj.api.ProgramState;
import io.xj.api.ProgramType;
import io.xj.api.Segment;
import io.xj.api.SegmentState;
import io.xj.api.User;
import io.xj.api.UserRole;
import io.xj.api.UserRoleType;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.testing.NexusTestConfiguration;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 [#176728582] Choose next Macro program based on the memes of the last sequence from the previous Macro program
 */
@SuppressWarnings("ALL")
@RunWith(MockitoJUnitRunner.class)
public class MacroFromOverlappingMemeSequencesTest {
  private static final int REPEAT_TIMES = 100;
  @Mock
  public HubClient hubClient;
  @Mock
  public ApiUrlProvider apiUrlProvider;
  private MacroMainCraftImpl subject;
  private Program macro2a;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault()
      .withValue("program.doTranspose", ConfigValueFactory.fromAnyRef(true))
      .withValue("instrument.isTonal", ConfigValueFactory.fromAnyRef(true));
    Environment env = Environment.getDefault();
    Injector injector = AppConfiguration.inject(config, env,
      ImmutableSet.of(Modules.override(new NexusWorkModule())
        .with(new AbstractModule() {
          @Override
          public void configure() {
            bind(HubClient.class).toInstance(hubClient);
          }
        })));
    FabricatorFactory fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    Topology.buildHubApiTopology(entityFactory);
    Topology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    NexusEntityStore store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    // Account "bananas"
    Account account1 = NexusIntegrationTestingFixtures.makeAccount("bananas");
    Library library2 = NexusIntegrationTestingFixtures.makeLibrary(account1, "house");
    User user2 = NexusIntegrationTestingFixtures.makeUser("john", "john@email.com", "http://pictures.com/john.gif");
    UserRole userRole2a = NexusIntegrationTestingFixtures.makeUserRole(user2, UserRoleType.ADMIN);
    User user3 = NexusIntegrationTestingFixtures.makeUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    UserRole userRole3a = NexusIntegrationTestingFixtures.makeUserRole(user3, UserRoleType.USER);
    AccountUser accountUser1a = NexusIntegrationTestingFixtures.makeAccountUser(account1, user3);

    // Macro Program already chosen for previous segment
    var macro1 = NexusIntegrationTestingFixtures.makeProgram(library2, ProgramType.MACRO, ProgramState.PUBLISHED, "Chosen Macro", "C", 120.0, 0.6);
    var macro1_meme = NexusIntegrationTestingFixtures.makeMeme(macro1, "Tropical");
    var macro1_sequenceA = NexusIntegrationTestingFixtures.makeSequence(macro1, 0, "Start Wild", 0.6, "C", 125.0);
    var macro1_sequenceA_binding = NexusIntegrationTestingFixtures.makeBinding(macro1_sequenceA, 0);
    var macro1_sequenceA_bindingMeme = NexusIntegrationTestingFixtures.makeMeme(macro1_sequenceA_binding, "Red");
    ProgramSequence macro1_sequenceB = NexusIntegrationTestingFixtures.makeSequence(macro1, 0, "Intermediate", 0.4, "Bb minor", 115.0);
    var macro1_sequenceB_binding = NexusIntegrationTestingFixtures.makeBinding(macro1_sequenceB, 1);
    var macro1_sequenceB_bindingMeme = NexusIntegrationTestingFixtures.makeMeme(macro1_sequenceB_binding, "Green");

    // Main Program already chosen for previous segment
    var main5 = NexusIntegrationTestingFixtures.makeProgram(library2, ProgramType.MAIN, ProgramState.PUBLISHED, "Chosen Main", "C", 120.0, 0.6);
    var main5_meme = NexusIntegrationTestingFixtures.makeMeme(main5, "Tropical");
    var main5_sequenceA = NexusIntegrationTestingFixtures.makeSequence(main5, 0, "Start Wild", 0.6, "C", 125.0);
    ProgramSequenceBinding main5_sequenceA_binding = NexusIntegrationTestingFixtures.makeBinding(main5_sequenceA, 0);

    // Macro Program will be chosen because of matching meme
    macro2a = NexusIntegrationTestingFixtures.makeProgram(library2, ProgramType.MACRO, ProgramState.PUBLISHED, "Always Chosen", "C", 120.0, 0.6);
    var macro2a_meme = NexusIntegrationTestingFixtures.makeMeme(macro2a, "Tropical");
    var macro2a_sequenceA = NexusIntegrationTestingFixtures.makeSequence(macro2a, 0, "Start Wild", 0.6, "C", 125.0);
    var macro2a_sequenceA_binding = NexusIntegrationTestingFixtures.makeBinding(macro2a_sequenceA, 0);
    var macro2a_sequenceA_bindingMeme = NexusIntegrationTestingFixtures.makeMeme(macro2a_sequenceA_binding, "Green");

    // Macro Program will NEVER be chosen because of non-matching meme
    var macro2b = NexusIntegrationTestingFixtures.makeProgram(library2, ProgramType.MACRO, ProgramState.PUBLISHED, "Never Chosen", "C", 120.0, 0.6);
    var macro2b_meme = NexusIntegrationTestingFixtures.makeMeme(macro2a, "Tropical");
    var macro2b_sequenceA = NexusIntegrationTestingFixtures.makeSequence(macro2a, 0, "Start Wild", 0.6, "C", 125.0);
    var macro2b_sequenceA_binding = NexusIntegrationTestingFixtures.makeBinding(macro2b_sequenceA, 0);
    var macro2b_sequenceA_bindingMeme = NexusIntegrationTestingFixtures.makeMeme(macro2b_sequenceA_binding, "Purple");

    HubContent sourceMaterial = new HubContent(ImmutableList.of(
      account1,
      library2,
      user2,
      userRole2a,
      user3,
      userRole3a,
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
      macro2b_sequenceA_bindingMeme
    ));

    // Chain "Test Print #1" has 5 total segments
    Chain chain1 = store.put(NexusIntegrationTestingFixtures.makeChain(account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(new ChainBinding()
      .id(UUID.randomUUID())
      .chainId(chain1.getId())
      .targetId(library2.getId())
      .type(ChainBindingType.LIBRARY)
      );
    Segment segment1 = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain1.getId())
      .offset(0L)
      .state(SegmentState.DUBBED)
      .beginAt("2017-02-14T12:01:00.000001Z")
      .endAt("2017-02-14T12:01:32.000001Z")
      .key("D major")
      .total(64)
      .density(0.73)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892")
      .outputEncoder("wav")
      );
    store.put(NexusIntegrationTestingFixtures.makeSegmentChoice(segment1, ProgramType.MACRO, macro1_sequenceA_binding));
    store.put(NexusIntegrationTestingFixtures.makeSegmentChoice(segment1, ProgramType.MAIN, main5_sequenceA_binding));

    Segment segment2 = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain1.getId())
      .offset(1L)
      .state(SegmentState.DUBBING)
      .beginAt("2017-02-14T12:01:32.000001Z")
      .endAt("2017-02-14T12:02:04.000001Z")
      .key("Db minor")
      .total(64)
      .density(0.85)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      );

    subject = new MacroMainCraftImpl(fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment2), apiUrlProvider);
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
