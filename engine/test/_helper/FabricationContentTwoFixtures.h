// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATION_CONTENT_TWO_FIXTURES_H
#define XJMUSIC_FABRICATION_CONTENT_TWO_FIXTURES_H

#include <chrono>
#include <iomanip>
#include <random>
#include <sstream>
#include <utility>

#include "ContentTestHelper.h"
#include "LoremIpsum.h"
#include "FabricationContentOneFixtures.h"
#include "xjmusic/entities/content/Instrument.h"
#include "xjmusic/entities/content/InstrumentAudio.h"
#include "xjmusic/entities/content/InstrumentMeme.h"
#include "xjmusic/entities/content/Library.h"
#include "xjmusic/entities/content/Program.h"
#include "xjmusic/entities/content/ProgramMeme.h"
#include "xjmusic/entities/content/ProgramSequence.h"
#include "xjmusic/entities/content/ProgramSequenceBinding.h"
#include "xjmusic/entities/content/ProgramSequenceBindingMeme.h"
#include "xjmusic/entities/content/ProgramSequenceChord.h"
#include "xjmusic/entities/content/ProgramSequenceChordVoicing.h"
#include "xjmusic/entities/content/ProgramSequencePattern.h"
#include "xjmusic/entities/content/ProgramSequencePatternEvent.h"
#include "xjmusic/entities/content/ProgramVoice.h"
#include "xjmusic/entities/content/ProgramVoiceTrack.h"
#include "xjmusic/entities/content/Project.h"
#include "xjmusic/entities/content/Template.h"
#include "xjmusic/entities/content/TemplateBinding.h"
#include "xjmusic/entities/content/TemplateConfig.h"
#include "xjmusic/entities/segment/Chain.h"
#include "xjmusic/util/StringUtils.h"
#include "xjmusic/entities/segment/Segment.h"
#include "xjmusic/util/ValueUtils.h"
#include "xjmusic/entities/segment/SegmentChoice.h"
#include "xjmusic/entities/segment/SegmentMeta.h"
#include "xjmusic/entities/segment/SegmentMeme.h"
#include "xjmusic/entities/segment/SegmentChord.h"
#include "xjmusic/entities/segment/SegmentChordVoicing.h"
#include "xjmusic/entities/segment/SegmentChoiceArrangement.h"
#include "xjmusic/entities/segment/SegmentChoiceArrangementPick.h"

namespace XJ {

/**
 Integration tests use shared scenario fixtures as much as possible https://github.com/xjmusic/workstation/issues/202
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought to be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
  class FabricationContentTwoFixtures {
    static constexpr float RANDOM_VALUE_FROM = 0.3f;
    static constexpr float RANDOM_VALUE_TO = 0.8f;

  protected:

    /**
     List of N random values
  
     @param N number of values
     @return array of values
     */
    static std::vector<float> listOfRandomValues(int N);

    /**
     Create an N-magnitude list of unique Strings at random of a source list of Strings
  
     @param N           size of list
     @param sourceItems source Strings
     @return array of unique random Strings
     */
    static std::vector<std::string> listOfUniqueRandom(long N, std::vector<std::string> sourceItems);

    /**
     Random value between A and B
  
     @param A floor
     @param B ceiling
     @return A <= value <= B
     */
    static float random(float A, float B);

    /**
     Get random std::string of array
  
     @param array to get std::string of
     @return random std::string
     */
    static std::string random(std::vector<std::string> array);

    /**
     Get random long of array
  
     @param array to get long of
     @return random long
     */
    static int random(std::vector<int> array);

  public:

    // Test data
    Project project1;
    Instrument instrument8;
    Instrument instrument9;
    InstrumentAudio instrument8_audio8bleep;
    InstrumentAudio instrument8_audio8kick;
    InstrumentAudio instrument8_audio8snare;
    InstrumentAudio instrument8_audio8toot;
    InstrumentAudio instrument9_audio8;
    InstrumentMeme instrument8_meme0;
    InstrumentMeme instrument9_meme0;
    Library library1;
    Library library2;
    Program program10;
    Program program15;
    Program program35;
    Program program3;
    Program program4;
    Program program5;
    Program program9;
    ProgramMeme program10_meme0;
    ProgramMeme program15_meme0;
    ProgramMeme program35_meme0;
    ProgramMeme program3_meme0;
    ProgramMeme program4_meme0;
    ProgramMeme program5_meme0;
    ProgramMeme program9_meme0;
    ProgramSequence program10_sequence0;
    ProgramSequence program15_sequence0;
    ProgramSequence program15_sequence1;
    ProgramSequence program35_sequence0;
    ProgramSequence program3_sequence0;
    ProgramSequence program3_sequence1;
    ProgramSequence program4_sequence0;
    ProgramSequence program4_sequence1;
    ProgramSequence program4_sequence2;
    ProgramSequence program5_sequence0;
    ProgramSequence program5_sequence1;
    ProgramSequence program9_sequence0;
    ProgramSequenceBinding program15_sequence0_binding0;
    ProgramSequenceBinding program15_sequence1_binding0;
    ProgramSequenceBinding program3_sequence0_binding0;
    ProgramSequenceBinding program3_sequence1_binding0;
    ProgramSequenceBinding program4_sequence0_binding0;
    ProgramSequenceBinding program4_sequence1_binding0;
    ProgramSequenceBinding program4_sequence2_binding0;
    ProgramSequenceBinding program5_sequence0_binding0;
    ProgramSequenceBinding program5_sequence1_binding0;
    ProgramSequenceBinding program5_sequence1_binding1;
    ProgramSequenceBindingMeme program15_sequence0_binding0_meme0;
    ProgramSequenceBindingMeme program15_sequence1_binding0_meme0;
    ProgramSequenceBindingMeme program15_sequence1_binding0_meme1;
    ProgramSequenceBindingMeme program3_sequence0_binding0_meme0;
    ProgramSequenceBindingMeme program3_sequence1_binding0_meme0;
    ProgramSequenceBindingMeme program4_sequence0_binding0_meme0;
    ProgramSequenceBindingMeme program4_sequence1_binding0_meme0;
    ProgramSequenceBindingMeme program4_sequence1_binding0_meme1;
    ProgramSequenceBindingMeme program4_sequence2_binding0_meme0;
    ProgramSequenceBindingMeme program5_sequence0_binding0_meme0;
    ProgramSequenceBindingMeme program5_sequence1_binding0_meme0;
    ProgramSequenceBindingMeme program5_sequence1_binding1_meme0;
    ProgramSequenceChord program15_sequence0_chord0;
    ProgramSequenceChord program15_sequence0_chord1;
    ProgramSequenceChord program15_sequence1_chord0;
    ProgramSequenceChord program15_sequence1_chord1;
    ProgramSequenceChord program5_sequence0_chord0;
    ProgramSequenceChord program5_sequence0_chord1;
    ProgramSequenceChord program5_sequence0_chord2;
    ProgramSequenceChord program5_sequence1_chord0;
    ProgramSequenceChord program5_sequence1_chord1;
    ProgramSequenceChordVoicing program15_sequence0_chord0_voicing;
    ProgramSequenceChordVoicing program15_sequence0_chord1_voicing;
    ProgramSequenceChordVoicing program15_sequence1_chord0_voicing;
    ProgramSequenceChordVoicing program15_sequence1_chord1_voicing;
    ProgramSequenceChordVoicing program5_sequence0_chord0_voicing;
    ProgramSequenceChordVoicing program5_sequence0_chord1_voicing;
    ProgramSequenceChordVoicing program5_sequence0_chord2_voicing;
    ProgramSequenceChordVoicing program5_sequence1_chord0_voicing;
    ProgramSequenceChordVoicing program5_sequence1_chord1_voicing;
    ProgramSequencePattern program10_sequence0_pattern0;
    ProgramSequencePattern program10_sequence0_pattern1;
    ProgramSequencePattern program10_sequence0_pattern2;
    ProgramSequencePattern program10_sequence0_pattern3;
    ProgramSequencePattern program35_sequence0_pattern0;
    ProgramSequencePattern program35_sequence0_pattern1;
    ProgramSequencePattern program9_sequence0_pattern0;
    ProgramSequencePattern program9_sequence0_pattern1;
    ProgramSequencePattern program9_sequence0_pattern2;
    ProgramSequencePattern program9_sequence0_pattern3;
    ProgramSequencePatternEvent program10_sequence0_pattern0_event0;
    ProgramSequencePatternEvent program10_sequence0_pattern0_event1;
    ProgramSequencePatternEvent program10_sequence0_pattern0_event2;
    ProgramSequencePatternEvent program10_sequence0_pattern0_event3;
    ProgramSequencePatternEvent program10_sequence0_pattern1_event0;
    ProgramSequencePatternEvent program10_sequence0_pattern1_event1;
    ProgramSequencePatternEvent program10_sequence0_pattern1_event2;
    ProgramSequencePatternEvent program10_sequence0_pattern1_event3;
    ProgramSequencePatternEvent program10_sequence0_pattern2_event0;
    ProgramSequencePatternEvent program10_sequence0_pattern2_event1;
    ProgramSequencePatternEvent program10_sequence0_pattern2_event2;
    ProgramSequencePatternEvent program10_sequence0_pattern2_event3;
    ProgramSequencePatternEvent program10_sequence0_pattern3_event0;
    ProgramSequencePatternEvent program10_sequence0_pattern3_event1;
    ProgramSequencePatternEvent program10_sequence0_pattern3_event2;
    ProgramSequencePatternEvent program10_sequence0_pattern3_event3;
    ProgramSequencePatternEvent program35_sequence0_pattern0_event0;
    ProgramSequencePatternEvent program35_sequence0_pattern0_event1;
    ProgramSequencePatternEvent program35_sequence0_pattern0_event2;
    ProgramSequencePatternEvent program35_sequence0_pattern0_event3;
    ProgramSequencePatternEvent program35_sequence0_pattern1_event0;
    ProgramSequencePatternEvent program35_sequence0_pattern1_event1;
    ProgramSequencePatternEvent program35_sequence0_pattern1_event2;
    ProgramSequencePatternEvent program35_sequence0_pattern1_event3;
    ProgramSequencePatternEvent program9_sequence0_pattern0_event0;
    ProgramSequencePatternEvent program9_sequence0_pattern0_event1;
    ProgramSequencePatternEvent program9_sequence0_pattern0_event2;
    ProgramSequencePatternEvent program9_sequence0_pattern0_event3;
    ProgramSequencePatternEvent program9_sequence0_pattern1_event0;
    ProgramSequencePatternEvent program9_sequence0_pattern1_event1;
    ProgramSequencePatternEvent program9_sequence0_pattern1_event2;
    ProgramSequencePatternEvent program9_sequence0_pattern1_event3;
    ProgramSequencePatternEvent program9_sequence0_pattern2_event0;
    ProgramSequencePatternEvent program9_sequence0_pattern2_event1;
    ProgramSequencePatternEvent program9_sequence0_pattern2_event2;
    ProgramSequencePatternEvent program9_sequence0_pattern2_event3;
    ProgramSequencePatternEvent program9_sequence0_pattern3_event0;
    ProgramSequencePatternEvent program9_sequence0_pattern3_event1;
    ProgramSequencePatternEvent program9_sequence0_pattern3_event2;
    ProgramSequencePatternEvent program9_sequence0_pattern3_event3;
    ProgramVoice program10_voice0;
    ProgramVoice program15_voiceBass;
    ProgramVoice program35_voice0;
    ProgramVoice program5_voiceBass;
    ProgramVoice program5_voicePad;
    ProgramVoice program5_voiceSticky;
    ProgramVoice program5_voiceStripe;
    ProgramVoice program9_voice0;
    ProgramVoiceTrack program10_voice0_track0;
    ProgramVoiceTrack program35_voice0_track0;
    ProgramVoiceTrack program35_voice0_track1;
    ProgramVoiceTrack program35_voice0_track2;
    ProgramVoiceTrack program35_voice0_track3;
    ProgramVoiceTrack program9_voice0_track0;
    ProgramVoiceTrack program9_voice0_track10;
    ProgramVoiceTrack program9_voice0_track11;
    ProgramVoiceTrack program9_voice0_track12;
    ProgramVoiceTrack program9_voice0_track13;
    ProgramVoiceTrack program9_voice0_track14;
    ProgramVoiceTrack program9_voice0_track15;
    ProgramVoiceTrack program9_voice0_track1;
    ProgramVoiceTrack program9_voice0_track2;
    ProgramVoiceTrack program9_voice0_track3;
    ProgramVoiceTrack program9_voice0_track4;
    ProgramVoiceTrack program9_voice0_track5;
    ProgramVoiceTrack program9_voice0_track6;
    ProgramVoiceTrack program9_voice0_track7;
    ProgramVoiceTrack program9_voice0_track8;
    ProgramVoiceTrack program9_voice0_track9;
    Template template1;
    TemplateBinding templateBinding1;

    /**
     * Build a chain from a template
     */
    static Chain buildChain(
        const Template &tmpl
    );

    /**
     * Build a chain from a template
     */
    static Chain buildChain(
        const Template &tmpl,
        Chain::State state
    );

    /**
     * Build a chain from a template
     */
    static Chain buildChain(
        const Project &project,
        const std::string &name,
        Chain::Type type,
        Chain::State state,
        const Template &tmpl
    );

    /**
     * Build a chain from a template
     */
    static Chain buildChain(
        const Project &project,
        const Template &tmpl,
        const std::string &name,
        Chain::Type type,
        Chain::State state
    );

    /**
     * Build a chain from a template
     */
    static Chain buildChain(
        const Project &project,
        std::string name,
        Chain::Type type,
        Chain::State state,
        const Template &tmpl,
        const std::string &shipKey
    );

    /**
     * Build a Segment
     */
    static Segment buildSegment();

    /**
     * Build a Segment for a chain
     */
    static Segment buildSegment(
        Chain chain,
        int id,
        Segment::State state,
        std::string key,
        int total,
        float intensity,
        float tempo,
        std::string storageKey
    );

    /**
     * Build a Segment for a chain
     */
    static Segment buildSegment(
        const Chain &chain,
        Segment::Type type,
        int id,
        int delta,
        Segment::State state,
        std::string key,
        int total,
        float intensity,
        float tempo,
        std::string storageKey,
        bool hasEndSet
    );

    /**
     * Build a Segment for a chain
     */
    static Segment buildSegment(
        Chain chain,
        std::string key,
        int total,
        float intensity,
        float tempo
    );

    /**
     * Build a Segment for a chain
     */
    static Segment buildSegment(
        Chain chain,
        int offset,
        std::string key,
        int total,
        float intensity,
        float tempo
    );

    /**
     * Build a choice for a segment
     */
    static SegmentChoice buildSegmentChoice(
        Segment segment,
        Program::Type programType,
        ProgramSequenceBinding programSequenceBinding
    );

    /**
     * Build a choice for a segment
     */
    static SegmentChoice buildSegmentChoice(
        Segment segment,
        Program::Type programType,
        ProgramSequence programSequence
    );

    /**
     * Build a choice for a segment
     */
    static SegmentChoice buildSegmentChoice(
        Segment segment,
        int deltaIn,
        int deltaOut,
        Program program,
        Instrument::Type instrumentType,
        Instrument::Mode instrumentMode
    );

    /**
     * Build a choice for a segment
     */
    static SegmentChoice buildSegmentChoice(
        Segment segment,
        Program program
    );

    /**
     * Build a choice for a segment
     */
    static SegmentChoice buildSegmentChoice(
        Segment segment,
        Instrument instrument
    );

    /**
     * Build a meta for a segment
     */
    static SegmentMeta buildSegmentMeta(
        Segment segment,
        std::string key,
        std::string value
    );

    /**
     * Build a choice for a segment
     */
    static SegmentChoice buildSegmentChoice(
        Segment segment,
        Program program,
        ProgramSequence programSequence,
        ProgramVoice voice,
        Instrument instrument
    );

    /**
     * Build a choice of program and binding for a segment
     */
    static SegmentChoice buildSegmentChoice(
        Segment segment,
        int deltaIn,
        int deltaOut,
        Program program,
        ProgramSequenceBinding programSequenceBinding
    );

    /**
     * Build a choice of program, voice, and instrument for a segment
     */
    static SegmentChoice buildSegmentChoice(
        Segment segment,
        int deltaIn,
        int deltaOut,
        Program program,
        ProgramVoice voice,
        Instrument instrument
    );

    /**
     * Build a choice of program for a segment
     */
    static SegmentChoice buildSegmentChoice(
        Segment segment,
        int deltaIn,
        int deltaOut,
        Program program
    );

    /**
     * Build a meme for a segment
     */
    static SegmentMeme buildSegmentMeme(
        Segment segment,
        std::string name
    );

    /**
     * Build a chord for a segment
     */
    static SegmentChord buildSegmentChord(
        Segment segment,
        double atPosition,
        std::string name
    );

    /**
     * Build a voicing for a segment chord
     */
    static SegmentChordVoicing buildSegmentChordVoicing(
        SegmentChord chord,
        Instrument::Type type,
        std::string notes
    );

    /**
     * Build an arrangement for a segment choice
     */
    static SegmentChoiceArrangement buildSegmentChoiceArrangement(
        SegmentChoice segmentChoice
    );

    /**
     * Build a pick for a segment choice arrangement
     */
    static SegmentChoiceArrangementPick buildSegmentChoiceArrangementPick(
        Segment segment,
        SegmentChoiceArrangement segmentChoiceArrangement,
        InstrumentAudio instrumentAudio,
        std::string pickEvent
    );

    /**
     * Build a pick for a segment choice arrangement
     */
    static SegmentChoiceArrangementPick buildSegmentChoiceArrangementPick(
        Segment segment,
        SegmentChoiceArrangement segmentChoiceArrangement,
        ProgramSequencePatternEvent event,
        InstrumentAudio instrumentAudio,
        std::string pickEvent
    );

    /**
     A whole library of mock content
  
     @return collection of entities
     */
    std::vector<AnyContentEntity> setupFixtureB1();

    /**
     Library of Content B-2 (shared test fixture)
     <p>
     Integration tests use shared scenario fixtures as much as possible https://github.com/xjmusic/workstation/issues/202
     */
    std::vector<AnyContentEntity> setupFixtureB2();

    /**
     Library of Content B-3 (shared test fixture)
     <p>
     Integration tests use shared scenario fixtures as much as possible https://github.com/xjmusic/workstation/issues/202
     <p>
     memes bound to sequence-pattern because sequence-binding is not considered for beat sequences, beat sequence patterns do not have memes. https://github.com/xjmusic/workstation/issues/203
     <p>
     Choice is either by sequence-pattern (macro- or main-type sequences) or by sequence (beat- and detail-type sequences) https://github.com/xjmusic/workstation/issues/204
     <p>
     Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Beat or Detail-type Sequence) in order to of a composition that is dynamic when chosen to fill a Segment. https://github.com/xjmusic/workstation/issues/257
     + For this test, there's an Intro Pattern with all BLEEPS, multiple Loop Patterns with KICK and SNARE (2x each), and an Outro Pattern with all TOOTS.
     <p>
     Artist wants to of multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset. https://github.com/xjmusic/workstation/issues/283
     */
    std::vector<AnyContentEntity> setupFixtureB3();

    /**
     Library of Content B-4 (shared test fixture)
     <p>
     Detail Craft v1 https://github.com/xjmusic/workstation/issues/284
     */
    std::vector<AnyContentEntity> setupFixtureB4_DetailBass();

    /**
     Generate a Library comprising many related entities
  
     @param N magnitude of library to generate
     @return entities
     */
    std::vector<AnyContentEntity> generatedFixture(int N) {
      std::vector<AnyContentEntity> entities;

      project1 = FabricationContentOneFixtures::buildProject("Generated");
      entities.emplace_back(project1);
      library1 = FabricationContentOneFixtures::buildLibrary(project1, "generated");
      entities.emplace_back(library1);

      template1 = FabricationContentOneFixtures::buildTemplate(project1, "Complex Library Test", "complex");
      entities.emplace_back(template1);
      entities.emplace_back(FabricationContentOneFixtures::buildTemplateBinding(template1, library1));

      // Create a N-magnitude set of unique major memes
      std::vector<std::string>
          majorMemeNames = listOfUniqueRandom(N, LoremIpsum::COLORS);
      std::vector<std::string>
          minorMemeNames = listOfUniqueRandom((long) (double) (N >> 1), LoremIpsum::VARIANTS);
      std::vector<std::string>
          percussiveNames = listOfUniqueRandom(N, LoremIpsum::PERCUSSIVE_NAMES);

      // Generate a Drum Instrument for each meme
      for (int i = 0; i < N; i++) {
        std::string majorMemeName = majorMemeNames[i];
        std::string minorMemeName = random(minorMemeNames);
        //
        Instrument instrument = FabricationContentOneFixtures::buildInstrument(library1, Instrument::Type::Drum,
                                                                               Instrument::Mode::Event,
                                                                               Instrument::State::Published,
                                                                               majorMemeName + " Drums");
        entities.emplace_back(instrument);
        entities.emplace_back(FabricationContentOneFixtures::buildInstrumentMeme(instrument, majorMemeName));
        entities.emplace_back(FabricationContentOneFixtures::buildInstrumentMeme(instrument, minorMemeName));
        // audios of instrument
        for (int k = 0; k < N; k++)
          entities.emplace_back(
              FabricationContentOneFixtures::buildAudio(
                  instrument, StringUtils::toProper(percussiveNames[k]),
                  StringUtils::toLowerSlug(percussiveNames[k]) + ".wav",
                  random(0, 0.05f),
                  random(0.25f, 2),
                  random(80, 120), 0.62f,
                  percussiveNames[k],
                  "X",
                  random(0.8f, 1))
          );
        //
        std::cout << "Generated Drum-type Instrument id=" << instrument.id << ", minorMeme=" << minorMemeName
                  << ", majorMeme=" << majorMemeName << std::endl;
      }

      // Generate Perc Loop Instruments
      for (int i = 0; i < N; i++) {
        Instrument instrument = FabricationContentOneFixtures::buildInstrument(
            library1,
            Instrument::Type::Percussion,
            Instrument::Mode::Loop,
            Instrument::State::Published,
            "Perc Loop"
        );
        entities.emplace_back(instrument);
        std::cout << "Generated PercLoop-type Instrument id=" << instrument.id << std::endl;
      }

      // Generate N*2 total Macro-type programs, each transitioning of one MemeEntity to another
      for (int i = 0; i < N << 1; i++) {
        std::vector<std::string>
            twoMemeNames = listOfUniqueRandom(2, majorMemeNames);
        std::string majorMemeFromName = twoMemeNames[0];
        std::string majorMemeToName = twoMemeNames[1];
        std::string minorMemeName = random(minorMemeNames);
        std::vector<std::string>
            twoKeys = listOfUniqueRandom(2, LoremIpsum::MUSICAL_KEYS);
        std::string keyFrom = twoKeys[0];
        std::string keyTo = twoKeys[1];
        float intensityFrom = random(0.3f, 0.9f);
        float tempoFrom = random(80, 120);
        //
        Program program = FabricationContentOneFixtures::buildProgram(
            library1,
            Program::Type::Macro,
            Program::State::Published,
            minorMemeName + ", create " + majorMemeFromName + " to " + majorMemeToName,
            keyFrom,
            tempoFrom
        );
        entities.emplace_back(program);
        entities.emplace_back(FabricationContentOneFixtures::buildProgramMeme(program, minorMemeName));
        // of offset 0
        ProgramSequence sequence0 = FabricationContentOneFixtures::buildSequence(
            program,
            0,
            "Start " + majorMemeFromName,
            intensityFrom,
            keyFrom
        );
        entities.emplace_back(sequence0);
        ProgramSequenceBinding binding0 = FabricationContentOneFixtures::buildProgramSequenceBinding(sequence0, 0);
        entities.emplace_back(binding0);
        entities.emplace_back(
            FabricationContentOneFixtures::buildProgramSequenceBindingMeme(
                binding0,
                majorMemeFromName
            )
        );
        // to offset 1
        float intensityTo = random(0.3f, 0.9f);
        ProgramSequence sequence1 = FabricationContentOneFixtures::buildSequence(program, 0,
                                                                                 std::string.format("Finish %s",
                                                                                                    majorMemeToName),
                                                                                 intensityTo, keyTo);
        entities.emplace_back(sequence1);
        ProgramSequenceBinding binding1 = FabricationContentOneFixtures::buildProgramSequenceBinding(sequence1, 1);
        entities.emplace_back(binding1);
        entities.emplace_back(
            FabricationContentOneFixtures::buildProgramSequenceBindingMeme(binding1, majorMemeToName));
        //
        std::cout << "Generated Macro-type Program id=" << program.id << ", minorMeme=" << minorMemeName
                  << ", majorMemeFrom=" << majorMemeFromName << ", majorMemeTo=" << majorMemeToName << std::endl;
      }

      // Generate N*4 total Main-type Programs, each having N patterns comprised of ~N*2 chords, bound to N*4 sequence patterns
      ProgramSequence[]
      sequences = new ProgramSequence[N];
      for (int i = 0; i < N << 2; i++) {
        std::string majorMemeName = random(majorMemeNames);
        std::vector<std::string>
            sequenceNames = listOfUniqueRandom(N, LoremIpsum::ELEMENTS);
        std::vector<std::string>
            subKeys = listOfUniqueRandom(N, LoremIpsum::MUSICAL_KEYS);
        float []
        subDensities = listOfRandomValues(N);
        float tempo = random(80, 120);
        //
        Program program = add(entities, FabricationContentOneFixtures::buildProgram(library1, Program::Type::Main,
                                                                                    Program::State::Published,
                                                                                    std::string.format("%s: %s",
                                                                                                       majorMemeName,
                                                                                                       std::string.join(
                                                                                                           ",",
                                                                                                           sequenceNames)),
                                                                                    subKeys[0], tempo));
        add(entities, FabricationContentOneFixtures::buildProgramMeme(program, majorMemeName));
        // sequences of program
        for (int iP = 0; iP < N; iP++) {
          Integer total = random(LoremIpsum::SEQUENCE_TOTALS);
          sequences[iP] = add(entities, FabricationContentOneFixtures::buildSequence(program, total,
                                                                                     std::string.format("%s in %s",
                                                                                                        majorMemeName,
                                                                                                        sequenceNames[iP]),
                                                                                     subDensities[iP], subKeys[iP]));
          for (int iPC = 0; iPC < N << 2; iPC++) {
            // always use first chord, then use more chords with more intensity
            if (0 == iPC || StrictMath.random() < subDensities[iP]) {
              add(entities,
                  FabricationContentOneFixtures::buildChord(sequences[iP],
                                                            StrictMath.floor((float) iPC * total * 4 / N),
                                                            random(LoremIpsum::MUSICAL_CHORDS)));
            }
          }
        }
        // sequence sequence binding
        for (int offset = 0; offset < N << 2; offset++) {
          int num = (int) StrictMath.floor(StrictMath.random() * N);
          var binding = add(entities,
                            FabricationContentOneFixtures::buildProgramSequenceBinding(sequences[num], offset));
          add(entities, FabricationContentOneFixtures::buildMeme(binding, random(minorMemeNames)));
        }
        LOG.debug("Generated Main-type Program id={}, majorMeme={} with {} sequences bound {} times", program.id,
                  majorMemeName, N, N << 2);
      }

      // Generate N total Beat-type Sequences, each having N voices, and N*2 patterns comprised of N*8 events
      ProgramVoice[]
      voices = new ProgramVoice[N];
      Map <std::string, ProgramVoiceTrack> trackMap = new HashMap<>();
      for (int i = 0; i < N; i++) {
        std::string majorMemeName = majorMemeNames[i];
        float tempo = random(80, 120);
        std::string key = random(LoremIpsum::MUSICAL_KEYS);
        float intensity = random(0.4f, 0.9f);
        //
        Program program = add(entities, FabricationContentOneFixtures::buildProgram(library1, Program::Type::Beat,
                                                                                    Program::State::Published,
                                                                                    std::string.format("%s Beat",
                                                                                                       majorMemeName),
                                                                                    key,
                                                                                    tempo));
        trackMap.clear();
        add(entities, FabricationContentOneFixtures::buildProgramMeme(program, majorMemeName));
        // voices of program
        for (int iV = 0; iV < N; iV++) {
          voices[iV] = add(entities, FabricationContentOneFixtures::buildVoice(program, Instrument::Type::Drum,
                                                                               std::string.format("%s %s",
                                                                                                  majorMemeName,
                                                                                                  percussiveNames[iV])));
        }
        var sequenceBase = add(entities,
                               FabricationContentOneFixtures::buildSequence(program,
                                                                            random(LoremIpsum::SEQUENCE_TOTALS),
                                                                            "Base", intensity, key));
        // patterns of program
        for (int iP = 0; iP < N << 1; iP++) {
          Integer total = random(LoremIpsum::PATTERN_TOTALS);
          int num = (int) StrictMath.floor(StrictMath.random() * N);

          // first pattern is always a Loop (because that's required) then the rest at random
          var pattern = add(entities, FabricationContentOneFixtures::buildPattern(sequenceBase, voices[num], total,
                                                                                  std::string.format("%s %s %s",
                                                                                                     majorMemeName,
                                                                                                     majorMemeName +
                                                                                                     " pattern", random(
                                                                                          LoremIpsum::ELEMENTS))));
          for (int iPE = 0; iPE < N << 2; iPE++) {
            // always use first chord, then use more chords with more intensity
            if (0 == iPE || StrictMath.random() < intensity) {
              std::string name = percussiveNames[num];
              if (!trackMap.containsKey(name))
                trackMap.put(name, add(entities, FabricationContentOneFixtures::buildTrack(voices[num], name)));
              add(entities, FabricationContentOneFixtures::buildEvent(pattern, trackMap.get(name),
                                                                      (float) StrictMath.floor(
                                                                          (float) iPE * total * 4 / N),
                                                                      random(0.25f, 1.0f), "X", random(0.4f, 0.9f)));
            }
          }
        }
        LOG.debug("Generated Beat-type Program id={}, majorMeme={} with {} patterns", program.id, majorMemeName, N);
      }

      return entities;
    }

  };

} // namespace XJ

#endif //XJMUSIC_FABRICATION_CONTENT_TWO_FIXTURES_H