// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <fstream>
#include <gtest/gtest.h>
#include <string>

#include "xjnexus/content/ContentStore.h"

#include "../_helper/ContentTestHelper.h"

static std::string CONTENT_STORE_TEST_JSON_PATH = "_data/content_store_test.json";

using namespace Content;

TEST(ContentStoreTest, FromJsonFile) {
  // Load the JSON file
  std::ifstream file(CONTENT_STORE_TEST_JSON_PATH);
  ASSERT_TRUE(file.is_open());

  // Deserialize a content store from a JSON file stream
  ContentStore subject = ContentStore::fromJson(file);

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

TEST(ContentStoreTest, FromJsonString) {
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

TEST(ContentStoreTest, SetProgramGetProgram) {
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

TEST(ContentStoreTest, GetInstrumentTypeOfEvent) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  ASSERT_EQ(Instrument::Type::Drum, subject.getInstrumentTypeOfEvent(test.program2_sequence_pattern1_event1));
}

TEST(ContentStoreTest, HasInstrumentsOfMode) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  ASSERT_FALSE(subject.hasInstrumentsOfMode(Instrument::Mode::Loop));
  ASSERT_TRUE(subject.hasInstrumentsOfMode(Instrument::Mode::Event));
}

TEST(ContentStoreTest, HasInstrumentsOfType) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Bass));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Hook));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Percussion));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Stab));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Sticky));
  ASSERT_FALSE(subject.hasInstrumentsOfType(Instrument::Type::Stripe));
  ASSERT_TRUE(subject.hasInstrumentsOfType(Instrument::Type::Drum));
  ASSERT_TRUE(subject.hasInstrumentsOfType(Instrument::Type::Pad));
}

TEST(ContentStoreTest, HasInstrumentsOfTypeAndMode) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  ASSERT_TRUE(subject.hasInstrumentsOfTypeAndMode(Instrument::Type::Drum, Instrument::Mode::Event));
  ASSERT_FALSE(subject.hasInstrumentsOfTypeAndMode(Instrument::Type::Drum, Instrument::Mode::Loop));
}

TEST(ContentStoreTest, GetAvailableOffsets) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getAvailableOffsets(test.program1_sequence_binding1);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getAudiosOfInstrumentId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getAudiosOfInstrument(test.instrument1.id);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getAudiosOfInstrument) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getAudiosOfInstrument(test.instrument2);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getBindingsOfSequence) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getBindingsOfSequence(test.program1_sequence);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getBindingsOfSequenceId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getBindingsOfSequence(test.program1_sequence.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getSequenceBindingMemesOfProgram) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getSequenceBindingMemesOfProgram(test.program1);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getSequenceBindingMemesOfProgramId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getSequenceBindingMemesOfProgram(test.program1.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getBindingsAtOffsetOfProgram) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(test.program1, 0, false).size());
  ASSERT_EQ(0, subject.getBindingsAtOffsetOfProgram(test.program1, 1, false).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(test.program1, 0, true).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(test.program1, 1, true).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(test.program1, 2, true).size());
}

TEST(ContentStoreTest, getBindingsAtOffsetOfProgramId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(test.program1.id, 0, false).size());
  ASSERT_EQ(0, subject.getBindingsAtOffsetOfProgram(test.program1.id, 1, false).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(test.program1.id, 0, true).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(test.program1.id, 1, true).size());
  ASSERT_EQ(1, subject.getBindingsAtOffsetOfProgram(test.program1.id, 2, true).size());
}

TEST(ContentStoreTest, getChordsOfSequence) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getChordsOfSequence(test.program1_sequence);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getChordsOfSequenceId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getChordsOfSequence(test.program1_sequence.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getEventsOfPattern) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getEventsOfPattern(test.program2_sequence_pattern1);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getEventsOfPatternId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getEventsOfPattern(test.program2_sequence_pattern1.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getEventsOfTrack) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getEventsOfTrack(test.program2_voice_track1);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getEventsOfTrackId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getEventsOfTrack(test.program2_voice_track1.id);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getEventsOfPatternAndTrack) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getEventsOfPatternAndTrack(test.program2_sequence_pattern1, test.program2_voice_track1);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getEventsOfPatternAndTrackId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getEventsOfPatternAndTrack(test.program2_sequence_pattern1.id, test.program2_voice_track1.id);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getInstrument) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getInstrument(test.instrument1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.instrument1.id, result.value()->id);
}

TEST(ContentStoreTest, getInstrumentAudio) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getInstrumentAudio(test.instrument1_audio.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.instrument1_audio.id, result.value()->id);
}

TEST(ContentStoreTest, getAudiosOfInstrumentTypesAndModes) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getAudiosOfInstrumentTypesAndModes({Instrument::Type::Drum}, {Instrument::Mode::Event});

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getAudiosOfInstrumentTypes) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getAudiosOfInstrumentTypes({Instrument::Type::Drum});

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getInstrumentAudios) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getInstrumentAudios();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getMemesOfInstrumentId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getMemesOfInstrument(test.instrument1.id);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getInstrumentMemes) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getInstrumentMemes();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getInstruments) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getInstruments();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getInstrumentsOfLibrary) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getInstrumentsOfLibrary(test.library1);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getInstrumentsOfLibraryId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getInstrumentsOfLibrary(test.library1.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getInstrumentsOfTypes) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getInstrumentsOfTypes({Instrument::Type::Drum});

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getInstrumentTypeOfAudioId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getInstrumentTypeOfAudio(test.instrument1_audio.id);

  ASSERT_EQ(Instrument::Type::Drum, result);
}

TEST(ContentStoreTest, getMemesOfProgramId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getMemesOfProgram(test.program1.id);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getMemesAtBeginning) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getMemesAtBeginning(test.program1);

  ASSERT_EQ(3, result.size());
}

TEST(ContentStoreTest, getPatternIdOfEventId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getPatternIdOfEvent(test.program2_sequence_pattern1_event1.id);

  ASSERT_EQ(test.program2_sequence_pattern1.id, result);
}

TEST(ContentStoreTest, getPatternsOfSequence) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getPatternsOfSequence(test.program2_sequence);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getPatternsOfSequenceId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getPatternsOfSequence(test.program2_sequence.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getPatternsOfVoice) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getPatternsOfVoice(test.program2_voice);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getPatternsOfVoiceId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getPatternsOfVoice(test.program2_voice.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getProgram) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgram(test.program1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program1.id, result.value()->id);
}

TEST(ContentStoreTest, getPrograms) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getPrograms();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getProgramsOfLibrary) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramsOfLibrary(test.library1);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getProgramsOfLibraryId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramsOfLibrary(test.library1.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getProgramMemes) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramMemes();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getProgramSequence) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramSequence(test.program1_sequence.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program1_sequence.id, result.value()->id);
}

TEST(ContentStoreTest, getSequenceOfBinding) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getSequenceOfBinding(test.program1_sequence_binding1);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program1_sequence.id, result.value()->id);
}

TEST(ContentStoreTest, getProgramSequences) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramSequences();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getSequencesOfProgramId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getSequencesOfProgram(test.program1.id);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getProgramSequenceBinding) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramSequenceBinding(test.program1_sequence_binding1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program1_sequence_binding1.id, result.value()->id);
}

TEST(ContentStoreTest, getProgramSequenceBindings) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramSequenceBindings();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getSequenceBindingsOfProgram) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getSequenceBindingsOfProgram(test.program1.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getProgramSequenceBindingMemes) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramSequenceBindingMemes();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getMemesOfSequenceBinding) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getMemesOfSequenceBinding(test.program1_sequence_binding1);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getMemesOfSequenceBindingId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getMemesOfSequenceBinding(test.program1_sequence_binding1.id);

  ASSERT_EQ(2, result.size());
}


TEST(ContentStoreTest, getProgramSequencePattern) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramSequencePattern(test.program2_sequence_pattern1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program2_sequence_pattern1.id, result.value()->id);
}

TEST(ContentStoreTest, getProgramSequencePatterns) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramSequencePatterns();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getProgramSequencePatternEvent) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramSequencePatternEvent(test.program2_sequence_pattern1_event1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program2_sequence_pattern1_event1.id, result.value()->id);
}

TEST(ContentStoreTest, getProgramSequencePatternEvents) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramSequencePatternEvents();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getProgramSequenceChord) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramSequenceChord(test.program1_sequence_chord0.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program1_sequence_chord0.id, result.value()->id);
}

TEST(ContentStoreTest, getProgramSequenceChords) {
  ContentTestHelper test;
  ContentStore &subject = test.store;
  auto result = subject.getProgramSequenceChords();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, testGetProgramSequenceChords) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getSequenceChordsOfProgram(test.program1_sequence.programId);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getProgramSequenceChordVoicings) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramSequenceChordVoicings();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getProgramVoice) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramVoice(test.program1_voice.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program1_voice.id, result.value()->id);
}

TEST(ContentStoreTest, getProgramVoices) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramVoices();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getProgramVoiceTrack) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramVoiceTrack(test.program2_voice_track1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program2_voice_track1.id, result.value()->id);
}

TEST(ContentStoreTest, getProgramVoiceTracks) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramVoiceTracks();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getTracksOfProgramId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getTracksOfProgram(test.program2.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getTracksOfVoice) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getTracksOfVoice(test.program2_voice);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getTracksOfVoiceId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getTracksOfVoice(test.program2_voice.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getTemplates) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getTemplates();

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getTemplate) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getTemplate(test.template1.id);

  ASSERT_EQ(test.template1.id, result.value()->id);
}

TEST(ContentStoreTest, getTemplateBindings) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getTemplateBindings();

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getBindingsOfTemplate) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getBindingsOfTemplate(test.template1.id);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getLibrary) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getLibrary(test.library1.id);

  ASSERT_EQ(test.library1.id, result.value()->id);
}

TEST(ContentStoreTest, getLibraries) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getLibraries();

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getProject) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProject();

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.project1.id, result.value()->id);
}

TEST(ContentStoreTest, getTrackOfEvent) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getTrackOfEvent(test.program2_sequence_pattern1_event1);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program2_voice_track1.id, result.value()->id);
}

TEST(ContentStoreTest, getTrackNames) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getTrackNamesOfVoice(test.program2_voice);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getVoicingsOfChord) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getVoicingsOfChord(test.program1_sequence_chord0);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getVoicingsOfChordId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getVoicingsOfChord(test.program1_sequence_chord0.id);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getSequenceChordVoicingsOfProgram) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getSequenceChordVoicingsOfProgram(test.program1.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getVoicingsOfChordAndVoice) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getVoicingsOfChordAndVoice(test.program1_sequence_chord0, test.program1_voice);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getVoicingsOfChordIdAndVoiceId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getVoicingsOfChordAndVoice(test.program1_sequence_chord0.id, test.program1_voice.id);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getVoiceOfEvent) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getVoiceOfEvent(test.program2_sequence_pattern1_event1);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program2_voice.id, result.value()->id);
}

TEST(ContentStoreTest, getVoicesOfProgram) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getVoicesOfProgram(test.program2);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getVoicesOfProgramId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getVoicesOfProgram(test.program2.id);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, setInstruments) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setInstruments({});

  ASSERT_TRUE(subject.getInstruments().empty());
}

TEST(ContentStoreTest, setInstrumentAudios) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setInstrumentAudios({});

  ASSERT_TRUE(subject.getInstrumentAudios().empty());
}

TEST(ContentStoreTest, setInstrumentMemes) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setInstrumentMemes({});

  ASSERT_TRUE(subject.getInstrumentMemes().empty());
}

TEST(ContentStoreTest, setLibraries) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setLibraries({});

  ASSERT_TRUE(subject.getLibraries().empty());
}

TEST(ContentStoreTest, setPrograms) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setPrograms({});

  ASSERT_TRUE(subject.getPrograms().empty());
}

TEST(ContentStoreTest, setProgramMemes) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setProgramMemes({});

  ASSERT_TRUE(subject.getProgramMemes().empty());
}

TEST(ContentStoreTest, setProgramSequences) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setProgramSequences({});

  ASSERT_TRUE(subject.getProgramSequences().empty());
}

TEST(ContentStoreTest, setProgramSequenceBindings) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setProgramSequenceBindings({});

  ASSERT_TRUE(subject.getProgramSequenceBindings().empty());
}

TEST(ContentStoreTest, setProgramSequenceBindingMemes) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setProgramSequenceBindingMemes({});

  ASSERT_TRUE(subject.getProgramSequenceBindingMemes().empty());
}

TEST(ContentStoreTest, setProgramSequenceChords) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setProgramSequenceChords({});

  ASSERT_TRUE(subject.getProgramSequenceChords().empty());
}

TEST(ContentStoreTest, setProgramSequenceChordVoicings) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setProgramSequenceChordVoicings({});

  ASSERT_TRUE(subject.getProgramSequenceChordVoicings().empty());
}

TEST(ContentStoreTest, setProgramSequencePatterns) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setProgramSequencePatterns({});

  ASSERT_TRUE(subject.getProgramSequencePatterns().empty());
}

TEST(ContentStoreTest, setProgramSequencePatternEvents) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setProgramSequencePatternEvents({});

  ASSERT_TRUE(subject.getProgramSequencePatternEvents().empty());
}

TEST(ContentStoreTest, setProgramVoices) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setProgramVoices({});

  ASSERT_TRUE(subject.getProgramVoices().empty());
}

TEST(ContentStoreTest, setProgramVoiceTracks) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setProgramVoiceTracks({});

  ASSERT_TRUE(subject.getProgramVoiceTracks().empty());
}

TEST(ContentStoreTest, setProjects) {
  ContentTestHelper test;
  ContentStore &subject = test.store;
  auto project2 = test.buildProject();
  subject.setProjects({project2});

  auto result = subject.getProject();

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(result.value()->id, project2.id);
}

TEST(ContentStoreTest, setTemplates) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setTemplates({});

  ASSERT_TRUE(subject.getTemplates().empty());
}

TEST(ContentStoreTest, setTemplateBindings) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  subject.setTemplateBindings({});

  ASSERT_TRUE(subject.getTemplateBindings().empty());
}


TEST(ContentStoreTest, getSequencePatternEventsOfProgram) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getSequencePatternEventsOfProgram(test.program2.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getEventsOfProgramId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getSequencePatternEventsOfProgram(test.program2_sequence_pattern1.programId);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getSequencePatternsOfProgram) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getSequencePatternsOfProgram(test.program2);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getPatternsOfProgramId) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getSequencePatternsOfProgram(test.program2_sequence_pattern1.programId);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getInstrumentsOfType) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getInstrumentsOfType(Instrument::Type::Drum);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getInstrumentsOfTypesAndModes) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getInstrumentsOfTypesAndModes({Instrument::Type::Drum}, {Instrument::Mode::Event});

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getInstrumentTypeOfAudio) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getInstrumentTypeOfAudio(test.instrument1_audio.id);

  ASSERT_EQ(Instrument::Type::Drum, result);
}

TEST(ContentStoreTest, getMemesOfInstrument) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getMemesOfInstrument(test.instrument1.id);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getPatternIdOfEvent) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getPatternIdOfEvent(test.program2_sequence_pattern1_event1.id);

  ASSERT_EQ(test.program2_sequence_pattern1.id, result);
}

TEST(ContentStoreTest, getProgramsOfType) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramsOfType(Program::Type::Main);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getMemesOfProgram) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getMemesOfProgram(test.program1.id);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getSequencesOfProgram) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getSequencesOfProgram(test.program1.id);

  ASSERT_EQ(1, result.size());
}

TEST(ContentStoreTest, getSequenceChordsOfProgram) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getSequenceChordsOfProgram(test.program1.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getTracksOfProgram) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getTracksOfProgram(test.program2.id);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getTracksOfProgramType) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getTracksOfProgramType(Program::Type::Beat);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getTemplateBinding) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getTemplateBinding(test.template1_binding.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.template1_binding.id, result.value()->id);
}

TEST(ContentStoreTest, getTrackNamesOfVoice) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getTrackNamesOfVoice(test.program2_voice);

  ASSERT_EQ(2, result.size());
}

TEST(ContentStoreTest, getInstrumentMeme) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getInstrumentMeme(test.instrument1_meme.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.instrument1_meme.id, result.value()->id);
}

TEST(ContentStoreTest, getProgramMeme) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramMeme(test.program1_meme.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program1_meme.id, result.value()->id);
}

TEST(ContentStoreTest, getProgramSequenceBindingMeme) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramSequenceBindingMeme(test.program1_sequence_binding1_meme1.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program1_sequence_binding1_meme1.id, result.value()->id);
}

TEST(ContentStoreTest, getProgramSequenceChordVoicing) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.getProgramSequenceChordVoicing(test.program1_sequence_chord0_voicing0.id);

  ASSERT_TRUE(result.has_value());
  ASSERT_EQ(test.program1_sequence_chord0_voicing0.id, result.value()->id);
}

TEST(ContentStoreTest, getPatternsOfSequenceAndVoice) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  ASSERT_EQ(2, subject.getPatternsOfSequenceAndVoice(test.program2_sequence.id, test.program2_voice.id).size());
  ASSERT_EQ(0, subject.getPatternsOfSequenceAndVoice(test.randomUUID(), test.program2_voice.id).size());
  ASSERT_EQ(0, subject.getPatternsOfSequenceAndVoice(test.program2_sequence.id, test.randomUUID()).size());
}

TEST(ContentStoreText, ForTemplate_BoundToLibrary) {
  ContentTestHelper test;
  ContentStore &subject = test.store;

  auto result = subject.forTemplate(test.template1);

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

TEST(ContentStoreText, ForTemplate_BoundToProgram) {
  ContentTestHelper test;
  ContentStore &subject = test.store;
  subject.setTemplateBindings({test.buildTemplateBinding(test.template2, test.program1)});

  auto result = subject.forTemplate(test.template2);

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

TEST(ContentStoreText, ForTemplate_BoundToInstrument) {
  ContentTestHelper test;
  ContentStore &subject = test.store;
  subject.setTemplateBindings({test.buildTemplateBinding(test.template2, test.instrument1)});

  auto result = subject.forTemplate(test.template2);

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
