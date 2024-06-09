// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATION_CONTENT_TWO_FIXTURES_H
#define XJMUSIC_FABRICATION_CONTENT_TWO_FIXTURES_H

#include <chrono>
#include <iomanip>
#include <random>
#include <sstream>
#include <utility>

#include "ContentTestHelper.h"
#include "ContentFixtures.h"
#include "LoremIpsum.h"
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
#include "xjmusic/entities/segment/Segment.h"
#include "xjmusic/entities/segment/SegmentChoice.h"
#include "xjmusic/entities/segment/SegmentChoiceArrangement.h"
#include "xjmusic/entities/segment/SegmentChoiceArrangementPick.h"
#include "xjmusic/entities/segment/SegmentChord.h"
#include "xjmusic/entities/segment/SegmentChordVoicing.h"
#include "xjmusic/entities/segment/SegmentMeme.h"
#include "xjmusic/entities/segment/SegmentMeta.h"
#include "xjmusic/util/StringUtils.h"
#include "xjmusic/util/ValueUtils.h"

namespace XJ {

  /**
 Integration tests use shared scenario fixtures as much as possible https://github.com/xjmusic/workstation/issues/202
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought to be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
  class SegmentFixtures {
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
    static std::vector<std::string> listOfUniqueRandom(long N, const std::vector<std::string>& sourceItems);

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
        const Template &tmpl);

    /**
     * Build a chain from a template
     */
    static Chain buildChain(
        const Template &tmpl,
        Chain::State state);

    /**
     * Build a chain from a template
     */
    static Chain buildChain(
        const Project &project,
        const std::string &name,
        Chain::Type type,
        Chain::State state,
        const Template &tmpl);

    /**
     * Build a chain from a template
     */
    static Chain buildChain(
        const Project &project,
        const Template &tmpl,
        const std::string &name,
        Chain::Type type,
        Chain::State state);

    /**
     * Build a chain from a template
     */
    static Chain buildChain(
        const Project &project,
        std::string name,
        Chain::Type type,
        Chain::State state,
        const Template &tmpl,
        const std::string &shipKey);

    /**
     * Build a Segment
     */
    static Segment buildSegment();

    /**
     * Build a Segment for a chain
     */
    static Segment buildSegment(
        const Chain& chain,
        int id,
        Segment::State state,
        std::string key,
        int total,
        float intensity,
        float tempo,
        std::string storageKey);

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
        bool hasEndSet);

    /**
     * Build a Segment for a chain
     */
    static Segment buildSegment(
        const Chain& chain,
        std::string key,
        int total,
        float intensity,
        float tempo);

    /**
     * Build a Segment for a chain
     */
    static Segment buildSegment(
        const Chain& chain,
        int offset,
        std::string key,
        int total,
        float intensity,
        float tempo);

    /**
     * Build a choice for a segment
     */
    static SegmentChoice buildSegmentChoice(
        const Segment& segment,
        Program::Type programType,
        const ProgramSequenceBinding& programSequenceBinding);

    /**
     * Build a choice for a segment
     */
    static SegmentChoice buildSegmentChoice(
        const Segment& segment,
        Program::Type programType,
        const ProgramSequence& programSequence);

    /**
     * Build a choice for a segment
     */
    static SegmentChoice buildSegmentChoice(
        const Segment& segment,
        int deltaIn,
        int deltaOut,
        const Program& program,
        Instrument::Type instrumentType,
        Instrument::Mode instrumentMode);

    /**
     * Build a choice for a segment
     */
    static SegmentChoice buildSegmentChoice(
        const Segment& segment,
        const Program& program);

    /**
     * Build a choice for a segment
     */
    static SegmentChoice buildSegmentChoice(
        const Segment& segment,
        const Instrument& instrument);

    /**
     * Build a meta for a segment
     */
    static SegmentMeta buildSegmentMeta(
        const Segment& segment,
        std::string key,
        std::string value);

    /**
     * Build a choice for a segment
     */
    static SegmentChoice buildSegmentChoice(
        const Segment& segment,
        const Program& program,
        const ProgramSequence& programSequence,
        const ProgramVoice& voice,
        const Instrument& instrument);

    /**
     * Build a choice of program and binding for a segment
     */
    static SegmentChoice buildSegmentChoice(
        Segment segment,
        int deltaIn,
        int deltaOut,
        Program program,
        const ProgramSequenceBinding& programSequenceBinding);

    /**
     * Build a choice of program, voice, and instrument for a segment
     */
    static SegmentChoice buildSegmentChoice(
        Segment segment,
        int deltaIn,
        int deltaOut,
        Program program,
        const ProgramVoice& voice,
        const Instrument& instrument);

    /**
     * Build a choice of program for a segment
     */
    static SegmentChoice buildSegmentChoice(
        const Segment& segment,
        int deltaIn,
        int deltaOut,
        const Program& program);

    /**
     * Build a meme for a segment
     */
    static SegmentMeme buildSegmentMeme(
        const Segment& segment,
        std::string name);

    /**
     * Build a chord for a segment
     */
    static SegmentChord buildSegmentChord(
        const Segment& segment,
        double atPosition,
        std::string name);

    /**
     * Build a voicing for a segment chord
     */
    static SegmentChordVoicing buildSegmentChordVoicing(
        const SegmentChord& chord,
        Instrument::Type type,
        std::string notes);

    /**
     * Build an arrangement for a segment choice
     */
    static SegmentChoiceArrangement buildSegmentChoiceArrangement(
        const SegmentChoice& segmentChoice);

    /**
     * Build a pick for a segment choice arrangement
     */
    static SegmentChoiceArrangementPick buildSegmentChoiceArrangementPick(
        const Segment& segment,
        const SegmentChoiceArrangement& segmentChoiceArrangement,
        const InstrumentAudio& instrumentAudio,
        std::string pickEvent);

    /**
     * Build a pick for a segment choice arrangement
     */
    static SegmentChoiceArrangementPick buildSegmentChoiceArrangementPick(
        const Segment& segment,
        const SegmentChoiceArrangement& segmentChoiceArrangement,
        const ProgramSequencePatternEvent& event,
        const InstrumentAudio& instrumentAudio,
        std::string pickEvent);

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
    std::vector<AnyContentEntity> generatedFixture(int N);
  };

}// namespace XJ

#endif//XJMUSIC_FABRICATION_CONTENT_TWO_FIXTURES_H