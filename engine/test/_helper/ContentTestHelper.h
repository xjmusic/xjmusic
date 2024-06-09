// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_CONTENT_TEST_HELPER_H
#define XJMUSIC_CONTENT_TEST_HELPER_H

#include <chrono>
#include <iomanip>
#include <random>
#include <sstream>
#include <variant>

#include "xjmusic/entities/content/ContentStore.h"

namespace XJ {

  static unsigned long long RANDOM_UUID_COUNTER = 0;

  using AnyContentEntity = std::variant<
      Project,
      Library,
      Template,
      TemplateBinding,
      Program,
      ProgramMeme,
      ProgramSequence,
      ProgramSequenceBinding,
      ProgramSequenceBindingMeme,
      ProgramVoice,
      ProgramVoiceTrack,
      ProgramSequenceChord,
      ProgramSequenceChordVoicing,
      ProgramSequencePattern,
      ProgramSequencePatternEvent,
      Instrument,
      InstrumentMeme,
      InstrumentAudio
  >;

  class ContentTestHelper {
  public:
    ContentTestHelper();

    ContentStore store;

    Project project1;
    Library library1;
    Template template1;
    TemplateBinding template1_binding;
    Template template2;
    Instrument instrument1;
    InstrumentMeme instrument1_meme;
    InstrumentAudio instrument1_audio;
    Instrument instrument2;
    InstrumentMeme instrument2_meme;
    InstrumentAudio instrument2_audio;
    Program program1;
    ProgramMeme program1_meme;
    ProgramVoice program1_voice;
    ProgramSequence program1_sequence;
    ProgramSequenceChord program1_sequence_chord0;
    ProgramSequenceChord program1_sequence_chord1;
    ProgramSequenceChordVoicing program1_sequence_chord0_voicing0;
    ProgramSequenceChordVoicing program1_sequence_chord1_voicing1;
    ProgramSequenceBinding program1_sequence_binding1;
    ProgramSequenceBinding program1_sequence_binding2;
    ProgramSequenceBindingMeme program1_sequence_binding1_meme1;
    ProgramSequenceBindingMeme program1_sequence_binding1_meme2;
    Program program2;
    ProgramMeme program2_meme;
    ProgramVoice program2_voice;
    ProgramSequence program2_sequence;
    ProgramSequencePattern program2_sequence_pattern1;
    ProgramSequencePattern program2_sequence_pattern2;
    ProgramVoiceTrack program2_voice_track1;
    ProgramVoiceTrack program2_voice_track2;
    ProgramSequencePatternEvent program2_sequence_pattern1_event1;
    ProgramSequencePatternEvent program2_sequence_pattern1_event2;

    static long long currentTimeMillis();

    static std::string randomUUID();

    static Project buildProject();

    static Library buildLibrary(const Project &project);

    static Template buildTemplate(const Project &project, std::string name, std::string shipKey);

    static TemplateBinding buildTemplateBinding(const Template &tmpl, const Library &library);

    static TemplateBinding buildTemplateBinding(const Template &tmpl, const Program &program);

    static TemplateBinding buildTemplateBinding(const Template &tmpl, const Instrument &instrument);

    static Instrument buildInstrument(const Library &library, Instrument::Type type, Instrument::Mode mode, std::string name);

    static InstrumentMeme buildInstrumentMeme(const Instrument &instrument, std::string name);

    static InstrumentAudio
    buildInstrumentAudio(const Instrument &instrument, std::string name, std::string waveformKey, float start,
                         float length, float tempo, float intensity, std::string event, std::string tones,
                         float volume);

    static Program buildProgram(const Library &library, Program::Type type, std::string name, std::string key, float tempo);

    static ProgramMeme buildProgramMeme(const Program &program, std::string name);

    static ProgramSequence
    buildProgramSequence(const Program &program, int total, std::string name, float intensity, std::string key);

    static ProgramSequenceBinding buildProgramSequenceBinding(const ProgramSequence &programSequence, int offset);

    static ProgramSequenceBindingMeme
    buildProgramSequenceBindingMeme(const ProgramSequenceBinding &programSequenceBinding, std::string name);

    static ProgramSequenceChord
    buildProgramSequenceChord(const ProgramSequence &programSequence, float position, std::string name);

    static ProgramSequenceChordVoicing
    buildProgramSequenceChordVoicing(const ProgramSequenceChord &programSequenceChord, const ProgramVoice &voice,
                                     std::string notes);

    static ProgramVoice buildProgramVoice(const Program &program, Instrument::Type type, std::string name);

    static ProgramVoiceTrack buildProgramVoiceTrack(const ProgramVoice &programVoice, std::string name);

    static ProgramSequencePattern
    buildProgramSequencePattern(const ProgramSequence &programSequence, const ProgramVoice &programVoice, int total,
                                std::string name);

    static ProgramSequencePatternEvent buildProgramSequencePatternEvent(const ProgramSequencePattern &programSequencePattern,
                                                                 const ProgramVoiceTrack &programVoiceTrack,
                                                                 float position, float duration, std::string tones,
                                                                 float velocity);
  };

} // namespace XJ

#endif//XJMUSIC_CONTENT_TEST_HELPER_H
