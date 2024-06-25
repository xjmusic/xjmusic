// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/craft/BeatCraft.h"

using namespace XJ;

BeatCraft::BeatCraft(
    Fabricator *fabricator
) : Craft(fabricator) {}


void BeatCraft::doWork() {
  auto priorBeatChoices = fabricator->getChoicesIfContinued(Program::Type::Beat);

  // Program is from prior choice, or freshly chosen
  auto program = priorBeatChoices.empty() ?
                 chooseFreshProgram(Program::Type::Beat, Instrument::Type::Drum) :
                 fabricator->getSourceMaterial()->getProgram((*priorBeatChoices.begin())->programId);

  // Should gracefully skip voicing type if unfulfilled by detail program https://github.com/xjmusic/xjmusic/issues/240
  if (!program.has_value()) {
    return;
  }

  // Segments have intensity arcs; automate mixer layers in and out of each main program https://github.com/xjmusic/xjmusic/issues/233
  Craft::ChoiceIndexProvider *choiceIndexProvider = new Craft::LambdaChoiceIndexProvider(
      [this](const SegmentChoice &choice) -> std::string {
        const auto voice = fabricator->getSourceMaterial()->getProgramVoice(choice.programVoiceId);
        if (voice.has_value()) return voice.value()->name;
        return "unknown";
      });

  std::function<bool(const SegmentChoice *)> choiceFilter = [](const SegmentChoice *choice) {
    return Program::Type::Beat == choice->programType;
  };

  auto programVoices = fabricator->getSourceMaterial()->getVoicesOfProgram(program.value());
  auto voiceNames = ProgramVoice::getNames(programVoices);
  auto voiceNameVector = std::vector<std::string>(voiceNames.begin(), voiceNames.end());

  precomputeDeltas(
      choiceFilter,
      choiceIndexProvider,
      voiceNameVector,
      fabricator->getTemplateConfig().deltaArcBeatLayersToPrioritize,
      fabricator->getTemplateConfig().deltaArcBeatLayersIncoming
  );

  // beat sequence is selected at random of the current program
  // FUTURE: Beat Program with multiple Sequences https://github.com/xjmusic/xjmusic/issues/241
  auto sequence = fabricator->getRandomlySelectedSequence(program.value());

  // voice arrangements
  if (sequence.has_value()) {
    for (auto voice: programVoices) {
      auto choice = SegmentChoice();
      choice.id = EntityUtils::computeUniqueId();
      choice.segmentId = fabricator->getSegment()->id;
      choice.mute = computeMute(voice->type);
      choice.programType = program.value()->type;
      choice.instrumentType = voice->type;
      choice.programId = voice->programId;
      choice.programSequenceId = sequence.value()->id;
      choice.programVoiceId = voice->id;

      // Whether there is a prior choice for this voice
      auto priorChoice = fabricator->getChoiceIfContinued(voice);

      if (priorChoice.has_value()) {
        // If there is a prior choice, then we should continue it
        choice.deltaIn = priorChoice.value()->deltaIn;
        choice.deltaOut = priorChoice.value()->deltaOut;
        choice.instrumentId = priorChoice.value()->instrumentId;
        choice.instrumentMode = priorChoice.value()->instrumentMode;
        auto storedChoice = fabricator->put(choice, false);
        if (storedChoice.has_value())
          craftNoteEventArrangements(static_cast<float>(fabricator->getTempo()), storedChoice.value(), true);
      } else {
        // If there is no prior choice, then we should choose a fresh instrument
        auto instrument = chooseFreshInstrument(Instrument::Type::Drum,
                                                fabricator->getSourceMaterial()->getTrackNamesOfVoice(voice));
        if (!instrument.has_value()) {
          continue;
        }
        choice.deltaIn = computeDeltaIn(&choice);
        choice.deltaOut = computeDeltaOut(&choice);
        choice.instrumentId = instrument.value()->id;
        choice.instrumentMode = instrument.value()->mode;
        auto storedChoice = fabricator->put(choice, false);
        if (storedChoice.has_value())
          craftNoteEventArrangements(static_cast<float>(fabricator->getTempo()), storedChoice.value(), true);
      }
    }
  }
}
