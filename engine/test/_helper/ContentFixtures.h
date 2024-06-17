// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATION_CONTENT_ONE_FIXTURES_H
#define XJMUSIC_FABRICATION_CONTENT_ONE_FIXTURES_H

#include <chrono>
#include <iomanip>
#include <random>
#include <sstream>

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
#include "xjmusic/entities/content/TemplateConfig.h"
#include "xjmusic/entities/content/TemplateBinding.h"
#include "xjmusic/util/StringUtils.h"
#include "xjmusic/entities/content/ContentEntityStore.h"

namespace XJ {

  /**
   Integration tests use shared scenario fixtures as much as possible https://github.com/xjmusic/xjmusic/issues/202
   <p>
   Testing the hypothesis that, while unit tests are all independent,
   integration tests ought to be as much about testing all features around a consensus model of the platform
   as they are about testing all resources.
   */
  class ContentFixtures {
  protected:
    static constexpr float RANDOM_VALUE_FROM = 0.3f;
    static constexpr float RANDOM_VALUE_TO = 0.8f;

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

    static const std::string TEST_TEMPLATE_CONFIG;

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
     * Build an instrument with audios
     */
    static std::vector<std::variant<Instrument,
        InstrumentAudio>> buildInstrumentWithAudios(
        Instrument instrument,
        const std::string& notes
    );

    /**
     * Build an audio from an instrument
     */
    static InstrumentAudio buildAudio(
        const Instrument& instrument,
        std::string name,
        std::string waveformKey,
        float start,
        float length,
        float tempo,
        float intensity,
        std::string event,
        std::string note,
        float volume
    );

    /**
     * Build an audio from an instrument
     */
    static InstrumentAudio buildAudio(
        const Instrument& instrument,
        std::string name,
        std::string note
    );


    /**
     * Build a project
     */
    static Project buildProject();

    /**
     * Build a program from a library
     */
    static Program buildProgram(
        const Library& library,
        Program::Type type,
        Program::State state,
        std::string name,
        std::string key,
        float tempo
    );

    /**
     * Build a program
     */
    static Program buildProgram(
        Program::Type type,
        std::string key,
        float tempo
    );

    /**
     * Build a program
     */
    static Program buildDetailProgram(
        std::string key,
        bool doPatternRestartOnChord,
        std::string name
    );

    /**
     * Build a program meme
     */
    static ProgramMeme buildMeme(
        const Program& program,
        std::string name
    );

    /**
     * Build a program sequence
     */
    static ProgramSequence buildSequence(
        const Program& program,
        int total,
        std::string name,
        float intensity,
        std::string key
    );

    /**
     * Build a program sequence binding
     */
    static ProgramSequence buildSequence(
        const Program& program,
        int total
    );

    /**
     * Build a program sequence binding
     */
    static ProgramSequenceBinding buildBinding(
        const ProgramSequence& programSequence,
        int offset
    );

    /**
     * Build a program sequence binding meme
     */
    static ProgramSequenceBindingMeme buildMeme(
        const ProgramSequenceBinding& programSequenceBinding,
        std::string name
    );

    /**
     * Build a program sequence chord
     */
    static ProgramSequenceChord buildChord(
        const ProgramSequence& programSequence,
        float position,
        std::string name
    );

    /**
     * Build a program sequence chord voicing
     */
    static ProgramSequenceChordVoicing buildVoicing(
        const ProgramSequenceChord& programSequenceChord,
        const ProgramVoice& voice,
        std::string notes
    );

    /**
     * Build a program voice
     */
    static ProgramVoice buildVoice(
        const Program& program,
        Instrument::Type type,
        std::string name
    );

    /**
     * Build a program voice
     */
    static ProgramVoice buildVoice(
        const Program& program,
        Instrument::Type type
    );

    /**
     * Build a program voice track
     */
    static ProgramVoiceTrack buildTrack(
        const ProgramVoice& programVoice,
        std::string name
    );

    /**
     * Build a program voice track
     */
    static ProgramVoiceTrack buildTrack(
        const ProgramVoice& programVoice
    );

    /**
     * Build a program sequence pattern
     */
    static ProgramSequencePattern buildPattern(
        const ProgramSequence& programSequence,
        const ProgramVoice& programVoice,
        int total,
        std::string name
    );

    /**
     * Build a program sequence pattern
     */
    static ProgramSequencePattern buildPattern(
        const ProgramSequence& sequence,
        const ProgramVoice& voice,
        int total
    );

    /**
     * Build a program sequence pattern event
     */
    static ProgramSequencePatternEvent buildEvent(
        const ProgramSequencePattern& pattern,
        const ProgramVoiceTrack& track,
        float position,
        float duration,
        std::string note,
        float velocity
    );

    /**
     * Build a program sequence pattern event
     */
    static ProgramSequencePatternEvent buildEvent(
        const ProgramSequencePattern& pattern,
        const ProgramVoiceTrack& track,
        float position,
        float duration,
        std::string note
    );

    /**
     * Build a program sequence pattern event
     */
    static Instrument buildInstrument(
        Instrument::Type type,
        Instrument::Mode mode,
        bool isTonal,
        bool isMultiphonic
    );

    /**
     * Build a program sequence pattern event
     */
    static InstrumentMeme buildMeme(
        const Instrument& instrument,
        std::string name
    );

    /**
     * Build a program sequence pattern event
     */
    static InstrumentAudio buildInstrumentAudio(
        const Instrument& instrument,
        std::string name,
        std::string waveformKey,
        float start,
        float length,
        float tempo,
        float intensity,
        std::string event,
        std::string tones,
        float volume
    );

    /**
     * Build a program sequence pattern event
     */
    static Library buildLibrary(
        const Project& project,
        std::string name
    );

    /**
     * Build a program sequence pattern event
     */
    static Project buildProject(
        std::string name
    );

    /**
     * Build a template
     * NOTE: it's crucial that a test template configuration disable certain aleatory features,
     * e.g. `deltaArcEnabled = false` to disable choice delta randomness,
     * otherwise tests may sporadically fail.
     */
    static Template buildTemplate(
        const Project& project1,
        std::string name,
        std::string shipKey
    );

    /**
     * Build a template
     */
    static Template buildTemplate(
        const Project& project1,
        std::string name,
        std::string shipKey,
        std::string config
    );

    /**
     * Build a template
     */
    static Template buildTemplate(
        const Project& project1,
        const std::string& name
    );

    /**
     * Build a template binding for a library
     */
    static TemplateBinding buildTemplateBinding(
        const Template& tmpl,
        const Library& library
    );

    /**
     * Build a template binding for a program
     */
    static TemplateBinding buildTemplateBinding(
        const Template& tmpl,
        const Program& program
    );

    /**
     * Build a template binding for a instrument
     */
    static TemplateBinding buildTemplateBinding(
        const Template& tmpl,
        const Instrument& instrument
    );

    /**
     * Build a template meme
     */
    static ProgramMeme buildProgramMeme(
        const Program& program,
        std::string name
    );

    /**
     * Build a program sequence
     */
    static ProgramSequence buildProgramSequence(
        const Program& program,
        int total,
        std::string name,
        float intensity,
        std::string key
    );

    /**
     * Build a program sequence binding
     */
    static ProgramSequenceBinding buildProgramSequenceBinding(
        const ProgramSequence& programSequence,
        int offset
    );

    /**
     * Build a program sequence binding meme
     */
    static ProgramSequenceBindingMeme buildProgramSequenceBindingMeme(
        const ProgramSequenceBinding& programSequenceBinding,
        std::string name
    );

    /**
     * Build a program sequence chord
     */
    static ProgramSequenceChord buildProgramSequenceChord(
        const ProgramSequence& programSequence,
        float position,
        std::string name
    );

    /**
     * Build a program sequence chord voicing
     */
    static ProgramSequenceChordVoicing buildProgramSequenceChordVoicing(
        const ProgramSequenceChord& programSequenceChord,
        const ProgramVoice& voice,
        std::string notes
    );

    /**
     * Build a program voice
     */
    static ProgramVoice buildProgramVoice(
        const Program& program,
        Instrument::Type type,
        std::string name
    );

    /**
     * Build a program voice
     */
    static ProgramVoiceTrack buildProgramVoiceTrack(
        const ProgramVoice& programVoice,
        std::string name
    );

    /**
     * Build a program sequence pattern
     */
    static ProgramSequencePattern buildProgramSequencePattern(
        const ProgramSequence& programSequence,
        const ProgramVoice& programVoice,
        int total,
        std::string name
    );

    /**
     * Build a program sequence pattern event
     */
    static ProgramSequencePatternEvent buildProgramSequencePatternEvent(
        const ProgramSequencePattern& programSequencePattern,
        const ProgramVoiceTrack& programVoiceTrack,
        float position,
        float duration,
        std::string tones,
        float velocity
    );

    /**
     * Build an instrument
     */
    static Instrument buildInstrument(
        const Library& library,
        Instrument::Type type,
        Instrument::Mode mode,
        Instrument::State state,
        std::string name
    );

    /**
     * Build an instrument
     */
    static InstrumentMeme buildInstrumentMeme(
        const Instrument& instrument,
        std::string name
    );

    /**
     A whole library of mock content

     @return collection of entities
     */
    void setupFixtureB1(ContentEntityStore *store);

    /**
     Library of Content B-2 (shared test fixture)
     <p>
     Integration tests use shared scenario fixtures as much as possible https://github.com/xjmusic/xjmusic/issues/202
     */
    void setupFixtureB2(ContentEntityStore *store);

    /**
     Library of Content B-3 (shared test fixture)
     <p>
     Integration tests use shared scenario fixtures as much as possible https://github.com/xjmusic/xjmusic/issues/202
     <p>
     memes bound to sequence-pattern because sequence-binding is not considered for beat sequences, beat sequence patterns do not have memes. https://github.com/xjmusic/xjmusic/issues/203
     <p>
     Choice is either by sequence-pattern (macro- or main-type sequences) or by sequence (beat- and detail-type sequences) https://github.com/xjmusic/xjmusic/issues/204
     <p>
     Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Beat or Detail-type Sequence) in order to of a composition that is dynamic when chosen to fill a Segment. https://github.com/xjmusic/xjmusic/issues/257
     + For this test, there's an Intro Pattern with all BLEEPS, multiple Loop Patterns with KICK and SNARE (2x each), and an Outro Pattern with all TOOTS.
     <p>
     Artist wants to of multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset. https://github.com/xjmusic/xjmusic/issues/283
     */
    void setupFixtureB3(ContentEntityStore *store);

    /**
     Library of Content B-4 (shared test fixture)
     <p>
     Detail Craft v1 https://github.com/xjmusic/xjmusic/issues/284
     */
    void setupFixtureB4_DetailBass(ContentEntityStore *store);

    /**
     Generate a Library comprising many related entities

     @param N magnitude of library to generate
     @return entities
     */
    void generatedFixture(ContentEntityStore *store, int N);

  };

} // namespace XJ

#endif //XJMUSIC_FABRICATION_CONTENT_ONE_FIXTURES_H