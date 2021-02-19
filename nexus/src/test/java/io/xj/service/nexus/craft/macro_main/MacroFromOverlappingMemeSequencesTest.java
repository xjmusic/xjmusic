package io.xj.service.nexus.craft.macro_main;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.Account;
import io.xj.AccountUser;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Library;
import io.xj.Program;
import io.xj.ProgramSequence;
import io.xj.ProgramSequenceBinding;
import io.xj.Segment;
import io.xj.User;
import io.xj.UserRole;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.fabricator.FabricatorFactory;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.service.nexus.testing.NexusTestConfiguration;
import io.xj.service.nexus.work.NexusWorkModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.UUID;

import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildAccount;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildAccountUser;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildLibrary;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgram;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgramMeme;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgramSequence;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgramSequenceBinding;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgramSequenceBindingMeme;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildUser;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildUserRole;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 [#176728582] Choose next Macro program based on the memes of the last sequence from the previous Macro program
 */
@RunWith(MockitoJUnitRunner.class)
public class MacroFromOverlappingMemeSequencesTest {
  private MacroMainCraftImpl subject;
  private static final int REPEAT_TIMES = 100;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Mock
  public HubClient hubClient;

  // Fake entities
  private Program macro2a;
  private ProgramSequence macro1_sequenceB;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault()
      .withValue("program.doTranspose", ConfigValueFactory.fromAnyRef(true))
      .withValue("instrument.isTonal", ConfigValueFactory.fromAnyRef(true));
    Injector injector = AppConfiguration.inject(config,
      ImmutableSet.of(Modules.override(new NexusWorkModule())
        .with(new AbstractModule() {
          @Override
          public void configure() {
            bind(HubClient.class).toInstance(hubClient);
          }
        })));
    FabricatorFactory fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    NexusEntityStore store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    // Account "bananas"
    Account account1 = buildAccount("bananas");
    Library library2 = buildLibrary(account1, "house");
    User user2 = buildUser("john", "john@email.com", "http://pictures.com/john.gif");
    UserRole userRole2a = buildUserRole(user2, UserRole.Type.Admin);
    User user3 = buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    UserRole userRole3a = buildUserRole(user3, UserRole.Type.User);
    AccountUser accountUser1a = buildAccountUser(account1, user3);

    // Macro Program already chosen for previous segment
    var macro1 = buildProgram(library2, Program.Type.Macro, Program.State.Published, "Chosen Macro", "C", 120.0, 0.6);
    var macro1_meme = buildProgramMeme(macro1, "Tropical");
    var macro1_sequenceA = buildProgramSequence(macro1, 0, "Start Wild", 0.6, "C", 125.0);
    var macro1_sequenceA_binding = buildProgramSequenceBinding(macro1_sequenceA, 0);
    var macro1_sequenceA_bindingMeme = buildProgramSequenceBindingMeme(macro1_sequenceA_binding, "Red");
    macro1_sequenceB = buildProgramSequence(macro1, 0, "Intermediate", 0.4, "Bb minor", 115.0);
    var macro1_sequenceB_binding = buildProgramSequenceBinding(macro1_sequenceB, 1);
    var macro1_sequenceB_bindingMeme = buildProgramSequenceBindingMeme(macro1_sequenceB_binding, "Green");

    // Main Program already chosen for previous segment
    var main5 = buildProgram(library2, Program.Type.Main, Program.State.Published, "Chosen Main", "C", 120.0, 0.6);
    var main5_meme = buildProgramMeme(main5, "Tropical");
    var main5_sequenceA = buildProgramSequence(main5, 0, "Start Wild", 0.6, "C", 125.0);
    ProgramSequenceBinding main5_sequenceA_binding = buildProgramSequenceBinding(main5_sequenceA, 0);

    // Macro Program will be chosen because of matching meme
    macro2a = buildProgram(library2, Program.Type.Macro, Program.State.Published, "Always Chosen", "C", 120.0, 0.6);
    var macro2a_meme = buildProgramMeme(macro2a, "Tropical");
    var macro2a_sequenceA = buildProgramSequence(macro2a, 0, "Start Wild", 0.6, "C", 125.0);
    var macro2a_sequenceA_binding = buildProgramSequenceBinding(macro2a_sequenceA, 0);
    var macro2a_sequenceA_bindingMeme = buildProgramSequenceBindingMeme(macro2a_sequenceA_binding, "Green");

    // Macro Program will NEVER be chosen because of non-matching meme
    var macro2b = buildProgram(library2, Program.Type.Macro, Program.State.Published, "Never Chosen", "C", 120.0, 0.6);
    var macro2b_meme = buildProgramMeme(macro2a, "Tropical");
    var macro2b_sequenceA = buildProgramSequence(macro2a, 0, "Start Wild", 0.6, "C", 125.0);
    var macro2b_sequenceA_binding = buildProgramSequenceBinding(macro2b_sequenceA, 0);
    var macro2b_sequenceA_bindingMeme = buildProgramSequenceBindingMeme(macro2b_sequenceA_binding, "Purple");

    when(hubClient.ingest(any(), any(), any(), any())).thenReturn(new HubContent(ImmutableList.of(
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
    )));

    // Chain "Test Print #1" has 5 total segments
    Chain chain1 = store.put(buildChain(account1, "Test Print #1", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setTargetId(library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());
    Segment segment1 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(0)
      .setState(Segment.State.Dubbed)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z")
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120)
      .setStorageKey("chains-1-segments-9f7s89d8a7892")
      .setOutputEncoder("wav")
      .build());
    store.put(buildSegmentChoice(segment1, Program.Type.Macro, macro1_sequenceA_binding));
    store.put(buildSegmentChoice(segment1, Program.Type.Main, main5_sequenceA_binding));

    Segment segment2 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(1)
      .setState(Segment.State.Dubbing)
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setEndAt("2017-02-14T12:02:04.000001Z")
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());

    subject = new MacroMainCraftImpl(fabricatorFactory.fabricate(HubClientAccess.internal(), segment2));
  }

  @Test
  public void chooseNextMacroProgram_alwaysBasedOnOverlappingMemes() {
    // This test is repeated many times to ensure the correct function of macro choice
    // At 100 repetitions, false positive is 2^100:1 against
    for (int i = 0; i < REPEAT_TIMES; i++) {
      var result = subject.chooseNextMacroProgram().orElseThrow();
      assertEquals(macro2a.getId(), result.getId());
    }
  }
}
