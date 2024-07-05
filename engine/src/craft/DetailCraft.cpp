// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/craft/DetailCraft.h"

using namespace XJ;

static std::set DETAIL_INSTRUMENT_TYPES = {
    Instrument::Type::Bass,
    Instrument::Type::Pad,
    Instrument::Type::Sticky,
    Instrument::Type::Stripe,
    Instrument::Type::Stab,
    Instrument::Type::Hook,
    Instrument::Type::Percussion};

DetailCraft::DetailCraft(
    Fabricator *fabricator) : Craft(fabricator) {}


void DetailCraft::doWork() {
  // Segments have delta arcs; automate mixer layers in and out of each main program https://github.com/xjmusic/xjmusic/issues/233
  auto choiceIndexProvider = LambdaChoiceIndexProvider(
      [](const SegmentChoice &choice) -> std::string {
        auto typeString = Instrument::toString(choice.instrumentType);
        if (typeString.empty()) {
          return choice.id;
        }

        return typeString;
      });
  const std::function choiceFilter = [](const SegmentChoice *choice) {
    return Program::Type::Detail == choice->programType;
  };

  precomputeDeltas(
      choiceFilter,
      choiceIndexProvider,
      Instrument::toStrings(fabricator->getTemplateConfig().detailLayerOrder),
      {},
      fabricator->getTemplateConfig().deltaArcDetailLayersIncoming);

  // For each type of detail instrument type, choose instrument, then program if necessary
  for (Instrument::Type instrumentType: DETAIL_INSTRUMENT_TYPES) {

    // Instrument is from prior choice, else freshly chosen
    auto priorChoice = fabricator->getChoiceIfContinued(instrumentType);

    // Instruments may be chosen without programs https://github.com/xjmusic/xjmusic/issues/234
    auto instrument = priorChoice.has_value()
                          ? fabricator->getSourceMaterial()->getInstrument(priorChoice.value()->instrumentId)
                          : chooseFreshInstrument(instrumentType, {});

    // Should gracefully skip voicing type if unfulfilled by detail instrument https://github.com/xjmusic/xjmusic/issues/240
    if (!instrument.has_value()) {
      continue;
    }

    // Instruments have Instrument::Mode https://github.com/xjmusic/xjmusic/issues/260
    std::optional<const Program*> program;
    switch (instrument.value()->mode) {

      // Event instrument mode takes over legacy behavior https://github.com/xjmusic/xjmusic/issues/234
      case Instrument::Mode::Event:
        // Event Use prior chosen program or find a new one
        program = priorChoice.has_value()
                           ? fabricator->getSourceMaterial()->getProgram(priorChoice.value()->programId)
                           : chooseFreshProgram(Program::Type::Detail, instrumentType);

        // Event Should gracefully skip voicing type if unfulfilled by detail program https://github.com/xjmusic/xjmusic/issues/240
        if (!program.has_value()) {
          continue;
        }
        craftEventParts(fabricator->getTempo(), instrument.value(), program.value());
        break;

        // Chord instrument mode https://github.com/xjmusic/xjmusic/issues/235
      case Instrument::Mode::Chord:
        craftChordParts(fabricator->getTempo(), instrument.value());
        break;

      case Instrument::Mode::Loop:
        craftLoopParts(fabricator->getTempo(), instrument.value());
        break;

        // As-yet Unsupported Modes
      default:
        fabricator->addWarningMessage(
            "Cannot craft unsupported mode " + Instrument::toString(instrument.value()->mode) + " for Instrument[" +
            instrument.value()->id + "]");
        break;
    }
  }
}

void DetailCraft::craftLoopParts(double tempo, const Instrument *instrument) const {
  auto choice = SegmentChoice();
  choice.id = EntityUtils::computeUniqueId();
  choice.segmentId = fabricator->getSegment()->id;
  choice.mute = computeMute(instrument->type);
  choice.instrumentType = instrument->type;
  choice.instrumentMode = instrument->mode;
  choice.instrumentId = instrument->id;
  if (!fabricator->put(choice, false).has_value()) return;
  auto arrangement = SegmentChoiceArrangement();
  arrangement.id = EntityUtils::computeUniqueId();
  arrangement.segmentId = fabricator->getSegment()->id;
  arrangement.segmentChoiceId = choice.id;
  fabricator->put(arrangement);

  for (auto audio: selectGeneralAudioIntensityLayers(instrument)) {

    // Start at zero and keep laying down loops until we're out of here
    float beats = 0;
    while (beats < fabricator->getSegment()->total) {

      // Pick attributes are expressed "rendered" as actual seconds
      long startAtSegmentMicros = fabricator->getSegmentMicrosAtPosition(tempo, beats);
      long lengthMicros = fmin(
          fabricator->getTotalSegmentMicros() - startAtSegmentMicros,
          static_cast<long>(audio->loopBeats * fabricator->getMicrosPerBeat(tempo)));

      // of pick
      auto pick = SegmentChoiceArrangementPick();
      pick.id = EntityUtils::computeUniqueId();
      pick.segmentId = fabricator->getSegment()->id;
      pick.segmentChoiceArrangementId = arrangement.id;
      pick.startAtSegmentMicros = startAtSegmentMicros;
      pick.lengthMicros = lengthMicros;
      pick.amplitude = 1.0f;
      pick.event = "LOOP";
      pick.instrumentAudioId = audio->id;
      fabricator->put(pick);

      beats += audio->loopBeats;
    }
  }
}
