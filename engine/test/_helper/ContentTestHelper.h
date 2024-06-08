// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_CONTENT_TEST_HELPER_H
#define XJMUSIC_CONTENT_TEST_HELPER_H

#include <chrono>
#include <iomanip>
#include <random>
#include <sstream>

#include "xjmusic/entities/content/ContentStore.h"

namespace XJ {

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
  private:
    unsigned long long counter;
    unsigned long long session;

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

    static const std::string randomUUID();

    Project buildProject();

    Library buildLibrary(const Project &project);

    Template buildTemplate(const Project &project, std::string name, std::string shipKey);

    TemplateBinding buildTemplateBinding(const Template &tmpl, const Library &library);

    TemplateBinding buildTemplateBinding(const Template &tmpl, const Program &program);

    TemplateBinding buildTemplateBinding(const Template &tmpl, const Instrument &instrument);

    Instrument buildInstrument(const Library &library, Instrument::Type type, Instrument::Mode mode, std::string name);

    InstrumentMeme buildInstrumentMeme(const Instrument &instrument, std::string name);

    InstrumentAudio
    buildInstrumentAudio(const Instrument &instrument, std::string name, std::string waveformKey, float start,
                         float length, float tempo, float intensity, std::string event, std::string tones,
                         float volume);

    Program buildProgram(const Library &library, Program::Type type, std::string name, std::string key, float tempo);

    ProgramMeme buildProgramMeme(const Program &program, std::string name);

    ProgramSequence
    buildProgramSequence(const Program &program, int total, std::string name, float intensity, std::string key);

    ProgramSequenceBinding buildProgramSequenceBinding(const ProgramSequence &programSequence, int offset);

    ProgramSequenceBindingMeme
    buildProgramSequenceBindingMeme(const ProgramSequenceBinding &programSequenceBinding, std::string name);

    ProgramSequenceChord
    buildProgramSequenceChord(const ProgramSequence &programSequence, float position, std::string name);

    ProgramSequenceChordVoicing
    buildProgramSequenceChordVoicing(const ProgramSequenceChord &programSequenceChord, const ProgramVoice &voice,
                                     std::string notes);

    ProgramVoice buildProgramVoice(const Program &program, Instrument::Type type, std::string name);

    ProgramVoiceTrack buildProgramVoiceTrack(const ProgramVoice &programVoice, std::string name);

    ProgramSequencePattern
    buildProgramSequencePattern(const ProgramSequence &programSequence, const ProgramVoice &programVoice, int total,
                                std::string name);

    ProgramSequencePatternEvent buildProgramSequencePatternEvent(const ProgramSequencePattern &programSequencePattern,
                                                                 const ProgramVoiceTrack &programVoiceTrack,
                                                                 float position, float duration, std::string tones,
                                                                 float velocity);
  };

} // namespace XJ

#endif//XJMUSIC_CONTENT_TEST_HELPER_H
