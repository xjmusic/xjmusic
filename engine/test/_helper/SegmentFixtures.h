// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATION_CONTENT_TWO_FIXTURES_H
#define XJMUSIC_FABRICATION_CONTENT_TWO_FIXTURES_H

#include <chrono>
#include <iomanip>
#include <random>
#include <sstream>
#include <utility>

#include "xjmusic/content/Instrument.h"
#include "xjmusic/content/InstrumentAudio.h"
#include "xjmusic/content/InstrumentMeme.h"
#include "xjmusic/content/Library.h"
#include "xjmusic/content/Program.h"
#include "xjmusic/content/ProgramMeme.h"
#include "xjmusic/content/ProgramSequence.h"
#include "xjmusic/content/ProgramSequenceBinding.h"
#include "xjmusic/content/ProgramSequenceBindingMeme.h"
#include "xjmusic/content/ProgramSequenceChord.h"
#include "xjmusic/content/ProgramSequenceChordVoicing.h"
#include "xjmusic/content/ProgramSequencePattern.h"
#include "xjmusic/content/ProgramSequencePatternEvent.h"
#include "xjmusic/content/ProgramVoice.h"
#include "xjmusic/content/ProgramVoiceTrack.h"
#include "xjmusic/content/Project.h"
#include "xjmusic/content/Template.h"
#include "xjmusic/content/TemplateBinding.h"
#include "xjmusic/content/TemplateConfig.h"
#include "xjmusic/segment/Chain.h"
#include "xjmusic/segment/Segment.h"
#include "xjmusic/segment/SegmentChoice.h"
#include "xjmusic/segment/SegmentChoiceArrangement.h"
#include "xjmusic/segment/SegmentChoiceArrangementPick.h"
#include "xjmusic/segment/SegmentChord.h"
#include "xjmusic/segment/SegmentChordVoicing.h"
#include "xjmusic/segment/SegmentMeme.h"
#include "xjmusic/segment/SegmentMeta.h"
#include "xjmusic/util/StringUtils.h"
#include "xjmusic/util/ValueUtils.h"

#include "ContentFixtures.h"
#include "LoremIpsum.h"

namespace XJ {

  /**
 Integration tests use shared scenario fixtures as much as possible https://github.com/xjmusic/xjmusic/issues/202
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought to be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
  class SegmentFixtures {
  public:
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
        const Segment& segment,
        int deltaIn,
        int deltaOut,
        const Program& program,
        const ProgramSequenceBinding& programSequenceBinding);

    /**
     * Build a choice of program, voice, and instrument for a segment
     */
    static SegmentChoice buildSegmentChoice(
        const Segment& segment,
        int deltaIn,
        int deltaOut,
        const Program& program,
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
  };

}// namespace XJ

#endif//XJMUSIC_FABRICATION_CONTENT_TWO_FIXTURES_H