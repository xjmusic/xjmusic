// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <fstream>
#include <string>

#include "xjmusic/content/ContentEntityStore.h"

#include "../_helper/TestHelpers.h"
#include "../_helper/ContentFixtures.h"

static std::string CONTENT_STORE_TEST_JSON_PATH = "_data/content_store_test.json";

using namespace XJ;

class ContentEntityStoreTest : public testing::Test {

protected:
  ContentEntityStore subject{};

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

  void SetUp() override {
    // project
    project1 = ContentFixtures::buildProject();

    // Library content all created at this known time
    library1 = ContentFixtures::buildLibrary(&project1, "test");

    // Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569
    template1 = ContentFixtures::buildTemplate(&project1, "test1", "test_ship_key_a");
    template1_binding = ContentFixtures::buildTemplateBinding(&template1, &library1);
    template2 = ContentFixtures::buildTemplate(&project1, "test2", "test_ship_key_b");

    // Instrument 1
    instrument1 = ContentFixtures::buildInstrument(&library1, Instrument::Type::Drum, Instrument::Mode::Event,
                                                   Instrument::State::Published, "808 Drums");
    instrument1_meme = ContentFixtures::buildInstrumentMeme(&instrument1, "Ants");
    instrument1_audio = ContentFixtures::buildInstrumentAudio(&instrument1, "Chords Cm to D",
                                                              "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav",
                                                              0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f);

    // Instrument 2
    instrument2 = ContentFixtures::buildInstrument(&library1, Instrument::Type::Pad, Instrument::Mode::Chord,
                                                   Instrument::State::Published, "Pad");
    instrument2_meme = ContentFixtures::buildInstrumentMeme(&instrument2, "Peanuts");
    instrument2_audio = ContentFixtures::buildInstrumentAudio(&instrument2, "Chord Fm",
                                                              "a0b9fg73k107s74kf9b4h8d9e009f7-g0e73982.wav",
                                                              0.02f, 1.123f, 140.0f, 0.52f, "BING", "F,A,C", 0.9f);

    // Program 1, main-type, has sequence with chords, bound to many offsets
    program1 = ContentFixtures::buildProgram(&library1, Program::Type::Main, Program::State::Published, "leaves", "C#",
                                             120.4f);
    program1_meme = ContentFixtures::buildProgramMeme(&program1, "Ants");
    program1_voice = ContentFixtures::buildProgramVoice(&program1, Instrument::Type::Stripe, "Birds");
    program1_sequence = ContentFixtures::buildProgramSequence(&program1, 8, "decay", 0.25f, "F#");
    program1_sequence_chord0 = ContentFixtures::buildProgramSequenceChord(&program1_sequence, 0.0, "G minor");
    program1_sequence_chord1 = ContentFixtures::buildProgramSequenceChord(&program1_sequence, 2.0, "A minor");
    program1_sequence_chord0_voicing0 = ContentFixtures::buildProgramSequenceChordVoicing(&program1_sequence_chord0,
                                                                                          &program1_voice, "G");
    program1_sequence_chord1_voicing1 = ContentFixtures::buildProgramSequenceChordVoicing(&program1_sequence_chord1,
                                                                                          &program1_voice, "Bb");
    program1_sequence_binding1 = ContentFixtures::buildProgramSequenceBinding(&program1_sequence, 0);
    program1_sequence_binding2 = ContentFixtures::buildProgramSequenceBinding(&program1_sequence, 5);
    program1_sequence_binding1_meme1 = ContentFixtures::buildProgramSequenceBindingMeme(&program1_sequence_binding1,
                                                                                        "Gravel");
    program1_sequence_binding1_meme2 = ContentFixtures::buildProgramSequenceBindingMeme(&program1_sequence_binding1,
                                                                                        "Road");

    // Program 2, beat-type, has unbound sequence with pattern with events
    program2 = ContentFixtures::buildProgram(&library1, Program::Type::Beat, Program::State::Published, "coconuts",
                                             "F#",
                                             110.3f);
    program2_meme = ContentFixtures::buildProgramMeme(&program2, "Bells");
    program2_voice = ContentFixtures::buildProgramVoice(&program2, Instrument::Type::Drum, "Drums");
    program2_sequence = ContentFixtures::buildProgramSequence(&program2, 16, "Base", 0.5f, "C");
    program2_sequence_pattern1 = ContentFixtures::buildProgramSequencePattern(&program2_sequence, &program2_voice, 16,
                                                                              "growth");
    program2_sequence_pattern2 = ContentFixtures::buildProgramSequencePattern(&program2_sequence, &program2_voice, 12,
                                                                              "decay");
    program2_voice_track1 = ContentFixtures::buildProgramVoiceTrack(&program2_voice, "BOOM");
    program2_voice_track2 = ContentFixtures::buildProgramVoiceTrack(&program2_voice, "SMACK");
    program2_sequence_pattern1_event1 = ContentFixtures::buildProgramSequencePatternEvent(&program2_sequence_pattern1,
                                                                                          &program2_voice_track1, 0.0f,
                                                                                          1.0f, "C", 1.0f);
    program2_sequence_pattern1_event2 = ContentFixtures::buildProgramSequencePatternEvent(&program2_sequence_pattern1,
                                                                                          &program2_voice_track2, 0.5f,
                                                                                          1.1f, "D", 0.9f);

    // Instantiate subject
    subject = ContentEntityStore();

    // Set all content in store
    subject.setProjects({project1});
    subject.setLibraries({library1});
    subject.setTemplates({template1, template2});
    subject.setTemplateBindings({template1_binding});
    subject.setInstruments({instrument1, instrument2});
    subject.setInstrumentMemes({instrument1_meme, instrument2_meme});
    subject.setInstrumentAudios({instrument1_audio, instrument2_audio});
    subject.setPrograms({program1, program2});
    subject.setProgramMemes({program1_meme, program2_meme});
    subject.setProgramVoices({program1_voice, program2_voice});
    subject.setProgramSequences({program1_sequence, program2_sequence});
    subject.setProgramSequenceChords({program1_sequence_chord0, program1_sequence_chord1});
    subject.setProgramSequenceChordVoicings({program1_sequence_chord0_voicing0, program1_sequence_chord1_voicing1});
    subject.setProgramSequenceBindings({program1_sequence_binding1, program1_sequence_binding2});
    subject.setProgramSequenceBindingMemes({program1_sequence_binding1_meme1, program1_sequence_binding1_meme2});
    subject.setProgramSequencePatterns({program2_sequence_pattern1, program2_sequence_pattern2});
    subject.setProgramVoiceTracks({program2_voice_track1, program2_voice_track2});
    subject.setProgramSequencePatternEvents({program2_sequence_pattern1_event1, program2_sequence_pattern1_event2});
  }
};

TEST_F(ContentEntityStoreTest, FromJsonFile) {
  // Load the JSON file
  std::ifstream file(CONTENT_STORE_TEST_JSON_PATH);
  ASSERT_TRUE(file.is_open());

  // Deserialize a content store from a JSON file stream
  auto result = ContentEntityStore(file);

  // Assert the correct count of entities in the content store
  ASSERT_EQ(2, result.getInstruments().size());
  ASSERT_EQ(2, result.getInstrumentAudios().size());
  ASSERT_EQ(2, result.getInstrumentMemes().size());
  ASSERT_EQ(1, result.getLibraries().size());
  ASSERT_EQ(2, result.getPrograms().size());
  ASSERT_EQ(2, result.getProgramMemes().size());
  ASSERT_EQ(2, result.getProgramSequences().size());
  ASSERT_EQ(2, result.getProgramSequenceBindings().size());
  ASSERT_EQ(2, result.getProgramSequenceBindingMemes().size());
  ASSERT_EQ(2, result.getProgramSequenceChords().size());
  ASSERT_EQ(2, result.getProgramSequenceChordVoicings().size());
  ASSERT_EQ(2, result.getProgramSequencePatterns().size());
  ASSERT_EQ(2, result.getProgramSequencePatternEvents().size());
  ASSERT_EQ(2, result.getProgramVoices().size());
  ASSERT_EQ(2, result.getProgramVoiceTracks().size());
  ASSERT_EQ(1, result.getProjects().size());
  ASSERT_EQ(2, result.getTemplates().size());
  ASSERT_EQ(3, result.getTemplateBindings().size());
}

TEST_F(ContentEntityStoreTest, FromJsonString) {
  // Load the JSON file
  std::ifstream file(CONTENT_STORE_TEST_JSON_PATH);
  ASSERT_TRUE(file.is_open());
  std::string jsonString((std::istreambuf_iterator(file)), std::istreambuf_iterator<char>());

  // Deserialize a content store from a JSON file stream
  auto result = ContentEntityStore(jsonString);

  // Assert the correct count of entities in the content store
  ASSERT_EQ(2, result.getInstruments().size());
  ASSERT_EQ(2, result.getInstrumentAudios().size());
  ASSERT_EQ(2, result.getInstrumentMemes().size());
  ASSERT_EQ(1, result.getLibraries().size());
  ASSERT_EQ(2, result.getPrograms().size());
  ASSERT_EQ(2, result.getProgramMemes().size());
  ASSERT_EQ(2, result.getProgramSequences().size());
  ASSERT_EQ(2, result.getProgramSequenceBindings().size());
  ASSERT_EQ(2, result.getProgramSequenceBindingMemes().size());
  ASSERT_EQ(2, result.getProgramSequenceChords().size());
  ASSERT_EQ(2, result.getProgramSequenceChordVoicings().size());
  ASSERT_EQ(2, result.getProgramSequencePatterns().size());
  ASSERT_EQ(2, result.getProgramSequencePatternEvents().size());
  ASSERT_EQ(2, result.getProgramVoices().size());
  ASSERT_EQ(2, result.getProgramVoiceTracks().size());
  ASSERT_EQ(1, result.getProjects().size());
  ASSERT_EQ(2, result.getTemplates().size());
  ASSERT_EQ(3, result.getTemplateBindings().size());
}

TEST_F(ContentEntityStoreTest, SetProgramGetProgram) {
  subject.clear();

  // Create an EntityUtils object
  Program program;
  program.id = "fd472031-dd6e-47ae-b0b7-ae4f02b94726";
  program.libraryId = "7ec2d282-d481-4fee-b57b-082e90284102";
  program.state = Program::State::Draft;
  program.type = Program::Type::Main;
  program.key = "C";
  program.tempo = 120.0f;
  program.name = "Test EntityUtils";
  program.config = ProgramConfig(R"(barBeats = 4)");
  program.isDeleted = false;
  program.updatedAt = 1711089919558;

  // Put the EntityUtils object into the ContentEntityStore
  subject.setPrograms({program});

  // Retrieve the EntityUtils object from the ContentEntityStore
  const auto retrievedEntity = subject.getProgram("fd472031-dd6e-47ae-b0b7-ae4f02b94726");

  // Assert that the retrieved EntityUtils object is equal to the original EntityUtils object
  ASSERT_TRUE(retrievedEntity.has_value());
  ASSERT_EQ(program.id, retrievedEntity.value()->id);
}

TEST_F(ContentEntityStoreTest, GetInstrumentTypeOfEvent) {
  ASSERT_EQ(Instrument::Type::Drum, subject.getInstrumentTypeOfEvent(&program2_sequence_pattern1_event1));
}

TEST_F(ContentEntityStoreTest, HasInstrumentsOfMode) {
  ASSERT_FALSE(subject.hasInstrumentsOfMode(Instrument::Mode::Loop));
  ASSERT_TRUE(subject.hasInstrumentsOfMode(Instrument::Mode::Event));
}

TEST_F(ContentEntityStoreTest, HasInstrumentsOfType) {
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Bass));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Hook));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Percussion));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Stab));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Sticky));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Stripe));
  ASSERT_TRUE(subject.hasInstrumentsOfType(Instrument::Type::Drum));
  ASSERT_TRUE(subject.hasInstrumentsOfType(Instrument::Type::Pad));
}

TEST_F(ContentEntityStoreTest, HasInstrumentsOfTypeAndMode) {
  ASSERT_TRUE(subject.hasInstrumentsOfTypeAndMode(Instrument::Type::Drum, Instrument::Mode::Event));
  ASSERT_FALSE(subject.hasInstrumentsOfTypeAndMode(Instrument::Type::Drum, Instrument::Mode::Loop));
}

TEST_F(ContentEntityStoreTest, GetAvailableOffsets) {
  const auto result = subject.getAvailableOffsets(&program1_sequence_binding1);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getAudiosOfInstrumentId) {
  const auto result = subject.getAudiosOfInstrument(instrument1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getAudiosOfInstrument) {
  const auto result = subject.getAudiosOfInstrument(&instrument2);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getBindingsOfSequence) {
  const auto result = subject.getBindingsOfSequence(&program1_sequence);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getBindingsOfSequenceId) {
  const auto result = subject.getBindingsOfSequence(program1_sequence.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getSequenceBindingMemesOfProgram) {
  const auto result = subject.getSequenceBindingMemesOfProgram(&program1);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getSequenceBindingMemesOfProgramId) {
  const auto result = subject.getSequenceBindingMemesOfProgram(program1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getBindingsAtOffsetOfProgram) {
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(&program1, 0, false).size());
  ASSERT_EQ(0, subject.getBindingsAtOffsetOfProgram(&program1, 1, false).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(&program1, 0, true).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(&program1, 1, true).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(&program1, 2, true).size());
}

TEST_F(ContentEntityStoreTest, getBindingsAtOffsetOfProgramId) {
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(program1.id, 0, false).size());
  ASSERT_EQ(0, subject.getBindingsAtOffsetOfProgram(program1.id, 1, false).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(program1.id, 0, true).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(program1.id, 1, true).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(program1.id, 2, true).size());
}

TEST_F(ContentEntityStoreTest, getChordsOfSequence) {
  const auto result = subject.getChordsOfSequence(&program1_sequence);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getChordsOfSequenceId) {
  const auto result = subject.getChordsOfSequence(program1_sequence.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getEventsOfPattern) {
  const auto result = subject.getEventsOfPattern(&program2_sequence_pattern1);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getEventsOfPatternId) {
  const auto result = subject.getEventsOfPattern(program2_sequence_pattern1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getEventsOfTrack) {
  const auto result = subject.getEventsOfTrack(&program2_voice_track1);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getEventsOfTrackId) {
  const auto result = subject.getEventsOfTrack(program2_voice_track1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getEventsOfPatternAndTrack) {
  const auto result = subject.getEventsOfPatternAndTrack(&program2_sequence_pattern1, &program2_voice_track1);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getEventsOfPatternAndTrackId) {
  const auto result = subject.getEventsOfPatternAndTrack(program2_sequence_pattern1.id, program2_voice_track1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getInstrument) {
  const auto result = subject.getInstrument(instrument1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(instrument1.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getInstrumentAudio) {
  const auto result = subject.getInstrumentAudio(instrument1_audio.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(instrument1_audio.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getAudiosOfInstrumentTypesAndModes) {
  const auto result = subject.getAudiosOfInstrumentTypesAndModes({Instrument::Type::Drum}, {Instrument::Mode::Event});

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getAudiosOfInstrumentTypes) {
  const auto result = subject.getAudiosOfInstrumentTypes({Instrument::Type::Drum});

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getInstrumentAudios) {
  const auto result = subject.getInstrumentAudios();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getMemesOfInstrumentId) {
  const auto result = subject.getMemesOfInstrument(instrument1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getInstrumentMemes) {
  const auto result = subject.getInstrumentMemes();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getInstruments) {
  const auto result = subject.getInstruments();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getInstrumentsOfLibrary) {
  const auto result = subject.getInstrumentsOfLibrary(&library1);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getInstrumentsOfLibraryId) {
  const auto result = subject.getInstrumentsOfLibrary(library1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getInstrumentsOfTypes) {
  const auto result = subject.getInstrumentsOfTypes({Instrument::Type::Drum});

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getInstrumentTypeOfAudioId) {
  const auto result = subject.getInstrumentTypeOfAudio(instrument1_audio.id);

  ASSERT_EQ(Instrument::Type::Drum, result);
}

TEST_F(ContentEntityStoreTest, getMemesOfProgramId) {
  const auto result = subject.getMemesOfProgram(program1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getMemesAtBeginning) {
  const auto result = subject.getMemesAtBeginning(&program1);

  ASSERT_EQ(3, result.size());
}

TEST_F(ContentEntityStoreTest, getPatternIdOfEventId) {
  const auto result = subject.getPatternIdOfEvent(program2_sequence_pattern1_event1.id);

  ASSERT_EQ(program2_sequence_pattern1.id, result);
}

TEST_F(ContentEntityStoreTest, getPatternsOfSequence) {
  const auto result = subject.getPatternsOfSequence(&program2_sequence);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getPatternsOfSequenceId) {
  const auto result = subject.getPatternsOfSequence(program2_sequence.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getPatternsOfVoice) {
  const auto result = subject.getPatternsOfVoice(&program2_voice);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getPatternsOfVoiceId) {
  const auto result = subject.getPatternsOfVoice(program2_voice.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getProgram) {
  const auto result = subject.getProgram(program1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getPrograms) {
  const auto result = subject.getPrograms();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getProgramsOfLibrary) {
  const auto result = subject.getProgramsOfLibrary(&library1);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getProgramsOfLibraryId) {
  const auto result = subject.getProgramsOfLibrary(library1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getProgramMemes) {
  const auto result = subject.getProgramMemes();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getProgramSequence) {
  const auto result = subject.getProgramSequence(program1_sequence.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_sequence.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getSequenceOfBinding) {
  const auto result = subject.getSequenceOfBinding(&program1_sequence_binding1);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_sequence.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getProgramSequences) {
  const auto result = subject.getProgramSequences();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getSequencesOfProgramId) {
  const auto result = subject.getSequencesOfProgram(program1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getProgramSequenceBinding) {
  const auto result = subject.getProgramSequenceBinding(program1_sequence_binding1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_sequence_binding1.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getProgramSequenceBindings) {
  const auto result = subject.getProgramSequenceBindings();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getSequenceBindingsOfProgram) {
  const auto result = subject.getSequenceBindingsOfProgram(program1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getProgramSequenceBindingMemes) {
  const auto result = subject.getProgramSequenceBindingMemes();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getMemesOfSequenceBinding) {
  const auto result = subject.getMemesOfSequenceBinding(&program1_sequence_binding1);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getMemesOfSequenceBindingId) {
  const auto result = subject.getMemesOfSequenceBinding(program1_sequence_binding1.id);

  ASSERT_EQ(2, result.size());
}


TEST_F(ContentEntityStoreTest, getProgramSequencePattern) {
  const auto result = subject.getProgramSequencePattern(program2_sequence_pattern1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program2_sequence_pattern1.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getProgramSequencePatterns) {
  const auto result = subject.getProgramSequencePatterns();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getProgramSequencePatternEvent) {
  const auto result = subject.getProgramSequencePatternEvent(program2_sequence_pattern1_event1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program2_sequence_pattern1_event1.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getProgramSequencePatternEvents) {
  const auto result = subject.getProgramSequencePatternEvents();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getProgramSequenceChord) {
  const auto result = subject.getProgramSequenceChord(program1_sequence_chord0.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_sequence_chord0.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getProgramSequenceChords) {
  const auto result = subject.getProgramSequenceChords();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, testGetProgramSequenceChords) {
  const auto result = subject.getSequenceChordsOfProgram(program1_sequence.programId);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getProgramSequenceChordVoicings) {
  const auto result = subject.getProgramSequenceChordVoicings();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getProgramVoice) {
  const auto result = subject.getProgramVoice(program1_voice.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_voice.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getProgramVoices) {
  const auto result = subject.getProgramVoices();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getProgramVoiceTrack) {
  const auto result = subject.getProgramVoiceTrack(program2_voice_track1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program2_voice_track1.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getProgramVoiceTracks) {
  const auto result = subject.getProgramVoiceTracks();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getTracksOfProgramId) {
  const auto result = subject.getTracksOfProgram(program2.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getTracksOfVoice) {
  const auto result = subject.getTracksOfVoice(&program2_voice);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getTracksOfVoiceId) {
  const auto result = subject.getTracksOfVoice(program2_voice.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getTemplates) {
  const auto result = subject.getTemplates();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getTemplate) {
  const auto result = subject.getTemplate(template1.id);

  ASSERT_EQ(template1.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getTemplateBindings) {
  const auto result = subject.getTemplateBindings();

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getBindingsOfTemplate) {
  const auto result = subject.getBindingsOfTemplate(template1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getLibrary) {
  const auto result = subject.getLibrary(library1.id);

  ASSERT_EQ(library1.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getLibraries) {
  const auto result = subject.getLibraries();

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getProject) {
  const auto result = subject.getProject();

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(project1.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getTrackOfEvent) {
  const auto result = subject.getTrackOfEvent(&program2_sequence_pattern1_event1);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program2_voice_track1.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getTrackNames) {
  const auto result = subject.getTrackNamesOfVoice(&program2_voice);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getVoicingsOfChord) {
  const auto result = subject.getVoicingsOfChord(&program1_sequence_chord0);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getVoicingsOfChordId) {
  const auto result = subject.getVoicingsOfChord(program1_sequence_chord0.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getSequenceChordVoicingsOfProgram_dontCountInvalidVoicings) {
  const auto program1_voice2 = ContentFixtures::buildProgramVoice(&program1, Instrument::Type::Pad, "Birds");
  subject.put(ContentFixtures::buildVoicing(
      &program1_sequence_chord0, &program1_voice2, "(None)")); // No voicing notes- doesn't count!
  const auto result = subject.getSequenceChordVoicingsOfProgram(program1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getVoicingsOfChordAndVoice) {
  const auto result = subject.getVoicingsOfChordAndVoice(&program1_sequence_chord0, &program1_voice);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getVoicingsOfChordIdAndVoiceId) {
  const auto result = subject.getVoicingsOfChordAndVoice(program1_sequence_chord0.id, program1_voice.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getVoiceOfEvent) {
  const auto result = subject.getVoiceOfEvent(&program2_sequence_pattern1_event1);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program2_voice.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getVoicesOfProgram) {
  const auto result = subject.getVoicesOfProgram(&program2);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getVoicesOfProgramId) {
  const auto result = subject.getVoicesOfProgram(program2.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getTemplateByIdentifier) {
  const auto result1 = subject.getTemplateByIdentifier(template1.name);
  ASSERT_TRUE(result1.has_value());
  ASSERT_EQ(template1.id, result1.value()->id);

  const auto result2 = subject.getTemplateByIdentifier(template2.name);
  ASSERT_TRUE(result2.has_value());
  ASSERT_EQ(template2.id, result2.value()->id);

  const auto result3 = subject.getTemplateByIdentifier(template1.shipKey);
  ASSERT_TRUE(result3.has_value());
  ASSERT_EQ(template1.id, result3.value()->id);

  const auto result4 = subject.getTemplateByIdentifier(template2.shipKey);
  ASSERT_TRUE(result4.has_value());
  ASSERT_EQ(template2.id, result4.value()->id);

  const auto result5 = subject.getTemplateByIdentifier(template1.id);
  ASSERT_TRUE(result5.has_value());
  ASSERT_EQ(template1.id, result5.value()->id);

  const auto result6 = subject.getTemplateByIdentifier(template2.id);
  ASSERT_TRUE(result6.has_value());
  ASSERT_EQ(template2.id, result6.value()->id);
}

TEST_F(ContentEntityStoreTest, setInstruments) {
  subject.setInstruments({});

  ASSERT_TRUE(subject.getInstruments().empty());
}

TEST_F(ContentEntityStoreTest, setInstrumentAudios) {
  subject.setInstrumentAudios({});

  ASSERT_TRUE(subject.getInstrumentAudios().empty());
}

TEST_F(ContentEntityStoreTest, setInstrumentMemes) {
  subject.setInstrumentMemes({});

  ASSERT_TRUE(subject.getInstrumentMemes().empty());
}

TEST_F(ContentEntityStoreTest, setLibraries) {
  subject.setLibraries({});

  ASSERT_TRUE(subject.getLibraries().empty());
}

TEST_F(ContentEntityStoreTest, setPrograms) {
  subject.setPrograms({});

  ASSERT_TRUE(subject.getPrograms().empty());
}

TEST_F(ContentEntityStoreTest, setProgramMemes) {
  subject.setProgramMemes({});

  ASSERT_TRUE(subject.getProgramMemes().empty());
}

TEST_F(ContentEntityStoreTest, setProgramSequences) {
  subject.setProgramSequences({});

  ASSERT_TRUE(subject.getProgramSequences().empty());
}

TEST_F(ContentEntityStoreTest, setProgramSequenceBindings) {
  subject.setProgramSequenceBindings({});

  ASSERT_TRUE(subject.getProgramSequenceBindings().empty());
}

TEST_F(ContentEntityStoreTest, setProgramSequenceBindingMemes) {
  subject.setProgramSequenceBindingMemes({});

  ASSERT_TRUE(subject.getProgramSequenceBindingMemes().empty());
}

TEST_F(ContentEntityStoreTest, setProgramSequenceChords) {
  subject.setProgramSequenceChords({});

  ASSERT_TRUE(subject.getProgramSequenceChords().empty());
}

TEST_F(ContentEntityStoreTest, setProgramSequenceChordVoicings) {
  subject.setProgramSequenceChordVoicings({});

  ASSERT_TRUE(subject.getProgramSequenceChordVoicings().empty());
}

TEST_F(ContentEntityStoreTest, setProgramSequencePatterns) {
  subject.setProgramSequencePatterns({});

  ASSERT_TRUE(subject.getProgramSequencePatterns().empty());
}

TEST_F(ContentEntityStoreTest, setProgramSequencePatternEvents) {
  subject.setProgramSequencePatternEvents({});

  ASSERT_TRUE(subject.getProgramSequencePatternEvents().empty());
}

TEST_F(ContentEntityStoreTest, setProgramVoices) {
  subject.setProgramVoices({});

  ASSERT_TRUE(subject.getProgramVoices().empty());
}

TEST_F(ContentEntityStoreTest, setProgramVoiceTracks) {
  subject.setProgramVoiceTracks({});

  ASSERT_TRUE(subject.getProgramVoiceTracks().empty());
}

TEST_F(ContentEntityStoreTest, setProjects) {
  auto project2 = ContentFixtures::buildProject();
  subject.setProjects({project2});

  const auto result = subject.getProject();

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(result.value()->id, project2.id);
}

TEST_F(ContentEntityStoreTest, setTemplates) {
  subject.setTemplates({});

  ASSERT_TRUE(subject.getTemplates().empty());
}

TEST_F(ContentEntityStoreTest, setTemplateBindings) {
  subject.setTemplateBindings({});

  ASSERT_TRUE(subject.getTemplateBindings().empty());
}


TEST_F(ContentEntityStoreTest, getSequencePatternEventsOfProgram) {
  const auto result = subject.getSequencePatternEventsOfProgram(program2.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getEventsOfProgramId) {
  const auto result = subject.getSequencePatternEventsOfProgram(program2_sequence_pattern1.programId);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getSequencePatternsOfProgram) {
  const auto result = subject.getSequencePatternsOfProgram(&program2);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getPatternsOfProgramId) {
  const auto result = subject.getSequencePatternsOfProgram(program2_sequence_pattern1.programId);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getInstrumentsOfType) {
  const auto result = subject.getInstrumentsOfType(Instrument::Type::Drum);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getInstrumentsOfTypesAndModes) {
  const auto result = subject.getInstrumentsOfTypesAndModes({Instrument::Type::Drum}, {Instrument::Mode::Event});

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getInstrumentTypeOfAudio) {
  const auto result = subject.getInstrumentTypeOfAudio(instrument1_audio.id);

  ASSERT_EQ(Instrument::Type::Drum, result);
}

TEST_F(ContentEntityStoreTest, getMemesOfInstrument) {
  const auto result = subject.getMemesOfInstrument(instrument1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getPatternIdOfEvent) {
  const auto result = subject.getPatternIdOfEvent(program2_sequence_pattern1_event1.id);

  ASSERT_EQ(program2_sequence_pattern1.id, result);
}

TEST_F(ContentEntityStoreTest, getProgramsOfType) {
  const auto result = subject.getProgramsOfType(Program::Type::Main);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getMemesOfProgram) {
  const auto result = subject.getMemesOfProgram(program1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getSequencesOfProgram) {
  const auto result = subject.getSequencesOfProgram(program1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentEntityStoreTest, getSequenceChordsOfProgram) {
  const auto result = subject.getSequenceChordsOfProgram(program1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getTracksOfProgram) {
  const auto result = subject.getTracksOfProgram(program2.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getTracksOfProgramType) {
  const auto result = subject.getTracksOfProgramType(Program::Type::Beat);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getTemplateBinding) {
  const auto result = subject.getTemplateBinding(template1_binding.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(template1_binding.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getTrackNamesOfVoice) {
  const auto result = subject.getTrackNamesOfVoice(&program2_voice);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentEntityStoreTest, getInstrumentMeme) {
  const auto result = subject.getInstrumentMeme(instrument1_meme.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(instrument1_meme.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getProgramMeme) {
  const auto result = subject.getProgramMeme(program1_meme.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_meme.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getProgramSequenceBindingMeme) {
  const auto result = subject.getProgramSequenceBindingMeme(program1_sequence_binding1_meme1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_sequence_binding1_meme1.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getProgramSequenceChordVoicing) {
  const auto result = subject.getProgramSequenceChordVoicing(program1_sequence_chord0_voicing0.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_sequence_chord0_voicing0.id, result.value()->id);
}

TEST_F(ContentEntityStoreTest, getPatternsOfSequenceAndVoice) {
  ASSERT_EQ(2, subject.getPatternsOfSequenceAndVoice(program2_sequence.id, program2_voice.id).size());
  ASSERT_EQ(0, subject.getPatternsOfSequenceAndVoice(EntityUtils::computeUniqueId(), program2_voice.id).size());
  ASSERT_EQ(0, subject.getPatternsOfSequenceAndVoice(program2_sequence.id, EntityUtils::computeUniqueId()).size());
}

TEST_F(ContentEntityStoreTest, ForTemplate_BoundToLibrary) {
  auto result = subject.forTemplate(&template1);

  // Assert the correct count of entities in the content store
  ASSERT_EQ(2, result.getInstruments().size());
  ASSERT_EQ(2, result.getInstrumentAudios().size());
  ASSERT_EQ(2, result.getInstrumentMemes().size());
  ASSERT_EQ(1, result.getLibraries().size());
  ASSERT_EQ(2, result.getPrograms().size());
  ASSERT_EQ(2, result.getProgramMemes().size());
  ASSERT_EQ(2, result.getProgramSequences().size());
  ASSERT_EQ(2, result.getProgramSequenceBindings().size());
  ASSERT_EQ(2, result.getProgramSequenceBindingMemes().size());
  ASSERT_EQ(2, result.getProgramSequenceChords().size());
  ASSERT_EQ(2, result.getProgramSequenceChordVoicings().size());
  ASSERT_EQ(2, result.getProgramSequencePatterns().size());
  ASSERT_EQ(2, result.getProgramSequencePatternEvents().size());
  ASSERT_EQ(2, result.getProgramVoices().size());
  ASSERT_EQ(2, result.getProgramVoiceTracks().size());
  ASSERT_EQ(0, result.getProjects().size());
  ASSERT_EQ(1, result.getTemplates().size());
  ASSERT_EQ(1, result.getTemplateBindings().size());
}

TEST_F(ContentEntityStoreTest, ForTemplate_BoundToProgram) {
  subject.setTemplateBindings({ContentFixtures::buildTemplateBinding(&template2, &program1)});

  auto result = subject.forTemplate(&template2);

  // Assert the correct count of entities in the content store
  ASSERT_EQ(0, result.getInstruments().size());
  ASSERT_EQ(0, result.getInstrumentAudios().size());
  ASSERT_EQ(0, result.getInstrumentMemes().size());
  ASSERT_EQ(0, result.getLibraries().size());
  ASSERT_EQ(1, result.getPrograms().size());
  ASSERT_EQ(1, result.getProgramMemes().size());
  ASSERT_EQ(1, result.getProgramSequences().size());
  ASSERT_EQ(2, result.getProgramSequenceBindings().size());
  ASSERT_EQ(2, result.getProgramSequenceBindingMemes().size());
  ASSERT_EQ(2, result.getProgramSequenceChords().size());
  ASSERT_EQ(2, result.getProgramSequenceChordVoicings().size());
  ASSERT_EQ(0, result.getProgramSequencePatterns().size());
  ASSERT_EQ(0, result.getProgramSequencePatternEvents().size());
  ASSERT_EQ(1, result.getProgramVoices().size());
  ASSERT_EQ(0, result.getProgramVoiceTracks().size());
  ASSERT_EQ(0, result.getProjects().size());
  ASSERT_EQ(1, result.getTemplates().size());
  ASSERT_EQ(1, result.getTemplateBindings().size());
}

TEST_F(ContentEntityStoreTest, ForTemplate_BoundToInstrument) {
  subject.setTemplateBindings({ContentFixtures::buildTemplateBinding(&template2, &instrument1)});

  auto result = subject.forTemplate(&template2);

  // Assert the correct count of entities in the content store
  ASSERT_EQ(1, result.getInstruments().size());
  ASSERT_EQ(1, result.getInstrumentAudios().size());
  ASSERT_EQ(1, result.getInstrumentMemes().size());
  ASSERT_EQ(0, result.getLibraries().size());
  ASSERT_EQ(0, result.getPrograms().size());
  ASSERT_EQ(0, result.getProgramMemes().size());
  ASSERT_EQ(0, result.getProgramSequences().size());
  ASSERT_EQ(0, result.getProgramSequenceBindings().size());
  ASSERT_EQ(0, result.getProgramSequenceBindingMemes().size());
  ASSERT_EQ(0, result.getProgramSequenceChords().size());
  ASSERT_EQ(0, result.getProgramSequenceChordVoicings().size());
  ASSERT_EQ(0, result.getProgramSequencePatterns().size());
  ASSERT_EQ(0, result.getProgramSequencePatternEvents().size());
  ASSERT_EQ(0, result.getProgramVoices().size());
  ASSERT_EQ(0, result.getProgramVoiceTracks().size());
  ASSERT_EQ(0, result.getProjects().size());
  ASSERT_EQ(1, result.getTemplates().size());
  ASSERT_EQ(1, result.getTemplateBindings().size());
}

TEST_F(ContentEntityStoreTest, Clear) {
  subject.clear();

  // Assert the correct count of entities in the content store
  ASSERT_EQ(0, subject.getInstruments().size());
  ASSERT_EQ(0, subject.getInstrumentAudios().size());
  ASSERT_EQ(0, subject.getInstrumentMemes().size());
  ASSERT_EQ(0, subject.getLibraries().size());
  ASSERT_EQ(0, subject.getPrograms().size());
  ASSERT_EQ(0, subject.getProgramMemes().size());
  ASSERT_EQ(0, subject.getProgramSequences().size());
  ASSERT_EQ(0, subject.getProgramSequenceBindings().size());
  ASSERT_EQ(0, subject.getProgramSequenceBindingMemes().size());
  ASSERT_EQ(0, subject.getProgramSequenceChords().size());
  ASSERT_EQ(0, subject.getProgramSequenceChordVoicings().size());
  ASSERT_EQ(0, subject.getProgramSequencePatterns().size());
  ASSERT_EQ(0, subject.getProgramSequencePatternEvents().size());
  ASSERT_EQ(0, subject.getProgramVoices().size());
  ASSERT_EQ(0, subject.getProgramVoiceTracks().size());
  ASSERT_EQ(0, subject.getProjects().size());
  ASSERT_EQ(0, subject.getTemplates().size());
  ASSERT_EQ(0, subject.getTemplateBindings().size());
}

TEST_F(ContentEntityStoreTest, PutFromOtherContentEntityStore) {
  auto subject1 = ContentEntityStore();
  subject1.setProjects({project1});
  subject1.setLibraries({library1});
  subject1.setTemplates({template1, template2});
  subject1.setTemplateBindings({template1_binding});
  subject1.setInstruments({instrument1, instrument2});
  subject1.setInstrumentMemes({instrument1_meme, instrument2_meme});
  subject1.setInstrumentAudios({instrument1_audio, instrument2_audio});
  subject1.setPrograms({program1, program2});
  subject1.setProgramMemes({program1_meme, program2_meme});
  auto subject2 = ContentEntityStore();
  subject2.setProgramVoices({program1_voice, program2_voice});
  subject2.setProgramSequences({program1_sequence, program2_sequence});
  subject2.setProgramSequenceChords({program1_sequence_chord0, program1_sequence_chord1});
  subject2.setProgramSequenceChordVoicings({program1_sequence_chord0_voicing0, program1_sequence_chord1_voicing1});
  subject2.setProgramSequenceBindings({program1_sequence_binding1, program1_sequence_binding2});
  subject2.setProgramSequenceBindingMemes({program1_sequence_binding1_meme1, program1_sequence_binding1_meme2});
  subject2.setProgramSequencePatterns({program2_sequence_pattern1, program2_sequence_pattern2});
  subject2.setProgramVoiceTracks({program2_voice_track1, program2_voice_track2});
  subject2.setProgramSequencePatternEvents({program2_sequence_pattern1_event1, program2_sequence_pattern1_event2});

  auto subject3 = ContentEntityStore();
  subject3.put(&subject1);
  subject3.put(&subject2);

  // Assert the correct count of entities in the content store
  ASSERT_EQ(2, subject3.getInstruments().size());
  ASSERT_EQ(2, subject3.getInstrumentAudios().size());
  ASSERT_EQ(2, subject3.getInstrumentMemes().size());
  ASSERT_EQ(1, subject3.getLibraries().size());
  ASSERT_EQ(2, subject3.getPrograms().size());
  ASSERT_EQ(2, subject3.getProgramMemes().size());
  ASSERT_EQ(2, subject3.getProgramSequences().size());
  ASSERT_EQ(2, subject3.getProgramSequenceBindings().size());
  ASSERT_EQ(2, subject3.getProgramSequenceBindingMemes().size());
  ASSERT_EQ(2, subject3.getProgramSequenceChords().size());
  ASSERT_EQ(2, subject3.getProgramSequenceChordVoicings().size());
  ASSERT_EQ(2, subject3.getProgramSequencePatterns().size());
  ASSERT_EQ(2, subject3.getProgramSequencePatternEvents().size());
  ASSERT_EQ(2, subject3.getProgramVoices().size());
  ASSERT_EQ(2, subject3.getProgramVoiceTracks().size());
  ASSERT_EQ(1, subject3.getProjects().size());
  ASSERT_EQ(2, subject3.getTemplates().size());
  ASSERT_EQ(1, subject3.getTemplateBindings().size());
}

TEST_F(ContentEntityStoreTest, ToString) {
  auto subject1 = ContentEntityStore();
  subject1.setProjects({project1});
  subject1.setLibraries({library1});
  subject1.setTemplates({template1, template2});
  subject1.setTemplateBindings({template1_binding});
  subject1.setInstruments({instrument1, instrument2});
  subject1.setInstrumentMemes({instrument1_meme, instrument2_meme});
  subject1.setInstrumentAudios({instrument1_audio, instrument2_audio});
  subject1.setPrograms({program1, program2});
  subject1.setProgramMemes({program1_meme, program2_meme});
  auto subject2 = ContentEntityStore();
  subject2.setProgramVoices({program1_voice, program2_voice});
  subject2.setProgramSequences({program1_sequence, program2_sequence});
  subject2.setProgramSequenceChords({program1_sequence_chord0, program1_sequence_chord1});
  subject2.setProgramSequenceChordVoicings({program1_sequence_chord0_voicing0, program1_sequence_chord1_voicing1});
  subject2.setProgramSequenceBindings({program1_sequence_binding1, program1_sequence_binding2});
  subject2.setProgramSequenceBindingMemes({program1_sequence_binding1_meme1, program1_sequence_binding1_meme2});
  subject2.setProgramSequencePatterns({program2_sequence_pattern1, program2_sequence_pattern2});
  subject2.setProgramVoiceTracks({program2_voice_track1, program2_voice_track2});
  subject2.setProgramSequencePatternEvents({program2_sequence_pattern1_event1, program2_sequence_pattern1_event2});

  auto subject3 = ContentEntityStore();
  subject3.put(&subject1);
  subject3.put(&subject2);

  auto result = subject3.toString();

  ASSERT_FALSE(result.empty());
}