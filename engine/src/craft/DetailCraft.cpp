// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/craft/DetailCraft.h"

using namespace XJ;

  private static Collection<Instrument::Type> DETAIL_INSTRUMENT_TYPES = Set.of(
    Instrument::Type::Bass,
    Instrument::Type::Pad,
    Instrument::Type::Sticky,
    Instrument::Type::Stripe,
    Instrument::Type::Stab,
    Instrument::Type::Hook,
    Instrument::Type::Percussion
  );

  public DetailCraftImpl(Fabricator fabricator) {
    super(fabricator);
  }

  @Override
  public void doWork() throws FabricationException {
    // Segments have delta arcs; automate mixer layers in and out of each main program https://github.com/xjmusic/xjmusic/issues/233
    ChoiceIndexProvider choiceIndexProvider = (SegmentChoice choice) -> StringUtils.stringOrDefault(choice.getInstrumentType(), choice.getId().toString());
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> Objects.equals(Program::Type::Detail, choice.getProgramType());
    precomputeDeltas(choiceFilter, choiceIndexProvider, fabricator.getTemplateConfig().getDetailLayerOrder().stream().map(Instrument::Type::toString).collect(Collectors.toList()), List.of(), fabricator.getTemplateConfig().getDeltaArcBeatLayersIncoming());

    // For each type of detail instrument type, choose instrument, then program if necessary
    for (Instrument::Type instrumentType : DETAIL_INSTRUMENT_TYPES) {

      // Instrument is from prior choice, else freshly chosen
      Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(instrumentType);

      // Instruments may be chosen without programs https://github.com/xjmusic/xjmusic/issues/234
      Optional<Instrument> instrument = priorChoice.isPresent() ? fabricator.sourceMaterial().getInstrument(priorChoice.get().getInstrumentId()) : chooseFreshInstrument(instrumentType, Set.of());

      // Should gracefully skip voicing type if unfulfilled by detail instrument https://github.com/xjmusic/xjmusic/issues/240
      if (instrument.isEmpty()) {
        continue;
      }

      // Instruments have Instrument::Mode https://github.com/xjmusic/xjmusic/issues/260
      switch (instrument.get().getMode()) {

        // Event instrument mode takes over legacy behavior https://github.com/xjmusic/xjmusic/issues/234
        case Event -> {
          // Event Use prior chosen program or find a new one
          Optional<Program> program = priorChoice.isPresent() ? fabricator.sourceMaterial().getProgram(priorChoice.get().getProgramId()) : chooseFreshProgram(Program::Type::Detail, instrumentType);

          // Event Should gracefully skip voicing type if unfulfilled by detail program https://github.com/xjmusic/xjmusic/issues/240
          if (program.isEmpty()) {
            continue;
          }
          craftEventParts(fabricator.getTempo(), instrument.get(), program.get());
        }

        // Chord instrument mode https://github.com/xjmusic/xjmusic/issues/235
        case Chord -> craftChordParts(fabricator.getTempo(), instrument.get());

        case Loop -> {
          craftLoopParts(fabricator.getTempo(), instrument.get());
        }

        // As-yet Unsupported Modes
        default ->
          fabricator.addWarningMessage(std::string.format("Cannot craft unsupported mode %s for Instrument[%s]", instrument.get().getMode(), instrument.get().getId()));
      }
    }

  }

  /**
   Craft loop parts

   @param tempo of main program
   @param instrument for which to craft
   */
  @SuppressWarnings("DuplicatedCode")
  void craftLoopParts(double tempo, Instrument instrument) throws FabricationException {
    var choice = new SegmentChoice();
    choice.setId(EntityUtils::computeUniqueId());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setMute(computeMute(instrument.getType()));
    choice.setInstrumentType(instrument.getType());
    choice.setInstrumentMode(instrument.getMode());
    choice.setInstrumentId(instrument.getId());
    fabricator.put(choice, false);
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(EntityUtils::computeUniqueId());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement, false);

    for (InstrumentAudio audio : selectGeneralAudioIntensityLayers(instrument)) {

      // Start at zero and keep laying down loops until we're out of here
      float beats = 0;
      while (beats < fabricator.getSegment().getTotal()) {

        // Pick attributes are expressed "rendered" as actual seconds
        long startAtSegmentMicros = fabricator.getSegmentMicrosAtPosition(tempo, beats);
        long lengthMicros = Math.min(
          fabricator.getTotalSegmentMicros() - startAtSegmentMicros,
          (long) (audio.getLoopBeats() * fabricator.getMicrosPerBeat(tempo))
        );

        // of pick
        var pick = new SegmentChoiceArrangementPick();
        pick.setId(EntityUtils::computeUniqueId());
        pick.setSegmentId(fabricator.getSegment().getId());
        pick.setSegmentChoiceArrangementId(arrangement.getId());
        pick.setStartAtSegmentMicros(startAtSegmentMicros);
        pick.setLengthMicros(lengthMicros);
        pick.setAmplitude(1.0f);
        pick.setEvent("LOOP");
        pick.setInstrumentAudioId(audio.getId());
        fabricator.put(pick, false);

        beats += audio.getLoopBeats();
      }
    }
  }
