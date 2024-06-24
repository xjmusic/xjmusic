// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/craft/BeatCraft.h"

using namespace XJ;

BeatCraft::BeatCraft(
      Fabricator * fabricator
  ) : Craft(fabricator) {}


void BeatCraft::doWork()  {
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
        auto voice = fabricator->getSourceMaterial()->getProgramVoice(choice.programVoiceId);
        if (voice.has_value()) return voice.value()->name;
        return "unknown";
      });

    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> Program::Type::Beat.equals(choice.getProgramType());
    auto programNames = fabricator->getSourceMaterial()->getVoicesOfProgram(program.get()).stream()
      .map(ProgramVoice::getName)
      .collect(Collectors.toList());
    precomputeDeltas(
      choiceFilter,
      choiceIndexProvider,
      programNames,
      fabricator->getTemplateConfig().getDeltaArcBeatLayersToPrioritize(),
      fabricator->getTemplateConfig().getDeltaArcBeatLayersIncoming()
    );

    // beat sequence is selected at random of the current program
    // FUTURE: Beat Program with multiple Sequences https://github.com/xjmusic/xjmusic/issues/241
    auto sequence = fabricator->getRandomlySelectedSequence(program.get());

    // voice arrangements
    if (sequence.isPresent()) {
      for (ProgramVoice voice : fabricator->getSourceMaterial()->getVoicesOfProgram(program.get())) {
        auto choice = SegmentChoice();
        choice.setId(EntityUtils::computeUniqueId());
        choice.setSegmentId(fabricator->getSegment().id);
        choice.setMute(computeMute(voice.type));
        choice.setProgramType(fabricator->getSourceMaterial()->getProgram(voice.getProgramId()).orElseThrow(() -> new FabricationException("Can't get program for voice")).type);
        choice.setInstrumentType(voice.type);
        choice.setProgramId(voice.getProgramId());
        choice.setProgramSequenceId(sequence.get().id);
        choice.setProgramVoiceId(voice.id);

        // Whether there is a prior choice for this voice
        std::optional<SegmentChoice> priorChoice = fabricator->getChoiceIfContinued(voice);

        if (priorChoice.isPresent()) {
          // If there is a prior choice, then we should continue it
          choice.setDeltaIn(priorChoice.get().getDeltaIn());
          choice.setDeltaOut(priorChoice.get().getDeltaOut());
          choice.setInstrumentId(priorChoice.get().getInstrumentId());
          choice.setInstrumentMode(priorChoice.get().getInstrumentMode());
          this.craftNoteEventArrangements(fabricator->getTempo(), fabricator->put(choice, false), true);
        } else {
          // If there is no prior choice, then we should choose a fresh instrument
          auto instrument = chooseFreshInstrument(Instrument::Type::Drum, fabricator->getSourceMaterial()->getTrackNamesOfVoice(voice));
          if (instrument.isEmpty()) {
            continue;
          }
          choice.setDeltaIn(computeDeltaIn(choice));
          choice.setDeltaOut(computeDeltaOut(choice));
          choice.setInstrumentId(instrument.get().id);
          choice.setInstrumentMode(instrument.get().mode);
          this.craftNoteEventArrangements(fabricator->getTempo(), fabricator->put(choice, false), true);
        }
      }
    }
  }
