// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_MOCK_FABRICATOR_H
#define XJMUSIC_MOCK_FABRICATOR_H

#include <gmock/gmock.h>

#include "xjmusic/fabricator/Fabricator.h"

namespace XJ {

  class MockFabricator : public Fabricator {
  public:
    MOCK_METHOD(void, addMessage, (SegmentMessage::Type messageType, std::string body), (override));
    MOCK_METHOD(void, addErrorMessage, (std::string body), (override));
    MOCK_METHOD(void, addWarningMessage, (std::string body), (override));
    MOCK_METHOD(void, addInfoMessage, (std::string body), (override));
    MOCK_METHOD(void, deletePick, (const UUID &id), (override));
    MOCK_METHOD(std::set<SegmentChoiceArrangement>, getArrangements, (), (override));
    MOCK_METHOD(std::set<SegmentChoiceArrangement>, getArrangements, (const std::set<SegmentChoice> &choices), (override));
    MOCK_METHOD(Chain, getChain, (), (override));
    MOCK_METHOD(TemplateConfig, getTemplateConfig, (), (override));
    MOCK_METHOD(std::set<SegmentChoice>, getChoices, (), (override));
    MOCK_METHOD(std::optional<SegmentChoice>, getChoiceIfContinued, (const ProgramVoice *voice), (override));
    MOCK_METHOD(std::vector<SegmentChoice>, getChoicesIfContinued, (Program::Type programType), (override));
    MOCK_METHOD(std::optional<SegmentChoice>, getChoiceIfContinued, (Instrument::Type instrumentType), (override));
    MOCK_METHOD(std::optional<SegmentChoice>, getChoiceIfContinued, (Instrument::Type instrumentType, Instrument::Mode instrumentMode), (override));
    MOCK_METHOD(std::optional<SegmentChord>, getChordAt, (float position), (override));
    MOCK_METHOD(std::optional<SegmentChoice>, getCurrentMainChoice, (), (override));
    MOCK_METHOD(std::optional<const ProgramSequence *>, getCurrentMainSequence, (), (override));
    MOCK_METHOD(std::vector<SegmentChoice>, getCurrentDetailChoices, (), (override));
    MOCK_METHOD(std::optional<SegmentChoice>, getCurrentBeatChoice, (), (override));
    MOCK_METHOD(std::set<Instrument::Type>, getDistinctChordVoicingTypes, (), (override));
    MOCK_METHOD(long, getElapsedMicros, (), (override));
    MOCK_METHOD(InstrumentConfig, getInstrumentConfig, (const Instrument& instrument), (override));
    MOCK_METHOD(Chord, getKeyForChoice, (const SegmentChoice &choice), (override));
    MOCK_METHOD(std::optional<SegmentChoice>, getMacroChoiceOfPreviousSegment, (), (override));
    MOCK_METHOD(std::optional<SegmentChoice>, getPreviousMainChoice, (), (override));
    MOCK_METHOD(ProgramConfig, getCurrentMainProgramConfig, (), (override));
    MOCK_METHOD(std::optional<const ProgramSequence *>, getPreviousMainSequence, (), (override));
    MOCK_METHOD(MemeIsometry, getMemeIsometryOfNextSequenceInPreviousMacro, (), (override));
    MOCK_METHOD(MemeIsometry, getMemeIsometryOfSegment, (), (override));
    MOCK_METHOD(int, getNextSequenceBindingOffset, (const SegmentChoice &choice), (override));
    MOCK_METHOD(std::optional<ProgramSequence>, getRandomlySelectedSequence, (const Program &program), (override));
    MOCK_METHOD(std::optional<const ProgramSequencePattern *>, getRandomlySelectedPatternOfSequenceByVoiceAndType, (const SegmentChoice &choice), (override));
    MOCK_METHOD(std::optional<ProgramSequenceBinding>, getRandomlySelectedSequenceBindingAtOffset, (const Program &program, int offset), (override));
    MOCK_METHOD(std::optional<Note>, getRootNoteMidRange, (const std::string &voicingNotes, const Chord &chord), (override));
    MOCK_METHOD(long, getSegmentMicrosAtPosition, (float tempo, float position), (override));
    MOCK_METHOD(long, getTotalSegmentMicros, (), (override));
    MOCK_METHOD(Segment, getSegment, (), (override));
    MOCK_METHOD(std::vector<SegmentChord>, getSegmentChords, (), (override));
    MOCK_METHOD(std::set<SegmentChordVoicing>, getChordVoicings, (), (override));
    MOCK_METHOD(std::set<SegmentMeme>, getSegmentMemes, (), (override));
    MOCK_METHOD(std::optional<ProgramSequence>, getSequence, (const SegmentChoice &choice), (override));
    MOCK_METHOD(int, getSequenceBindingOffsetForChoice, (const SegmentChoice &choice), (override));
    MOCK_METHOD(void, putStickyBun, (StickyBun bun), (override));
    MOCK_METHOD(std::optional<StickyBun>, getStickyBun, (const UUID &eventId), (override));
    MOCK_METHOD(std::string, getTrackName, (const ProgramSequencePatternEvent &event), (override));
    MOCK_METHOD(Segment::Type, getType, (), (override));
    MOCK_METHOD(void, updateSegment, (Segment segment), (override));
    MOCK_METHOD(SegmentRetrospective *, getRetrospective, (), (override));
    MOCK_METHOD(ContentEntityStore *, getSourceMaterial, (), (override));
    MOCK_METHOD(float, getMicrosPerBeat, (float tempo), (override));
    MOCK_METHOD(int, getSecondMacroSequenceBindingOffset, (const Program &macroProgram), (override));
    MOCK_METHOD(double, getTempo, (), (override));
    MOCK_METHOD(MemeTaxonomy, getMemeTaxonomy, (), (const, override));
  };

} // namespace XJ
#endif //XJMUSIC_MOCK_FABRICATOR_H
