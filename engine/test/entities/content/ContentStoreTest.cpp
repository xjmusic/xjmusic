// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <fstream>
#include <string>

#include "xjmusic/entities/content/ContentStore.h"

#include "../../_helper/TestHelpers.h"
#include "../../_helper/ContentFixtures.h"

static std::string CONTENT_STORE_TEST_JSON_PATH = "_data/content_store_test.json";

using namespace XJ;

class ContentStoreTest : public ::testing::Test {

protected:
  ContentStore subject;

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

  // This function runs before each test
  void SetUp() override {
    // project
    project1 = ContentFixtures::buildProject();

    // Library content all created at this known time
    library1 = ContentFixtures::buildLibrary(project1, "test");

    // Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569
    template1 = ContentFixtures::buildTemplate(project1, "test1", TestHelpers::randomUUID());
    template1_binding = ContentFixtures::buildTemplateBinding(template1, library1);
    template2 = ContentFixtures::buildTemplate(project1, "test2", TestHelpers::randomUUID());

    // Instrument 1
    instrument1 = ContentFixtures::buildInstrument(library1, Instrument::Type::Drum, Instrument::Mode::Event,
                                                   Instrument::State::Published, "808 Drums");
    instrument1_meme = ContentFixtures::buildInstrumentMeme(instrument1, "Ants");
    instrument1_audio = ContentFixtures::buildInstrumentAudio(instrument1, "Chords Cm to D",
                                                              "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav",
                                                              0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f);

    // Instrument 2
    instrument2 = ContentFixtures::buildInstrument(library1, Instrument::Type::Pad, Instrument::Mode::Chord,
                                                   Instrument::State::Published, "Pad");
    instrument2_meme = ContentFixtures::buildInstrumentMeme(instrument2, "Peanuts");
    instrument2_audio = ContentFixtures::buildInstrumentAudio(instrument2, "Chord Fm",
                                                              "a0b9fg73k107s74kf9b4h8d9e009f7-g0e73982.wav",
                                                              0.02f, 1.123f, 140.0f, 0.52f, "BING", "F,A,C", 0.9f);

    // Program 1, main-type, has sequence with chords, bound to many offsets
    program1 = ContentFixtures::buildProgram(library1, Program::Type::Main, Program::State::Published, "leaves", "C#",
                                             120.4f);
    program1_meme = ContentFixtures::buildProgramMeme(program1, "Ants");
    program1_voice = ContentFixtures::buildProgramVoice(program1, Instrument::Type::Stripe, "Birds");
    program1_sequence = ContentFixtures::buildProgramSequence(program1, 8, "decay", 0.25f, "F#");
    program1_sequence_chord0 = ContentFixtures::buildProgramSequenceChord(program1_sequence, 0.0, "G minor");
    program1_sequence_chord1 = ContentFixtures::buildProgramSequenceChord(program1_sequence, 2.0, "A minor");
    program1_sequence_chord0_voicing0 = ContentFixtures::buildProgramSequenceChordVoicing(program1_sequence_chord0,
                                                                                          program1_voice, "G");
    program1_sequence_chord1_voicing1 = ContentFixtures::buildProgramSequenceChordVoicing(program1_sequence_chord1,
                                                                                          program1_voice, "Bb");
    program1_sequence_binding1 = ContentFixtures::buildProgramSequenceBinding(program1_sequence, 0);
    program1_sequence_binding2 = ContentFixtures::buildProgramSequenceBinding(program1_sequence, 5);
    program1_sequence_binding1_meme1 = ContentFixtures::buildProgramSequenceBindingMeme(program1_sequence_binding1,
                                                                                        "Gravel");
    program1_sequence_binding1_meme2 = ContentFixtures::buildProgramSequenceBindingMeme(program1_sequence_binding1,
                                                                                        "Road");

    // Program 2, beat-type, has unbound sequence with pattern with events
    program2 = ContentFixtures::buildProgram(library1, Program::Type::Beat, Program::State::Published, "coconuts",
                                             "F#",
                                             110.3f);
    program2_meme = ContentFixtures::buildProgramMeme(program2, "Bells");
    program2_voice = ContentFixtures::buildProgramVoice(program2, Instrument::Type::Drum, "Drums");
    program2_sequence = ContentFixtures::buildProgramSequence(program2, 16, "Base", 0.5f, "C");
    program2_sequence_pattern1 = ContentFixtures::buildProgramSequencePattern(program2_sequence, program2_voice, 16,
                                                                              "growth");
    program2_sequence_pattern2 = ContentFixtures::buildProgramSequencePattern(program2_sequence, program2_voice, 12,
                                                                              "decay");
    program2_voice_track1 = ContentFixtures::buildProgramVoiceTrack(program2_voice, "BOOM");
    program2_voice_track2 = ContentFixtures::buildProgramVoiceTrack(program2_voice, "SMACK");
    program2_sequence_pattern1_event1 = ContentFixtures::buildProgramSequencePatternEvent(program2_sequence_pattern1,
                                                                                          program2_voice_track1, 0.0f,
                                                                                          1.0f, "C", 1.0f);
    program2_sequence_pattern1_event2 = ContentFixtures::buildProgramSequencePatternEvent(program2_sequence_pattern1,
                                                                                          program2_voice_track2, 0.5f,
                                                                                          1.1f, "D", 0.9f);

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

  // You can also define a TearDown() function that runs after each test
  void TearDown() override {
    // Cleanup code here...
  }
};

TEST_F(ContentStoreTest, FromJsonFile) {
  // Load the JSON file
  std::ifstream file(CONTENT_STORE_TEST_JSON_PATH);
  ASSERT_TRUE(file.is_open());

  // Deserialize a content store from a JSON file stream
  subject = ContentStore::fromJson(file);

  // Assert the correct count of entities in the content store
  ASSERT_EQ(2, subject.getInstruments().size());
  ASSERT_EQ(2, subject.getInstrumentAudios().size());
  ASSERT_EQ(2, subject.getInstrumentMemes().size());
  ASSERT_EQ(1, subject.getLibraries().size());
  ASSERT_EQ(2, subject.getPrograms().size());
  ASSERT_EQ(2, subject.getProgramMemes().size());
  ASSERT_EQ(2, subject.getProgramSequences().size());
  ASSERT_EQ(2, subject.getProgramSequenceBindings().size());
  ASSERT_EQ(2, subject.getProgramSequenceBindingMemes().size());
  ASSERT_EQ(2, subject.getProgramSequenceChords().size());
  ASSERT_EQ(2, subject.getProgramSequenceChordVoicings().size());
  ASSERT_EQ(2, subject.getProgramSequencePatterns().size());
  ASSERT_EQ(2, subject.getProgramSequencePatternEvents().size());
  ASSERT_EQ(2, subject.getProgramVoices().size());
  ASSERT_EQ(2, subject.getProgramVoiceTracks().size());
  ASSERT_EQ(1, subject.getProjects().size());
  ASSERT_EQ(2, subject.getTemplates().size());
  ASSERT_EQ(3, subject.getTemplateBindings().size());
}

TEST_F(ContentStoreTest, FromJsonString) {
  // Load the JSON file
  std::ifstream file(CONTENT_STORE_TEST_JSON_PATH);
  ASSERT_TRUE(file.is_open());
  std::string jsonString((std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());

  // Deserialize a content store from a JSON file stream
  ContentStore subject = ContentStore::fromJson(jsonString);

  // Assert the correct count of entities in the content store
  ASSERT_EQ(2, subject.getInstruments().size());
  ASSERT_EQ(2, subject.getInstrumentAudios().size());
  ASSERT_EQ(2, subject.getInstrumentMemes().size());
  ASSERT_EQ(1, subject.getLibraries().size());
  ASSERT_EQ(2, subject.getPrograms().size());
  ASSERT_EQ(2, subject.getProgramMemes().size());
  ASSERT_EQ(2, subject.getProgramSequences().size());
  ASSERT_EQ(2, subject.getProgramSequenceBindings().size());
  ASSERT_EQ(2, subject.getProgramSequenceBindingMemes().size());
  ASSERT_EQ(2, subject.getProgramSequenceChords().size());
  ASSERT_EQ(2, subject.getProgramSequenceChordVoicings().size());
  ASSERT_EQ(2, subject.getProgramSequencePatterns().size());
  ASSERT_EQ(2, subject.getProgramSequencePatternEvents().size());
  ASSERT_EQ(2, subject.getProgramVoices().size());
  ASSERT_EQ(2, subject.getProgramVoiceTracks().size());
  ASSERT_EQ(1, subject.getProjects().size());
  ASSERT_EQ(2, subject.getTemplates().size());
  ASSERT_EQ(3, subject.getTemplateBindings().size());
}

TEST_F(ContentStoreTest, SetProgramGetProgram) {
  ContentStore subject;

  // Create an Entity object
  Program program;
  program.id = "fd472031-dd6e-47ae-b0b7-ae4f02b94726";
  program.libraryId = "7ec2d282-d481-4fee-b57b-082e90284102";
  program.state = Program::State::Draft;
  program.type = Program::Type::Main;
  program.key = "C";
  program.tempo = 120.0f;
  program.name = "Test Entity";
  program.config = R"(barBeats = 4)";
  program.isDeleted = false;
  program.updatedAt = 1711089919558;

  // Put the Entity object into the ContentStore
  subject.setPrograms({program});

  // Retrieve the Entity object from the ContentStore
  auto retrievedEntity = subject.getProgram("fd472031-dd6e-47ae-b0b7-ae4f02b94726");

  // Assert that the retrieved Entity object is equal to the original Entity object
  ASSERT_TRUE(retrievedEntity.has_value());
  ASSERT_EQ(program.id, retrievedEntity.value()->id);
}

TEST_F(ContentStoreTest, GetInstrumentTypeOfEvent) {
  ASSERT_EQ(Instrument::Type::Drum, subject.getInstrumentTypeOfEvent(program2_sequence_pattern1_event1));
}

TEST_F(ContentStoreTest, HasInstrumentsOfMode) {
  ASSERT_FALSE(subject.hasInstrumentsOfMode(Instrument::Mode::Loop));
  ASSERT_TRUE(subject.hasInstrumentsOfMode(Instrument::Mode::Event));
}

TEST_F(ContentStoreTest, HasInstrumentsOfType) {
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Bass));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Hook));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Percussion));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Stab));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Sticky));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Stripe));
  ASSERT_TRUE(subject.hasInstrumentsOfType(Instrument::Type::Drum));
  ASSERT_TRUE(subject.hasInstrumentsOfType(Instrument::Type::Pad));
}

TEST_F(ContentStoreTest, HasInstrumentsOfTypeAndMode) {
  ASSERT_TRUE(subject.hasInstrumentsOfTypeAndMode(Instrument::Type::Drum, Instrument::Mode::Event));
  ASSERT_FALSE(subject.hasInstrumentsOfTypeAndMode(Instrument::Type::Drum, Instrument::Mode::Loop));
}

TEST_F(ContentStoreTest, GetAvailableOffsets) {
  auto result = subject.getAvailableOffsets(program1_sequence_binding1);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getAudiosOfInstrumentId) {
  auto result = subject.getAudiosOfInstrument(instrument1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getAudiosOfInstrument) {
  auto result = subject.getAudiosOfInstrument(instrument2);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getBindingsOfSequence) {
  auto result = subject.getBindingsOfSequence(program1_sequence);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getBindingsOfSequenceId) {
  auto result = subject.getBindingsOfSequence(program1_sequence.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getSequenceBindingMemesOfProgram) {
  auto result = subject.getSequenceBindingMemesOfProgram(program1);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getSequenceBindingMemesOfProgramId) {
  auto result = subject.getSequenceBindingMemesOfProgram(program1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getBindingsAtOffsetOfProgram) {
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(program1, 0, false).size());
  ASSERT_EQ(0, subject.getBindingsAtOffsetOfProgram(program1, 1, false).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(program1, 0, true).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(program1, 1, true).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(program1, 2, true).size());
}

TEST_F(ContentStoreTest, getBindingsAtOffsetOfProgramId) {
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(program1.id, 0, false).size());
  ASSERT_EQ(0, subject.getBindingsAtOffsetOfProgram(program1.id, 1, false).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(program1.id, 0, true).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(program1.id, 1, true).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(program1.id, 2, true).size());
}

TEST_F(ContentStoreTest, getChordsOfSequence) {
  auto result = subject.getChordsOfSequence(program1_sequence);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getChordsOfSequenceId) {
  auto result = subject.getChordsOfSequence(program1_sequence.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getEventsOfPattern) {
  auto result = subject.getEventsOfPattern(program2_sequence_pattern1);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getEventsOfPatternId) {
  auto result = subject.getEventsOfPattern(program2_sequence_pattern1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getEventsOfTrack) {
  auto result = subject.getEventsOfTrack(program2_voice_track1);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getEventsOfTrackId) {
  auto result = subject.getEventsOfTrack(program2_voice_track1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getEventsOfPatternAndTrack) {
  auto result = subject.getEventsOfPatternAndTrack(program2_sequence_pattern1, program2_voice_track1);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getEventsOfPatternAndTrackId) {
  auto result = subject.getEventsOfPatternAndTrack(program2_sequence_pattern1.id, program2_voice_track1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getInstrument) {
  auto result = subject.getInstrument(instrument1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(instrument1.id, result.value()->id);
}

TEST_F(ContentStoreTest, getInstrumentAudio) {
  auto result = subject.getInstrumentAudio(instrument1_audio.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(instrument1_audio.id, result.value()->id);
}

TEST_F(ContentStoreTest, getAudiosOfInstrumentTypesAndModes) {
  auto result = subject.getAudiosOfInstrumentTypesAndModes({Instrument::Type::Drum}, {Instrument::Mode::Event});

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getAudiosOfInstrumentTypes) {
  auto result = subject.getAudiosOfInstrumentTypes({Instrument::Type::Drum});

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getInstrumentAudios) {
  auto result = subject.getInstrumentAudios();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getMemesOfInstrumentId) {
  auto result = subject.getMemesOfInstrument(instrument1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getInstrumentMemes) {
  auto result = subject.getInstrumentMemes();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getInstruments) {
  auto result = subject.getInstruments();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getInstrumentsOfLibrary) {
  auto result = subject.getInstrumentsOfLibrary(library1);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getInstrumentsOfLibraryId) {
  auto result = subject.getInstrumentsOfLibrary(library1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getInstrumentsOfTypes) {
  auto result = subject.getInstrumentsOfTypes({Instrument::Type::Drum});

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getInstrumentTypeOfAudioId) {
  auto result = subject.getInstrumentTypeOfAudio(instrument1_audio.id);

  ASSERT_EQ(Instrument::Type::Drum, result);
}

TEST_F(ContentStoreTest, getMemesOfProgramId) {
  auto result = subject.getMemesOfProgram(program1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getMemesAtBeginning) {
  auto result = subject.getMemesAtBeginning(program1);

  ASSERT_EQ(3, result.size());
}

TEST_F(ContentStoreTest, getPatternIdOfEventId) {
  auto result = subject.getPatternIdOfEvent(program2_sequence_pattern1_event1.id);

  ASSERT_EQ(program2_sequence_pattern1.id, result);
}

TEST_F(ContentStoreTest, getPatternsOfSequence) {
  auto result = subject.getPatternsOfSequence(program2_sequence);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getPatternsOfSequenceId) {
  auto result = subject.getPatternsOfSequence(program2_sequence.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getPatternsOfVoice) {
  auto result = subject.getPatternsOfVoice(program2_voice);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getPatternsOfVoiceId) {
  auto result = subject.getPatternsOfVoice(program2_voice.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getProgram) {
  auto result = subject.getProgram(program1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1.id, result.value()->id);
}

TEST_F(ContentStoreTest, getPrograms) {
  auto result = subject.getPrograms();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getProgramsOfLibrary) {
  auto result = subject.getProgramsOfLibrary(library1);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getProgramsOfLibraryId) {
  auto result = subject.getProgramsOfLibrary(library1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getProgramMemes) {
  auto result = subject.getProgramMemes();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getProgramSequence) {
  auto result = subject.getProgramSequence(program1_sequence.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_sequence.id, result.value()->id);
}

TEST_F(ContentStoreTest, getSequenceOfBinding) {
  auto result = subject.getSequenceOfBinding(program1_sequence_binding1);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_sequence.id, result.value()->id);
}

TEST_F(ContentStoreTest, getProgramSequences) {
  auto result = subject.getProgramSequences();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getSequencesOfProgramId) {
  auto result = subject.getSequencesOfProgram(program1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getProgramSequenceBinding) {
  auto result = subject.getProgramSequenceBinding(program1_sequence_binding1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_sequence_binding1.id, result.value()->id);
}

TEST_F(ContentStoreTest, getProgramSequenceBindings) {
  auto result = subject.getProgramSequenceBindings();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getSequenceBindingsOfProgram) {
  auto result = subject.getSequenceBindingsOfProgram(program1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getProgramSequenceBindingMemes) {
  auto result = subject.getProgramSequenceBindingMemes();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getMemesOfSequenceBinding) {
  auto result = subject.getMemesOfSequenceBinding(program1_sequence_binding1);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getMemesOfSequenceBindingId) {
  auto result = subject.getMemesOfSequenceBinding(program1_sequence_binding1.id);

  ASSERT_EQ(2, result.size());
}


TEST_F(ContentStoreTest, getProgramSequencePattern) {
  auto result = subject.getProgramSequencePattern(program2_sequence_pattern1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program2_sequence_pattern1.id, result.value()->id);
}

TEST_F(ContentStoreTest, getProgramSequencePatterns) {
  auto result = subject.getProgramSequencePatterns();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getProgramSequencePatternEvent) {
  auto result = subject.getProgramSequencePatternEvent(program2_sequence_pattern1_event1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program2_sequence_pattern1_event1.id, result.value()->id);
}

TEST_F(ContentStoreTest, getProgramSequencePatternEvents) {
  auto result = subject.getProgramSequencePatternEvents();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getProgramSequenceChord) {
  auto result = subject.getProgramSequenceChord(program1_sequence_chord0.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_sequence_chord0.id, result.value()->id);
}

TEST_F(ContentStoreTest, getProgramSequenceChords) {
  auto result = subject.getProgramSequenceChords();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, testGetProgramSequenceChords) {
  auto result = subject.getSequenceChordsOfProgram(program1_sequence.programId);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getProgramSequenceChordVoicings) {
  auto result = subject.getProgramSequenceChordVoicings();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getProgramVoice) {
  auto result = subject.getProgramVoice(program1_voice.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_voice.id, result.value()->id);
}

TEST_F(ContentStoreTest, getProgramVoices) {
  auto result = subject.getProgramVoices();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getProgramVoiceTrack) {
  auto result = subject.getProgramVoiceTrack(program2_voice_track1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program2_voice_track1.id, result.value()->id);
}

TEST_F(ContentStoreTest, getProgramVoiceTracks) {
  auto result = subject.getProgramVoiceTracks();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getTracksOfProgramId) {
  auto result = subject.getTracksOfProgram(program2.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getTracksOfVoice) {
  auto result = subject.getTracksOfVoice(program2_voice);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getTracksOfVoiceId) {
  auto result = subject.getTracksOfVoice(program2_voice.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getTemplates) {
  auto result = subject.getTemplates();

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getTemplate) {
  auto result = subject.getTemplate(template1.id);

  ASSERT_EQ(template1.id, result.value()->id);
}

TEST_F(ContentStoreTest, getTemplateBindings) {
  auto result = subject.getTemplateBindings();

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getBindingsOfTemplate) {
  auto result = subject.getBindingsOfTemplate(template1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getLibrary) {
  auto result = subject.getLibrary(library1.id);

  ASSERT_EQ(library1.id, result.value()->id);
}

TEST_F(ContentStoreTest, getLibraries) {
  auto result = subject.getLibraries();

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getProject) {
  auto result = subject.getProject();

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(project1.id, result.value()->id);
}

TEST_F(ContentStoreTest, getTrackOfEvent) {
  auto result = subject.getTrackOfEvent(program2_sequence_pattern1_event1);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program2_voice_track1.id, result.value()->id);
}

TEST_F(ContentStoreTest, getTrackNames) {
  auto result = subject.getTrackNamesOfVoice(program2_voice);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getVoicingsOfChord) {
  auto result = subject.getVoicingsOfChord(program1_sequence_chord0);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getVoicingsOfChordId) {
  auto result = subject.getVoicingsOfChord(program1_sequence_chord0.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getSequenceChordVoicingsOfProgram) {
  auto result = subject.getSequenceChordVoicingsOfProgram(program1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getVoicingsOfChordAndVoice) {
  auto result = subject.getVoicingsOfChordAndVoice(program1_sequence_chord0, program1_voice);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getVoicingsOfChordIdAndVoiceId) {
  auto result = subject.getVoicingsOfChordAndVoice(program1_sequence_chord0.id, program1_voice.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getVoiceOfEvent) {
  auto result = subject.getVoiceOfEvent(program2_sequence_pattern1_event1);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program2_voice.id, result.value()->id);
}

TEST_F(ContentStoreTest, getVoicesOfProgram) {
  auto result = subject.getVoicesOfProgram(program2);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getVoicesOfProgramId) {
  auto result = subject.getVoicesOfProgram(program2.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, setInstruments) {
  subject.setInstruments({});

  ASSERT_TRUE(subject.getInstruments().empty());
}

TEST_F(ContentStoreTest, setInstrumentAudios) {
  subject.setInstrumentAudios({});

  ASSERT_TRUE(subject.getInstrumentAudios().empty());
}

TEST_F(ContentStoreTest, setInstrumentMemes) {
  subject.setInstrumentMemes({});

  ASSERT_TRUE(subject.getInstrumentMemes().empty());
}

TEST_F(ContentStoreTest, setLibraries) {
  subject.setLibraries({});

  ASSERT_TRUE(subject.getLibraries().empty());
}

TEST_F(ContentStoreTest, setPrograms) {
  subject.setPrograms({});

  ASSERT_TRUE(subject.getPrograms().empty());
}

TEST_F(ContentStoreTest, setProgramMemes) {
  subject.setProgramMemes({});

  ASSERT_TRUE(subject.getProgramMemes().empty());
}

TEST_F(ContentStoreTest, setProgramSequences) {
  subject.setProgramSequences({});

  ASSERT_TRUE(subject.getProgramSequences().empty());
}

TEST_F(ContentStoreTest, setProgramSequenceBindings) {
  subject.setProgramSequenceBindings({});

  ASSERT_TRUE(subject.getProgramSequenceBindings().empty());
}

TEST_F(ContentStoreTest, setProgramSequenceBindingMemes) {
  subject.setProgramSequenceBindingMemes({});

  ASSERT_TRUE(subject.getProgramSequenceBindingMemes().empty());
}

TEST_F(ContentStoreTest, setProgramSequenceChords) {
  subject.setProgramSequenceChords({});

  ASSERT_TRUE(subject.getProgramSequenceChords().empty());
}

TEST_F(ContentStoreTest, setProgramSequenceChordVoicings) {
  subject.setProgramSequenceChordVoicings({});

  ASSERT_TRUE(subject.getProgramSequenceChordVoicings().empty());
}

TEST_F(ContentStoreTest, setProgramSequencePatterns) {
  subject.setProgramSequencePatterns({});

  ASSERT_TRUE(subject.getProgramSequencePatterns().empty());
}

TEST_F(ContentStoreTest, setProgramSequencePatternEvents) {
  subject.setProgramSequencePatternEvents({});

  ASSERT_TRUE(subject.getProgramSequencePatternEvents().empty());
}

TEST_F(ContentStoreTest, setProgramVoices) {
  subject.setProgramVoices({});

  ASSERT_TRUE(subject.getProgramVoices().empty());
}

TEST_F(ContentStoreTest, setProgramVoiceTracks) {
  subject.setProgramVoiceTracks({});

  ASSERT_TRUE(subject.getProgramVoiceTracks().empty());
}

TEST_F(ContentStoreTest, setProjects) {
  auto project2 = ContentFixtures::buildProject();
  subject.setProjects({project2});

  auto result = subject.getProject();

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(result.value()->id, project2.id);
}

TEST_F(ContentStoreTest, setTemplates) {
  subject.setTemplates({});

  ASSERT_TRUE(subject.getTemplates().empty());
}

TEST_F(ContentStoreTest, setTemplateBindings) {
  subject.setTemplateBindings({});

  ASSERT_TRUE(subject.getTemplateBindings().empty());
}


TEST_F(ContentStoreTest, getSequencePatternEventsOfProgram) {
  auto result = subject.getSequencePatternEventsOfProgram(program2.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getEventsOfProgramId) {
  auto result = subject.getSequencePatternEventsOfProgram(program2_sequence_pattern1.programId);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getSequencePatternsOfProgram) {
  auto result = subject.getSequencePatternsOfProgram(program2);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getPatternsOfProgramId) {
  auto result = subject.getSequencePatternsOfProgram(program2_sequence_pattern1.programId);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getInstrumentsOfType) {
  auto result = subject.getInstrumentsOfType(Instrument::Type::Drum);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getInstrumentsOfTypesAndModes) {
  auto result = subject.getInstrumentsOfTypesAndModes({Instrument::Type::Drum}, {Instrument::Mode::Event});

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getInstrumentTypeOfAudio) {
  auto result = subject.getInstrumentTypeOfAudio(instrument1_audio.id);

  ASSERT_EQ(Instrument::Type::Drum, result);
}

TEST_F(ContentStoreTest, getMemesOfInstrument) {
  auto result = subject.getMemesOfInstrument(instrument1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getPatternIdOfEvent) {
  auto result = subject.getPatternIdOfEvent(program2_sequence_pattern1_event1.id);

  ASSERT_EQ(program2_sequence_pattern1.id, result);
}

TEST_F(ContentStoreTest, getProgramsOfType) {
  auto result = subject.getProgramsOfType(Program::Type::Main);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getMemesOfProgram) {
  auto result = subject.getMemesOfProgram(program1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getSequencesOfProgram) {
  auto result = subject.getSequencesOfProgram(program1.id);

  ASSERT_EQ(1, result.size());
}

TEST_F(ContentStoreTest, getSequenceChordsOfProgram) {
  auto result = subject.getSequenceChordsOfProgram(program1.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getTracksOfProgram) {
  auto result = subject.getTracksOfProgram(program2.id);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getTracksOfProgramType) {
  auto result = subject.getTracksOfProgramType(Program::Type::Beat);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getTemplateBinding) {
  auto result = subject.getTemplateBinding(template1_binding.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(template1_binding.id, result.value()->id);
}

TEST_F(ContentStoreTest, getTrackNamesOfVoice) {
  auto result = subject.getTrackNamesOfVoice(program2_voice);

  ASSERT_EQ(2, result.size());
}

TEST_F(ContentStoreTest, getInstrumentMeme) {
  auto result = subject.getInstrumentMeme(instrument1_meme.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(instrument1_meme.id, result.value()->id);
}

TEST_F(ContentStoreTest, getProgramMeme) {
  auto result = subject.getProgramMeme(program1_meme.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_meme.id, result.value()->id);
}

TEST_F(ContentStoreTest, getProgramSequenceBindingMeme) {
  auto result = subject.getProgramSequenceBindingMeme(program1_sequence_binding1_meme1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_sequence_binding1_meme1.id, result.value()->id);
}

TEST_F(ContentStoreTest, getProgramSequenceChordVoicing) {
  auto result = subject.getProgramSequenceChordVoicing(program1_sequence_chord0_voicing0.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(program1_sequence_chord0_voicing0.id, result.value()->id);
}

TEST_F(ContentStoreTest, getPatternsOfSequenceAndVoice) {
  ASSERT_EQ(2, subject.getPatternsOfSequenceAndVoice(program2_sequence.id, program2_voice.id).size());
  ASSERT_EQ(0, subject.getPatternsOfSequenceAndVoice(TestHelpers::randomUUID(), program2_voice.id).size());
  ASSERT_EQ(0, subject.getPatternsOfSequenceAndVoice(program2_sequence.id, TestHelpers::randomUUID()).size());
}

TEST_F(ContentStoreTest, ForTemplate_BoundToLibrary) {
  auto result = subject.forTemplate(template1);

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

TEST_F(ContentStoreTest, ForTemplate_BoundToProgram) {
  subject.setTemplateBindings({ContentFixtures::buildTemplateBinding(template2, program1)});

  auto result = subject.forTemplate(template2);

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

TEST_F(ContentStoreTest, ForTemplate_BoundToInstrument) {
  subject.setTemplateBindings({ContentFixtures::buildTemplateBinding(template2, instrument1)});

  auto result = subject.forTemplate(template2);

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
