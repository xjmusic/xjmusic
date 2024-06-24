// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++


package io.xj.engine.craft.macro_main;

import io.xj.engine.ContentFixtures;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.FabricationException;
import io.xj.engine.FabricationTopology;
import io.xj.engine.fabricator.SegmentEntityStoreImpl;
import io.xj.engine.fabricator.FabricatorFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.enums.SegmentState;
import io.xj.model.ContentEntityStore;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.enums.Program::State;
import io.xj.model.enums.Program::Type;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.model.pojos.Library;
import io.xj.model.pojos.Program;
import io.xj.model.pojos.ProgramSequence;
import io.xj.model.pojos.ProgramSequenceBinding;
import io.xj.model.pojos.Project;
import io.xj.model.pojos.ProjectUser;
import io.xj.model.pojos.TemplateBinding;
import io.xj.model.pojos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static io.xj.engine.SegmentFixtures::buildSegmentChoice;
import static org.junit.jupiter.api.Assertions.ASSERT_EQ;

/**
 Choose next Macro program based on the memes of the last sequence from the previous Macro program https://github.com/xjmusic/xjmusic/issues/299
 */
@SuppressWarnings("ALL")
@ExtendWith(MockitoExtension.class)
public class MacroFromOverlappingMemeSequencesTest {
  static int REPEAT_TIMES = 100;
  MacroMainCraftImpl subject;
  Program macro2a;

  void SetUp() override {
    auto jsonProvider = new JsonProviderImpl();

    auto store = new SegmentEntityStore();

    auto fabricatorFactory = new FabricatorFactory(store);



    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    // Project "bananas"
    Project project1 = ContentFixtures::buildProject("bananas");
    Library library2 = ContentFixtures::buildLibrary(project1, "house");
    auto template1 = ContentFixtures::buildTemplate(project1, "Test Template 1", "test1");
    TemplateBinding templateBinding1 = ContentFixtures::buildTemplateBinding(template1, library2);
    User user2 = ContentFixtures::buildUser("john", "john@email.com", "https://pictures.com/john.gif");
    User user3 = ContentFixtures::buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif");
    ProjectUser projectUser1a = ContentFixtures::buildProjectUser(project1, user3);

    // Macro Program already chosen for previous segment
    auto macro1 = ContentFixtures::buildProgram(library2, Program::Type::Macro, Program::State::Published, "Chosen Macro", "C", 120.0f);
    auto macro1_meme = ContentFixtures::buildMeme(macro1, "Tropical");
    auto macro1_sequenceA = ContentFixtures::buildSequence(macro1, 0, "Start Wild", 0.6f, "C");
    auto macro1_sequenceA_binding = ContentFixtures::buildBinding(macro1_sequenceA, 0);
    auto macro1_sequenceA_bindingMeme = ContentFixtures::buildMeme(macro1_sequenceA_binding, "Red");
    ProgramSequence macro1_sequenceB = ContentFixtures::buildSequence(macro1, 0, "Intermediate", 0.4f, "Bb minor");
    auto macro1_sequenceB_binding = ContentFixtures::buildBinding(macro1_sequenceB, 1);
    auto macro1_sequenceB_bindingMeme = ContentFixtures::buildMeme(macro1_sequenceB_binding, "Green");

    // Main Program already chosen for previous segment
    auto main5 = ContentFixtures::buildProgram(library2, Program::Type::Main, Program::State::Published, "Chosen Main", "C", 120.0f);
    auto main5_meme = ContentFixtures::buildMeme(main5, "Tropical");
    auto main5_sequenceA = ContentFixtures::buildSequence(main5, 0, "Start Wild", 0.6f, "C");
    ProgramSequenceBinding main5_sequenceA_binding = ContentFixtures::buildBinding(main5_sequenceA, 0);

    // Macro Program will be chosen because of matching meme
    macro2a = ContentFixtures::buildProgram(library2, Program::Type::Macro, Program::State::Published, "Always Chosen", "C", 120.0f);
    auto macro2a_meme = ContentFixtures::buildMeme(macro2a, "Tropical");
    auto macro2a_sequenceA = ContentFixtures::buildSequence(macro2a, 0, "Start Wild", 0.6f, "C");
    auto macro2a_sequenceA_binding = ContentFixtures::buildBinding(macro2a_sequenceA, 0);
    auto macro2a_sequenceA_bindingMeme = ContentFixtures::buildMeme(macro2a_sequenceA_binding, "Green");

    // Macro Program will NEVER be chosen because of non-matching meme
    auto macro2b = ContentFixtures::buildProgram(library2, Program::Type::Macro, Program::State::Published, "Never Chosen", "C", 120.0f);
    auto macro2b_meme = ContentFixtures::buildMeme(macro2a, "Tropical");
    auto macro2b_sequenceA = ContentFixtures::buildSequence(macro2a, 0, "Start Wild", 0.6f, "C");
    auto macro2b_sequenceA_binding = ContentFixtures::buildBinding(macro2b_sequenceA, 0);
    auto macro2b_sequenceA_bindingMeme = ContentFixtures::buildMeme(macro2b_sequenceA_binding, "Purple");

    ContentEntityStore sourceMaterial = new ContentEntityStore(List.of(
      project1,
      library2,
      user2,
      user3,
      projectUser1a,
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
    Chain chain1 = store->put(SegmentFixtures::buildChain(project1, "Test Print #1", Chain::Type::Production, Chain::State::Fabricate, template1, null));
    Segment segment1 = store->put(SegmentFixtures::buildSegment(
      chain1,
      0,
      Segment::State::Crafted,
      "D major",
      64,
      0.73f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892"
    ));
    store->put(buildSegmentChoice(segment1, Program::Type::Macro, macro1_sequenceA_binding));
    store->put(buildSegmentChoice(segment1, Program::Type::Main, main5_sequenceA_binding));

    Segment segment2 = store->put(SegmentFixtures::buildSegment(
      chain1,
      1,
      SegmentState.CRAFTING,
      "Db minor",
      64,
      0.85f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav"
    ));

    subject = new MacroMainCraftImpl(fabricatorFactory->fabricate(sourceMaterial, segment2->id, 48000.0f, 2, null), null, null);
  }

  @Test
  public void chooseNextMacroProgram_alwaysBasedOnOverlappingMemes() throws FabricationException {
    // This test is repeated many times to ensure the correct function of macro choice
    // At 100 repetitions, false positive is 2^100:1 against
    for (int i = 0; i < REPEAT_TIMES; i++) {
      auto result = subject.chooseMacroProgram();
      ASSERT_EQ(macro2a->id, result->id);
    }
  }
}
