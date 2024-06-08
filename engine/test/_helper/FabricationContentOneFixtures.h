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
#include "ContentTestHelper.h"

namespace XJ {

/**
 Integration tests use shared scenario fixtures as much as possible https://github.com/xjmusic/workstation/issues/202
 <p>
 Testing the hypothesis that,
while unit tests are all independent,
 integration tests ought to be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
  class FabricationContentOneFixtures {
  public:

    static const std::string TEST_TEMPLATE_CONFIG;

    // Test data
    Project project1;
    Library library2;
    Program program15;
    Program program35;
    Program program3;
    Program program4;
    Template template1;

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
        ProgramSequencePattern pattern,
        ProgramVoiceTrack track,
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
        Project project1,
        std::string name,
        std::string shipKey,
        std::string config
    );

    /**
     * Build a template
     */
    static Template buildTemplate(
        Project project1,
        const std::string& name
    );

    /**
     * Build a template binding
     */
    static TemplateBinding buildTemplateBinding(
        const Template& tmpl,
        const Library& library
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

  };

} // namespace XJ

#endif //XJMUSIC_FABRICATION_CONTENT_ONE_FIXTURES_H