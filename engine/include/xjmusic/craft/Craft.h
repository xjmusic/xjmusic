// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>
#include <string>
#include <optional>
#include <utility>

#include "xjmusic/fabricator/Fabricator.h"
#include "xjmusic/fabricator/FabricationWrapper.h"
#include "xjmusic/fabricator/MarbleBag.h"

namespace XJ {

/**
 Arrangement of Segment Events is a common foundation for all craft
 */
  class Craft : FabricationWrapper {
    std::map<std::string, int> deltaIns;
    std::map<std::string, int> deltaOuts;
    std::set<Instrument::Type> finalizeAudioLengthsForInstrumentTypes;

    /**
     Instrument provider to make some code more portable
     */
    class InstrumentProvider {
    public:
      virtual std::optional<Instrument> get(const ProgramVoice *voice);
    };

    /**
     * Lambda instrument provider to create an instrument provider from a lambda
     */
    class LambdaInstrumentProvider : public InstrumentProvider {
    public:
      LambdaInstrumentProvider(std::function<std::optional<Instrument>(ProgramVoice)> func) : func_(std::move(func)) {}

      std::optional<Instrument> get(const ProgramVoice *voice) override {
        return func_(*voice);
      }

    private:
      std::function<std::optional<Instrument>(ProgramVoice)> func_;
    };

    /**
     Class to get a comparable string index based on any given choice, e.g. it's voice name or instrument type
     */
    class ChoiceIndexProvider {
    public:
      virtual std::string get(const SegmentChoice *choice);
    };

    /**
     * Lambda choiceIndex provider to create an choiceIndex provider from a lambda
     */
    class LambdaChoiceIndexProvider : public ChoiceIndexProvider {
    public:
      LambdaChoiceIndexProvider(std::function<std::string(SegmentChoice)> func) : func_(std::move(func)) {}

      std::string get(const SegmentChoice *choice) override {
        return func_(*choice);
      }

    private:
      std::function<std::string(SegmentChoice)> func_;
    };

    /**
   Representation of a section of an arrangement, having a chord, beginning position and end position
   */
    class Section {
    public:
      SegmentChord chord;
      double fromPos;
      double toPos;
    };


    ChoiceIndexProvider *choiceIndexProvider = {};

  public:

    /**
     Must extend this class and inject

     @param fabricator internal
     */
    explicit Craft(Fabricator *fabricator) : FabricationWrapper(fabricator) {
      finalizeAudioLengthsForInstrumentTypes = fabricator->getTemplateConfig().instrumentTypesForAudioLengthFinalization;
    }

    /**
     Whether the current segment contains the delta in for the given choice

     @param choice to test whether the current segment contains this choice delta in
     @return true if the current segment contains the given choice's delta in
     */
    bool isIntroSegment(const SegmentChoice &choice) {
      return !isUnlimitedIn(choice) && choice.deltaIn >= fabricator->getSegment().delta &&
             choice.deltaIn < fabricator->getSegment().delta + fabricator->getSegment().total;
    }

    /**
     Whether the current segment contains the delta out for the given choice

     @param choice to test whether the current segment contains this choice delta out
     @return true if the current segment contains the given choice's delta out
     */
    bool isOutroSegment(const SegmentChoice &choice) {
      return !isUnlimitedOut(choice) &&
             choice.deltaOut <= fabricator->getSegment().delta + fabricator->getSegment().total &&
             choice.deltaOut > fabricator->getSegment().delta;
    }

    /**
     Whether the given choice is silent during the entire segment

     @param choice to test for silence
     @return true if choice is silent the entire segment
     */
    bool isSilentEntireSegment(const SegmentChoice &choice) {
      return (choice.deltaOut < fabricator->getSegment().delta) ||
             (choice.deltaIn >= fabricator->getSegment().delta + fabricator->getSegment().total);
    }

    /**
     Whether the given choice is fully active during the current segment

     @param choice to test for activation
     @return true if this choice is active the entire time
     */
    bool isActiveEntireSegment(const SegmentChoice &choice) {
      return (choice.deltaIn <= fabricator->getSegment().delta) &&
             (choice.deltaOut >= fabricator->getSegment().delta + fabricator->getSegment().total);
    }

  protected:

    /**
     Choose a fresh program based on a set of memes

     @param programType to choose
     @param voicingType (optional) for which to choose a program for-- and the program is required to have this type of voice
     @return Program
     */
    std::optional<const Program *>
    chooseFreshProgram(Program::Type programType, std::optional<Instrument::Type> voicingType) {
      auto bag = MarbleBag::empty();

      // Retrieve programs bound to chain having a voice of the specified type
      std::map<UUID, Program> programMap;
      for (auto program: fabricator->getSourceMaterial()->getProgramsOfType(programType)) {
        programMap[program->id] = *program;
      }

      std::set<Program> candidates;
      for (const auto &programVoice: fabricator->getSourceMaterial()->getProgramVoices()) {
        if (voicingType.has_value() && voicingType.value() == programVoice->type &&
            programMap.count(programVoice->programId)) {
          candidates.insert(programMap[programVoice->programId]);
        }
      }

      // (3) score each source program based on meme isometry
      MemeIsometry iso = fabricator->getMemeIsometryOfSegment();
      std::set<std::string> memes;

      // Phase 1: Directly Bound Programs
      for (const Program &program: programsDirectlyBound(candidates)) {
        memes = ProgramMeme::getNames(fabricator->getSourceMaterial()->getMemesOfProgram(program.id));
        // FUTURE consider meme isometry, but for now, just use the meme stack
        if (iso.isAllowed(memes)) bag.add(1, program.id, 1 + iso.score(memes));
      }

      // Phase 2: All Published Programs
      for (const Program &program: programsPublished(candidates)) {
        memes = ProgramMeme::getNames(fabricator->getSourceMaterial()->getMemesOfProgram(program.id));
        // FUTURE consider meme isometry, but for now, just use the meme stack
        if (iso.isAllowed(memes)) bag.add(2, program.id, 1 + iso.score(memes));
      }

      // report
      fabricator->putReport("choiceOf" + (voicingType.has_value() ? Instrument::toString(voicingType.value()) : "") +
                            Program::toString(programType) + "Program", bag.toString());

      // (4) return the top choice
      if (bag.isEmpty()) return std::nullopt;
      return fabricator->getSourceMaterial()->getProgram(bag.pick());
    }

    /**
     Choose instrument
     <p>
     Choose drum instrument to fulfill beat program event names https://github.com/xjmusic/xjmusic/issues/253

     @param type              of instrument to choose from
     @param requireEventNames instrument candidates are required to have event names https://github.com/xjmusic/xjmusic/issues/253
     @return Instrument
     */
    std::optional<const Instrument *>
    chooseFreshInstrument(Instrument::Type type, const std::set<std::string> &requireEventNames) {
      auto bag = MarbleBag::empty();

      // Retrieve instruments bound to chain
      std::set<const Instrument *> candidates;
      for (const auto &instrument: fabricator->getSourceMaterial()->getInstrumentsOfType(type)) {
        if (instrumentContainsAudioEventsLike(instrument, requireEventNames)) {
          candidates.insert(instrument);
        }
      }

      // Retrieve meme isometry of segment
      MemeIsometry iso = fabricator->getMemeIsometryOfSegment();
      std::set<std::string> memes;

      // Phase 1: Directly Bound Instruments
      for (const Instrument &instrument: instrumentsDirectlyBound(candidates)) {
        memes = InstrumentMeme::getNames(fabricator->getSourceMaterial()->getMemesOfInstrument(instrument.id));
        if (iso.isAllowed(memes)) bag.add(1, instrument.id, 1 + iso.score(memes));
      }

      // Phase 2: All Published Instruments
      for (const Instrument &instrument: instrumentsPublished(candidates)) {
        memes = InstrumentMeme::getNames(fabricator->getSourceMaterial()->getMemesOfInstrument(instrument.id));
        if (iso.isAllowed(memes)) bag.add(2, instrument.id, 1 + iso.score(memes));
      }

      // report
      fabricator->putReport("choiceOf" + Instrument::toString(type) + "Instrument", bag.toString());

      // (4) return the top choice
      if (bag.isEmpty()) return std::nullopt;
      return fabricator->getSourceMaterial()->getInstrument(bag.pick());
    }

    /**
     Percussion-type Loop-mode instrument audios are chosen in order of priority
     https://github.com/xjmusic/xjmusic/issues/255
     <p>
     Choose drum instrument to fulfill beat program event names https://github.com/xjmusic/xjmusic/issues/253

     @param types           of instrument to choose from
     @param modes           of instrument to choose from
     @param avoidIds        to avoid, or empty list
     @param preferredEvents instrument candidates are required to have event names https://github.com/xjmusic/xjmusic/issues/253
     @return Instrument
     */
    std::optional<const InstrumentAudio *>
    chooseFreshInstrumentAudio(
        const std::set<Instrument::Type> &types,
        const std::set<Instrument::Mode> &modes,
        const std::set<UUID> &avoidIds,
        const std::set<std::string> &preferredEvents
    ) {
      auto bag = MarbleBag::empty();

      // (2) retrieve instruments bound to chain
      std::set<InstrumentAudio> candidates;
      for (const auto &audio: fabricator->getSourceMaterial()->getAudiosOfInstrumentTypesAndModes(types, modes)) {
        if (avoidIds.find(audio->id) == avoidIds.end()) {
          candidates.insert(*audio);
        }
      }

      // (3) score each source instrument based on meme isometry
      MemeIsometry iso = fabricator->getMemeIsometryOfSegment();
      std::set<std::string> memes;

      // Phase 1: Directly Bound Audios (Preferred)
      for (const InstrumentAudio &audio: audiosDirectlyBound(candidates)) {
        memes = InstrumentMeme::getNames(fabricator->getSourceMaterial()->getMemesOfInstrument(audio.instrumentId));
        if (iso.isAllowed(memes)) {
          bag.add(preferredEvents.find(audio.event) != preferredEvents.end() ? 1 : 3, audio.id, 1 + iso.score(memes));
        }
      }

      // Phase 2: All Published Audios (Preferred)
      for (const InstrumentAudio &audio: audiosPublished(candidates)) {
        memes = InstrumentMeme::getNames(fabricator->getSourceMaterial()->getMemesOfInstrument(audio.instrumentId));
        if (iso.isAllowed(memes)) {
          bag.add(preferredEvents.find(audio.event) != preferredEvents.end() ? 2 : 4, audio.id, 1 + iso.score(memes));
        }
      }

      // report
      std::string typeNames;
      for (const auto &type: types) {
        if (!typeNames.empty()) {
          typeNames += ",";
        }
        typeNames += Instrument::toString(type);
      }
      std::string modeNames;
      for (const auto &mode: modes) {
        if (!modeNames.empty()) {
          modeNames += ",";
        }
        modeNames += Instrument::toString(mode);
      }
      fabricator->putReport("choice" + typeNames + modeNames, bag.toString());

      // (4) return the top choice
      if (bag.isEmpty()) return std::nullopt;
      return fabricator->getSourceMaterial()->getInstrumentAudio(bag.pick());
    }

    /**
     Filter only the directly bound programs

     @param programs to filter
     @return filtered programs
     */
    std::set<Program> programsDirectlyBound(const std::set<Program> &programs) {
      std::set<Program> result;
      for (const auto &program: programs) {
        if (fabricator->isDirectlyBound(program)) {
          result.insert(program);
        }
      }
      return result;
    }

    /**
     Filter only the published programs

     @param programs to filter
     @return filtered programs
     */
    std::set<Program> programsPublished(const std::set<Program> &programs) {
      std::set<Program> result;
      for (const auto &program: programs) {
        if (program.state == Program::State::Published) {
          result.insert(program);
        }
      }
      return result;
    }

    /**
     Filter only the directly bound instruments

     @param instruments to filter
     @return filtered instruments
     */
    std::set<Instrument> instrumentsDirectlyBound(const std::set<const Instrument *> &instruments) {
      std::set<Instrument> result;
      for (const auto &instrument: instruments) {
        if (fabricator->isDirectlyBound(*instrument)) {
          result.insert(*instrument);
        }
      }
      return result;
    }

    /**
     Filter only the published instruments

     @param instruments to filter
     @return filtered instruments
     */
    std::set<Instrument> instrumentsPublished(const std::set<const Instrument *> &instruments) {
      std::set<Instrument> result;
      for (const auto &instrument: instruments) {
        if (instrument->state == Instrument::State::Published) {
          result.insert(*instrument);
        }
      }
      return result;
    }

    /**
     Filter only the directly bound instrumentAudios

     @param instrumentAudios to filter
     @return filtered instrumentAudios
     */
    std::set<InstrumentAudio> audiosDirectlyBound(const std::set<InstrumentAudio> &instrumentAudios) {
      std::set<InstrumentAudio> result;
      for (const auto &audio: instrumentAudios) {
        if (fabricator->isDirectlyBound(audio)) {
          result.insert(audio);
        }
      }
      return result;
    }

    /**
     Filter only the published instrumentAudios

     @param instrumentAudios to filter
     @return filtered instrumentAudios
     */
    std::set<InstrumentAudio> audiosPublished(const std::set<InstrumentAudio> &instrumentAudios) {
      std::set<InstrumentAudio> result;
      for (const auto &audio: instrumentAudios) {
        auto instrument = fabricator->getSourceMaterial()->getInstrument(audio.instrumentId);
        if (instrument.has_value() && instrument.value()->state == Instrument::State::Published) {
          result.insert(audio);
        }
      }
      return result;
    }

    /**
     Compute a mute value, based on the template config

     @param instrumentType of instrument for which to compute mute
     @return true if muted
     */
    bool computeMute(Instrument::Type instrumentType) {
      return MarbleBag::quickBooleanChanceOf(fabricator->getTemplateConfig().getChoiceMuteProbability(instrumentType));
    }


    /**
     Select a new random instrument audio based on a pattern event

     @param instrument of which to score available audios, and make a selection
     @param chord      to match
     @return matched new audio
     */
    std::optional<const InstrumentAudio *>
    selectNewChordPartInstrumentAudio(const Instrument &instrument, const Chord &chord) {
      auto bag = MarbleBag::empty();

      for (auto a: fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument)) {
        Chord audioChord = Chord::of(a->tones);
        if (audioChord == chord) {
          bag.add(0, a->id);
        } else if (audioChord.isAcceptable(chord)) {
          bag.add(1, a->id);
        }
      }

      if (bag.isEmpty()) return std::nullopt;

      return fabricator->getSourceMaterial()->getInstrumentAudio(bag.pick());
    }

    /**
     Pick the transition

     @param arrangement          to pick
     @param audio                to pick
     @param startAtSegmentMicros to pick
     @param lengthMicros         to pick
     @param event                to pick
     @on failure
     */
    void pickInstrumentAudio(
        const SegmentChoiceArrangement &arrangement,
        const InstrumentAudio &audio,
        long startAtSegmentMicros,
        long lengthMicros,
        std::string event
    ) {
      auto pick = new SegmentChoiceArrangementPick();
      pick->id = EntityUtils::computeUniqueId();
      pick->segmentId = fabricator->getSegment().id;
      pick->segmentChoiceArrangementId = arrangement.id;
      pick->startAtSegmentMicros = startAtSegmentMicros;
      pick->lengthMicros = lengthMicros;
      pick->event = std::move(event);
      pick->amplitude = (float) 1.0;
      pick->instrumentAudioId = audio.id;
      fabricator->put(*pick);
    }

    /**
     Select audios for the given instrument

     @param instrument for which to pick audio
     @return drum-type Instrument
     */
    std::set<InstrumentAudio> selectGeneralAudioIntensityLayers(Instrument instrument) {
      auto previous = fabricator->getRetrospective()->getPreviousPicksForInstrument(instrument.id);
      if (fabricator->getInstrumentConfig(instrument).isAudioSelectionPersistent && !previous.empty()) {
        std::set<InstrumentAudio> result;
        for (const auto &pick: previous) {
          auto audio = fabricator->getSourceMaterial()->getInstrumentAudio(pick.instrumentAudioId);
          if (audio.has_value()) {
            result.insert(*audio.value());
          }
        }
        return result;
      }

      return selectAudioIntensityLayers(
          fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument.id),
          fabricator->getTemplateConfig().getIntensityLayers(instrument.type)
      );
    }

    /**
     Pick one audio for each desired intensity level, by layering the audios by intensity and picking one from each layer.
     Divide the audios into layers (ergo grouping them by intensity ascending) and pick one audio per layer.

     @param audios from which to pick layers
     @param layers number of layers to pick
     @return picked audios
     */
    std::set<InstrumentAudio> selectAudioIntensityLayers(std::set<const InstrumentAudio *> audios, int layers) {
      // Sort audios by intensity
      std::vector<const InstrumentAudio *> sorted(audios.begin(), audios.end());
      std::sort(sorted.begin(), sorted.end(), [](const InstrumentAudio *a, const InstrumentAudio *b) {
        return a->intensity < b->intensity;
      });

      if (sorted.empty()) return {};

      // Create a vector of bags, one for each layer
      std::vector<MarbleBag> bags(layers, MarbleBag::empty());

      // Iterate through the available audios, and add them to the bags, divided into the number of layers
      int marblesPerLayer = static_cast<int>(std::ceil(static_cast<double>(sorted.size()) / layers));
      if (marblesPerLayer == 0) return {};
      for (int i = 0; i < sorted.size(); i++) {
        bags[i / marblesPerLayer].add(1, sorted[i]->id);
      }

      std::set<InstrumentAudio> result;
      for (auto &bag: bags) {
        if (!bag.isEmpty()) {
          auto audio = fabricator->getSourceMaterial()->getInstrumentAudio(bag.pick());
          if (audio.has_value()) {
            result.insert(*audio.value());
          }
        }
      }

      return result;
    }

    /**
     Whether a given choice has deltaIn unlimited

     @param choice to test
     @return true if deltaIn is unlimited
     */
    static bool isUnlimitedIn(SegmentChoice choice) {
      return SegmentChoice::DELTA_UNLIMITED == choice.deltaIn;
    }

    /**
     Whether a given choice has deltaOut unlimited

     @param choice to test
     @return true if deltaOut is unlimited
     */
    static bool isUnlimitedOut(SegmentChoice choice) {
      return SegmentChoice::DELTA_UNLIMITED == choice.deltaOut;
    }

    /**
     Whether a position is in the given bounds

     @param floor   of boundary
     @param ceiling of boundary
     @param value   to test for within bounds
     @return true if value is within bounds (inclusive)
     */
    static bool inBounds(int floor, int ceiling, double value) {
      if (SegmentChoice::DELTA_UNLIMITED == floor && SegmentChoice::DELTA_UNLIMITED == ceiling) return true;
      if (SegmentChoice::DELTA_UNLIMITED == floor && value <= ceiling) return true;
      if (SegmentChoice::DELTA_UNLIMITED == ceiling && value >= floor) return true;
      return value >= floor && value <= ceiling;
    }

    /**
     Segments have intensity arcs; automate mixer layers in and out of each main program
     https://github.com/xjmusic/xjmusic/issues/233

     @param tempo              of main program
     @param sequence           for which to craft choices
     @param voices             for which to craft choices
     @param instrumentProvider from which to get instruments
     @on failure
     */
    void craftNoteEvents(
        double tempo,
        ProgramSequence sequence,
        std::set<const ProgramVoice *> voices,
        InstrumentProvider *instrumentProvider
    ) {
      // Craft each voice into choice
      for (const ProgramVoice *voice: voices) {
        auto choice = new SegmentChoice();
        choice->id = EntityUtils::computeUniqueId();
        choice->segmentId = fabricator->getSegment().id;
        choice->mute = computeMute(voice->type);
        auto p = fabricator->getSourceMaterial()->getProgram(voice->programId);
        if (!p.has_value()) {
          throw FabricationException("Can't get program for voice");
        }
        choice->programType = p.value()->type;
        choice->instrumentType = voice->type;
        choice->programId = voice->programId;
        choice->programSequenceId = sequence.id;
        choice->programVoiceId = voice->id;

        // Whether there is a prior choice for this voice
        std::optional<SegmentChoice> priorChoice = fabricator->getChoiceIfContinued(voice);

        if (priorChoice.has_value()) {
          choice->deltaIn = priorChoice.value().deltaIn;
          choice->deltaOut = priorChoice.value().deltaOut;
          choice->instrumentId = priorChoice.value().instrumentId;
          choice->instrumentMode = priorChoice.value().instrumentMode;
          this->craftNoteEventArrangements(tempo, fabricator->put(*choice, false), false);
          continue;
        }

        auto instrument = instrumentProvider->get(voice);
        if (!instrument.has_value()) {
          continue;
        }

        // make new choices
        choice->deltaIn = computeDeltaIn(choice);
        choice->deltaOut = computeDeltaOut(choice);
        choice->instrumentId = instrument.value().id;
        choice->instrumentMode = instrument.value().mode;
        this->craftNoteEventArrangements(tempo, fabricator->put(*choice, false), false);
      }
    }

    /**
     Chord instrument mode
     https://github.com/xjmusic/xjmusic/issues/235

     @param tempo      of main program
     @param instrument for which to craft choices
     @on failure
     */
    void craftChordParts(double tempo, Instrument instrument) {
      // Craft each voice into choice
      auto *choice = new SegmentChoice();

      choice->id = EntityUtils::computeUniqueId();
      choice->segmentId = fabricator->getSegment().id;
      choice->mute = computeMute(instrument.type);
      choice->instrumentType = instrument.type;
      choice->instrumentMode = instrument.mode;
      choice->instrumentId = instrument.id;

      // Whether there is a prior choice for this voice
      std::optional<SegmentChoice> priorChoice = fabricator->getChoiceIfContinued(instrument.type);

      if (priorChoice.has_value()) {
        choice->deltaIn = priorChoice->deltaIn;
        choice->deltaOut = priorChoice->deltaOut;
        choice->instrumentId = priorChoice->instrumentId;
        fabricator->put(*choice, false);
        this->craftChordParts(tempo, instrument, choice);
        return;
      }

      // make new choices
      choice->deltaIn = computeDeltaIn(choice);
      choice->deltaOut = computeDeltaOut(choice);
      choice->instrumentId = instrument.id;
      fabricator->put(*choice, false);
      this->craftChordParts(tempo, instrument, choice);
    }

    /**
     Chord instrument mode
     https://github.com/xjmusic/xjmusic/issues/235

     @param tempo      of main program
     @param instrument chosen
     @param choice     for which to craft chord parts
     @on failure
     */
    void craftChordParts(double tempo, Instrument &instrument, SegmentChoice *choice) {
      if (fabricator->getSegmentChords().empty()) return;

      // Arrangement
      auto *arrangement = new SegmentChoiceArrangement();
      arrangement->id = EntityUtils::computeUniqueId();
      arrangement->segmentId = choice->segmentId;
      arrangement->segmentChoiceId = choice->id;
      fabricator->put(*arrangement);

      // Pick for each section
      for (auto &section: computeSections()) {
        auto audio = selectChordPartInstrumentAudio(instrument, Chord::of(section.chord.name));

        // Should gracefully skip audio in unfulfilled by instrument
        if (!audio.has_value()) continue;

        // Pick attributes are expressed "rendered" as actual seconds
        long startAtSegmentMicros = fabricator->getSegmentMicrosAtPosition(tempo, section.fromPos);
        std::optional<long> lengthMicros;
        if (fabricator->isOneShot(instrument)) {
          lengthMicros = std::nullopt;
        } else {
          lengthMicros = fabricator->getSegmentMicrosAtPosition(tempo, section.toPos) - startAtSegmentMicros;
        }

        // Volume ratio
        auto volRatio = computeVolumeRatioForPickedNote(choice, section.fromPos);
        if (volRatio <= 0) continue;

        // Pick
        auto *pick = new SegmentChoiceArrangementPick();
        pick->id = EntityUtils::computeUniqueId();
        pick->segmentId = choice->segmentId;
        pick->segmentChoiceArrangementId = arrangement->id;
        pick->instrumentAudioId = audio->id;
        pick->startAtSegmentMicros = startAtSegmentMicros;
        pick->tones = section.chord.name;
        pick->event = StringUtils::toEvent(Instrument::toString(instrument.type));
        if (lengthMicros.has_value()) pick->lengthMicros = lengthMicros.value();
        pick->amplitude = volRatio;
        fabricator->put(*pick);
      }

      // Final pass to set the actual length of one-shot audio picks
      finalizeNoteEventCutoffsOfOneShotInstrumentAudioPicks(choice);
    }

    /**
     Event instrument mode

     @param tempo      of main program
     @param instrument for which to craft choices
     @param program    for which to craft choices
     @on failure
     */
    void craftEventParts(double tempo, const Instrument &instrument, const Program &program) {
      // Event detail sequence is selected at random of the current instrument
      // FUTURE: Detail Instrument with multiple Sequences https://github.com/xjmusic/xjmusic/issues/241
      auto sequence = fabricator->getRandomlySelectedSequence(program);

      // Event voice arrangements
      if (sequence.has_value()) {
        auto voices = fabricator->getSourceMaterial()->getVoicesOfProgram(program);
        if (voices.empty()) return;
        InstrumentProvider *instrumentProvider = new LambdaInstrumentProvider(
            [&instrument](const ProgramVoice &voice) -> std::optional<Instrument> {
              return {instrument};
            });
        craftNoteEvents(tempo, sequence.value(), voices, instrumentProvider);
      }
    }

    /**
     Get the delta in for the given voice

     @param choice for which to get delta in
     @return delta in for given voice
     */
    int computeDeltaIn(SegmentChoice *choice) {
      auto it = deltaIns.find(choiceIndexProvider->get(choice));
      return (it != deltaIns.end()) ? it->second : SegmentChoice::DELTA_UNLIMITED;
    }

    /**
     Get the delta out for the given voice

     @param choice for which to get delta out
     @return delta out for given voice
     */
    int computeDeltaOut(SegmentChoice *choice) {
      auto it = deltaOuts.find(choiceIndexProvider->get(choice));
      return (it != deltaOuts.end()) ? it->second : SegmentChoice::DELTA_UNLIMITED;
    }

    /**
     Craft the arrangement for a given voice
     <p>
     Choice inertia
     https://github.com/xjmusic/xjmusic/issues/242
     Perform the inertia analysis, and determine whether they actually use the new choice or not
     IMPORTANT** If the previously chosen instruments are for the previous main program as the current segment,
     the inertia scores are not actually added to the regular scores or used to make choices--
     this would prevent new choices from being made. **Inertia must be its own layer of calculation,
     a question of whether the choices will be followed or whether the inertia will be followed**
     thus the new choices have been made, we know *where* we're going next,
     but we aren't actually using them yet until we hit the next main program in full, N segments later.
     <p>
     Ends with a pass to set the actual length of one-shot audio picks
     One-shot instruments cut off when other notes played with same instrument, or at end of segment https://github.com/xjmusic/xjmusic/issues/243

     @param tempo         of main program
     @param choice        to craft arrangements for
     @param defaultAtonal whether to default to a single atonal note, if no voicings are available
     @on failure
     */
    void craftNoteEventArrangements(double tempo, SegmentChoice *choicePtr, bool defaultAtonal) {
      SegmentChoice& choice = *choicePtr;
      // this is used to invert voicings into the tightest possible range
      // passed to each iteration of note voicing arrangement in order to move as little as possible from the previous
      NoteRange range = NoteRange::empty();

      if (!fabricator->getProgram(choice).has_value())
        throw FabricationException("Can't get program config");
      auto programConfig = XJ::Fabricator::getProgramConfig(fabricator->getProgram(choice).value());

      if (fabricator->getSegmentChords().empty())
        craftNoteEventSection(tempo, choice, 0, fabricator->getSegment().total, range, defaultAtonal);

      else if (programConfig.doPatternRestartOnChord)
        craftNoteEventSectionRestartingEachChord(tempo, choice, range, defaultAtonal);

      else craftNoteEventSection(tempo, choice, 0, fabricator->getSegment().total, range, defaultAtonal);

      // Final pass to set the actual length of one-shot audio picks
      finalizeNoteEventCutoffsOfOneShotInstrumentAudioPicks(choicePtr);
    }

    /**
     Precompute all deltas for a given program. This is where deltaIns and deltaOuts values come from.
     <p>
     Precompute deltas dynamically based on whatever is extending the arranger--
     Don't have anything in this class that's proprietary to beat or detail-- abstract that out into provider interfaces
     <p>
     Segments have intensity arcs; automate mixer layers in and out of each main program
     https://github.com/xjmusic/xjmusic/issues/233
     <p>
     Shift deltas so 2x more time is spent on construction than deconstruction
     <p>
     Vary the high plateau between delta in and out across layers

     @on failure
     */
    void precomputeDeltas(Predicate <SegmentChoice> choiceFilter, ChoiceIndexProvider choiceIndexProvider,
                          std::set<std::string> layers, std::set<std::string> layerPrioritizationSearches,
                          int numLayersIncoming) {
      this.choiceIndexProvider = choiceIndexProvider;
      deltaIns.clear();
      deltaOuts.clear();

      // Ensure that we can bypass delta arcs using the template config
      if (!fabricator->getTemplateConfig().isDeltaArcEnabled()) {
        layers.forEach(layer->
        {
          deltaIns.put(layer, SegmentChoice::DELTA_UNLIMITED);
          deltaOuts.put(layer, SegmentChoice::DELTA_UNLIMITED);
        });
        return;
      }

      // then we overwrite the wall-to-wall random values with more specific values depending on the situation
      switch (fabricator->getType()) {
        case PENDING->{
          // No Op
        }

        case INITIAL, NEXT_MAIN, NEXT_MACRO->
          {
            // randomly override N incoming (deltaIn unlimited) and N outgoing (deltaOut unlimited)
            // shuffle the layers into a random order, then step through them, assigning delta ins and then outs
            // random order in
            auto barBeats = fabricator->getCurrentMainProgramConfig().getBarBeats();
            auto deltaUnits = Bar.of(barBeats).computeSubsectionBeats(fabricator->getSegment().total);

            // Delta arcs can prioritize the presence of a layer by name, e.g. containing "kick"
            // separate layers into primary and secondary, shuffle them separately, then concatenate
            List <std::string> priLayers = new Arraystd::vector<>();
            List <std::string> secLayers = new Arraystd::vector<>();
            layers.forEach(layer->
            {
              auto layerName = layer.toLowerCase(Locale.ROOT);
              if (layerPrioritizationSearches.stream().anyMatch(m->layerName.contains(m.toLowerCase(Locale.ROOT))))
                priLayers.add(layer);
              else secLayers.add(layer);
            });
            Collections.shuffle(priLayers);
            if (!priLayers.isEmpty())
              fabricator->addInfoMessage(std::string.format("Prioritized %s", CsvUtils.join(priLayers)));
            Collections.shuffle(secLayers);
            auto orderedLayers = Stream.concat(priLayers.stream(), secLayers.stream()).toList();
            auto delta = ValueUtils.roundToNearest(deltaUnits, TremendouslyRandom.zeroToLimit(deltaUnits * 4) -
                                                               deltaUnits * 2 * numLayersIncoming);
            for (std::string orderedLayer: orderedLayers) {
              deltaIns.put(orderedLayer, delta > 0 ? delta : SegmentChoice::DELTA_UNLIMITED);
              deltaOuts.put(orderedLayer, SegmentChoice::DELTA_UNLIMITED); // all layers get delta out unlimited
              delta += ValueUtils.roundToNearest(deltaUnits, TremendouslyRandom.zeroToLimit(deltaUnits * 5));
            }
          }

        case CONTINUE->{
          for (std::string index: layers)
            fabricator->retrospective().getChoices().stream()
                .filter(choiceFilter)
                .filter(choice->Objects.equals(index, choiceIndexProvider.get(choice)))
                .findAny()
                .ifPresent(choice->deltaIns.put(choiceIndexProvider.get(choice), choice.deltaIn));
        }
      }
    }

  private:

    /**
     Iterate through all the chords of a sequence and arrange events per each chord
     <p>
     Detail programs can be made to repeat every chord change https://github.com/xjmusic/xjmusic/issues/244

     @param tempo         of main program
     @param choice        from which to craft events
     @param range         used to keep voicing in the tightest range possible
     @param defaultAtonal whether to default to a single atonal note, if no voicings are available
     @on failure
     */
    void
    craftNoteEventSectionRestartingEachChord(double tempo, SegmentChoice choice, NoteRange range, bool defaultAtonal) {
      for (auto section: computeSections())
        craftNoteEventSection(tempo, choice, section.fromPos, section.toPos, range, defaultAtonal);
    }

    /**
     Compute the segment chord sections

     @return sections in order of position ascending
     */
    std::vector<Section> computeSections() {
      // guaranteed to be in order of position ascending
      SegmentChord[]
      chords = new SegmentChord[fabricator->getSegmentChords().size()];
      auto i = 0;
      for (auto chord: fabricator->getSegmentChords()) {
        chords[i] = chord;
        i++;
      }
      Section[]
      sections = new Section[chords.length];
      for (i = 0; i < chords.length; i++) {
        sections[i] = new Section();
        sections[i].chord = chords[i];
        sections[i].fromPos = chords[i].getPosition();
        sections[i].toPos = i < chords.length - 1 ? chords[i + 1].getPosition() : fabricator->getSegment().total;
      }
      return Arrays.stream(sections).toList();
    }

    /**
     Craft events for a section of one detail voice

     @param tempo         of main program
     @param choice        from which to craft events
     @param fromPos       position (in beats)
     @param maxPos        position (in beats)
     @param range         used to keep voicing in the tightest range possible
     @param defaultAtonal whether to default to a single atonal note, if no voicings are available
     @on failure
     */
    void craftNoteEventSection(double tempo, SegmentChoice choice, double fromPos, double maxPos, NoteRange range,
                               bool defaultAtonal) {

      // begin at the beginning and fabricate events for the segment of beginning to end
      double curPos = fromPos;

      // choose loop patterns until arrive at the out point or end of segment
      while (curPos < maxPos) {
        std::optional<ProgramSequencePattern> loopPattern = fabricator->getRandomlySelectedPatternOfSequenceByVoiceAndType(
            choice);
        if (loopPattern.isPresent())
          curPos += craftPatternEvents(tempo, choice, loopPattern.get(), curPos, maxPos, range, defaultAtonal);
        else curPos = maxPos;
      }
    }

    /**
     Craft the voice events of a single pattern.
     Artist during craft audio selection wants randomness of outro audio selection to gently ramp of zero to N over the course of the outro.

     @param tempo         of main program
     @param pattern       to source events
     @param fromPosition  to write events to segment
     @param toPosition    to write events to segment
     @param range         used to keep voicing in the tightest range possible
     @param defaultAtonal whether to default to a single atonal note, if no voicings are available
     @return deltaPos of start, after crafting this batch of pattern events
     */
    double craftPatternEvents(double tempo, SegmentChoice choice, ProgramSequencePattern pattern, double fromPosition,
                              double toPosition, NoteRange range, bool defaultAtonal) {
      if (Objects.isNull(pattern)) throw new FabricationException("Cannot craft create null pattern");
      double loopBeats = toPosition - fromPosition;
      List <ProgramSequencePatternEvent> events = fabricator->getSourceMaterial()->getEventsOfPattern(pattern);

      auto arrangement = new SegmentChoiceArrangement();
      arrangement.setId(EntityUtils::computeUniqueId());
      arrangement.setSegmentId(choice.segmentId);
      arrangement.segmentChoiceId(choice.id);
      arrangement.setProgramSequencePatternId(pattern.id);
      fabricator->put(arrangement, false);

      auto instrument = fabricator->getSourceMaterial()->getInstrument(choice.instrumentId).orElseThrow(()->
      new FabricationException("Failed to retrieve instrument"));
      for (ProgramSequencePatternEvent event: events)
        pickNotesAndInstrumentAudioForEvent(tempo, instrument, choice, arrangement, fromPosition, toPosition, event,
                                            range, defaultAtonal);
      return Math.min(loopBeats, pattern.total);
    }

    /**
     of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
     pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement@param tempo

     @param choice        to pick notes for
     @param fromPosition  to pick notes for
     @param toPosition    to pick notes for
     @param event         to pick audio for
     @param range         used to keep voicing in the tightest range possible
     @param defaultAtonal whether to default to a single atonal note, if no voicings are available
     */
    void pickNotesAndInstrumentAudioForEvent(double tempo, Instrument instrument, SegmentChoice choice,
                                             SegmentChoiceArrangement arrangement, double fromPosition,
                                             double toPosition,
                                             ProgramSequencePatternEvent event, NoteRange range, bool defaultAtonal) {
      // Segment position is expressed in beats
      double segmentPosition = fromPosition + event.getPosition();

      // Should never place segment events outside of segment time range
      if (segmentPosition < 0 || segmentPosition >= fabricator->getSegment().total) return;

      double duration = Math.min(event.getDuration(), toPosition - segmentPosition);
      auto chord = fabricator->getChordAt(segmentPosition);
      std::optional<SegmentChordVoicing> voicing = chord.isPresent() ? fabricator->chooseVoicing(chord.get(),
                                                                                                 instrument.getType())
                                                                     : std::nullopt;

      auto volRatio = computeVolumeRatioForPickedNote(choice, segmentPosition);
      if (0 >= volRatio) return;

      // The note is voiced from the chord voicing (if found) or else the default is used
      Set <std::string> notes = voicing.isPresent() ? pickNotesForEvent(instrument.type, choice, event, chord.get(),
                                                                        voicing.get(), range) : (defaultAtonal ? Set.of(
          Note.ATONAL) : Set.of());

      // Pick attributes are expressed "rendered" as actual seconds
      long startAtSegmentMicros = fabricator->getSegmentMicrosAtPosition(tempo, segmentPosition);
      @Nullable long lengthMicros = fabricator->isOneShot(instrument, fabricator->getTrackName(event)) ? null :
                                    fabricator->getSegmentMicrosAtPosition(tempo, segmentPosition + duration) -
                                    startAtSegmentMicros;

      // pick an audio for each note
      for (auto note: notes)
        pickInstrumentAudio(note, instrument, event, arrangement, startAtSegmentMicros, lengthMicros,
                            voicing.map(SegmentChordVoicing::getId).orElse(null), volRatio);
    }

    /**
     Ends with a pass to set the actual length of one-shot audio picks
     One-shot instruments cut off when other notes played with same instrument, or at end of segment https://github.com/xjmusic/xjmusic/issues/243

     @param choice for which to finalize length of one-shot audio picks
     */
    void finalizeNoteEventCutoffsOfOneShotInstrumentAudioPicks(SegmentChoice *choice) {
      if (!fabricator->getSourceMaterial()->getInstrument(choice->instrumentId).has_value()) {
        throw FabricationException("Failed to get instrument from source material for segment choice!");
      }
      auto instrumentPtr = fabricator->getSourceMaterial()->getInstrument(choice->instrumentId).value();
      auto &instrument = *instrumentPtr;

      // skip instruments that are not one-shot
      if (!fabricator->isOneShot(instrument)) return;

      // skip instruments that are do not have one-shot cutoff enabled https://github.com/xjmusic/xjmusic/issues/225
      if (!fabricator->isOneShotCutoffEnabled(instrument)) return;

      // skip instruments that are not on the list
      if (finalizeAudioLengthsForInstrumentTypes.find(instrument.type) == finalizeAudioLengthsForInstrumentTypes.end()) return;

      // get all the picks, ordered chronologically, and skip the rest of this process if there are none
      std::vector<SegmentChoiceArrangementPick> picks = fabricator->getPicks(choice);
      if (picks.isEmpty()) return;

      // build an ordered unique list of the moments in time when the one-shot will be cut off
      std::vector<long> cutoffAtSegmentMicros = picks.stream().map(
          SegmentChoiceArrangementPick::getStartAtSegmentMicros).collect(Collectors.toSet()).stream().sorted().toList();

      // iterate and set lengths of all picks in series
      for (SegmentChoiceArrangementPick pick: picks) {

        // Skip picks that already have their end length set
        if (Objects.nonNull(pick.getLengthMicros())) continue;

        auto nextCutoffAtSegmentMicros = cutoffAtSegmentMicros.stream().filter(
            c->c > pick.getStartAtSegmentMicros()).findFirst();

        if (nextCutoffAtSegmentMicros.isPresent()) {
          pick.setLengthMicros(nextCutoffAtSegmentMicros.get() - pick.getStartAtSegmentMicros());
          fabricator->put(pick, false);
          continue;
        }

        if (pick.getStartAtSegmentMicros() < fabricator->getTotalSegmentMicros()) {
          pick.setLengthMicros(fabricator->getTotalSegmentMicros() - pick.getStartAtSegmentMicros());
          fabricator->put(pick, false);
          continue;
        }

        fabricator->
        delete (pick.segmentId, SegmentChoiceArrangementPick.
        class, pick.id);
      }
    }

    /**
     Compute the volume ratio of a picked note

     @param choice          for which to compute volume ratio
     @param segmentPosition at which to compute
     @return volume ratio
     */
    float computeVolumeRatioForPickedNote(SegmentChoice *choice, double segmentPosition) {
      if (!fabricator->getTemplateConfig().isDeltaArcEnabled()) return 1.0f;
      return (float) (inBounds(choice.deltaIn, choice.deltaOut, fabricator->getSegment().delta + segmentPosition) ? 1.0
                                                                                                                  : 0.0);
    }

    /**
     Pick note based on instrument type, voice event, transposition and current chord
     <p>
     XJ should choose correct instrument note based on detail program note

     @param instrumentType  comprising audios
     @param choice          for reference
     @param event           of program to pick instrument note for
     @param rawSegmentChord to use for interpreting the voicing
     @param voicing         to choose a note from
     @param optimalRange    used to keep voicing in the tightest range possible
     @return note picked from the available voicing
     */
    Set <std::string>
    pickNotesForEvent(Instrument::Type instrumentType, SegmentChoice choice, ProgramSequencePatternEvent event,
                      SegmentChord rawSegmentChord, SegmentChordVoicing voicing, NoteRange optimalRange) {
      // Various computations to prepare for picking
      auto segChord = Chord.of(rawSegmentChord.getName());
      auto dpKey = fabricator->getKeyForChoice(choice);
      auto dpRange = fabricator->getProgramRange(choice.programId, instrumentType);
      auto voicingListRange = fabricator->getProgramVoicingNoteRange(instrumentType);

      // take semitone shift into project before computing octave shift! https://github.com/xjmusic/xjmusic/issues/245
      auto dpTransposeSemitones = fabricator->getProgramTargetShift(instrumentType, dpKey, segChord);
      auto dpTransposeOctaveSemitones = 12 * fabricator->getProgramRangeShiftOctaves(instrumentType, dpRange.shifted(
          dpTransposeSemitones), voicingListRange);

      // Event notes are either interpreted from specific notes in dp, or via sticky bun from X notes in dp
      List <Note> eventNotes = CsvUtils.split(event.getTones()).stream().map(
          n->Note.of(n).shift(dpTransposeSemitones + dpTransposeOctaveSemitones)).sorted().collect(Collectors.toList());
      auto dpEventRelativeOffsetWithinRangeSemitones = dpRange.shifted(
          dpTransposeSemitones + dpTransposeOctaveSemitones).getDeltaSemitones(NoteRange.ofNotes(eventNotes));
      auto dpEventRangeWithinWholeDP = NoteRange.ofNotes(eventNotes).shifted(dpEventRelativeOffsetWithinRangeSemitones);

      if (optimalRange.isEmpty() && !dpEventRangeWithinWholeDP.isEmpty())
        optimalRange.expand(dpEventRangeWithinWholeDP);

      // Leverage segment meta to look up a sticky bun if it exists
      auto bun = fabricator->getStickyBun(event.id);

      // Prepare voicing notes and note picker
      auto voicingNotes = fabricator->getNotes(voicing).stream().flatMap(Note::ofValid).collect(Collectors.toList());
      auto notePicker = new NotePicker(optimalRange.shifted(dpEventRelativeOffsetWithinRangeSemitones), voicingNotes,
                                       fabricator->getTemplateConfig().getInstrumentTypesForInversionSeeking().contains(
                                           instrumentType));

      // Go through the notes in the event and pick a note from the voicing, either by note picker or by sticky bun
      List <Note> pickedNotes = new Arraystd::vector<>();
      for (auto i = 0; i < eventNotes.size(); i++) {
        auto pickedNote =
            eventNotes.get(i).isAtonal() && bun.isPresent() ? bun.get().compute(voicingNotes, i) : notePicker.pick(
                eventNotes.get(i));
        pickedNotes.add(pickedNote);
      }

      auto pickedNoteStrings = pickedNotes.stream().map(n->n.toString(segChord.getAdjSymbol())).collect(
          Collectors.toSet());

      // expand the optimal range for voice leading by the notes that were just picked
      optimalRange.expand(pickedNotes);

      // outcome
      return pickedNoteStrings;
    }

    /**
     XJ has a serviceable voicing algorithm https://github.com/xjmusic/xjmusic/issues/221
     <p>
     Artist can edit comma-separated notes into detail program events https://github.com/xjmusic/xjmusic/issues/246
     <p>
     of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
     pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement

     @param note                     to pick audio for
     @param instrument               from which to pick audio
     @param event                    to pick audio for
     @param segmentChoiceArrangement arranging this instrument for a program
     @param startAtSegmentMicros     of audio
     @param lengthMicros             of audio
     @param volRatio                 ratio of volume
     @on failure
     */
    void pickInstrumentAudio(
        std::string note,
        Instrument instrument,
        ProgramSequencePatternEvent event,
        SegmentChoiceArrangement segmentChoiceArrangement,
        long startAtSegmentMicros,
        std::optional<long> lengthMicros,
        std::optional<UUID> segmentChordVoicingId,
        float volRatio
    ) {
      auto audio = fabricator->getInstrumentConfig(instrument).isMultiphonic ? selectMultiphonicInstrumentAudio(
          instrument, event, note) : selectMonophonicInstrumentAudio(instrument, event);

      // Should gracefully skip audio if unfulfilled by instrument https://github.com/xjmusic/xjmusic/issues/240
      if (!audio.has_value()) return;

      // of pick
      auto pick = new SegmentChoiceArrangementPick();
      pick->id = EntityUtils::computeUniqueId();
      pick->segmentId = segmentChoiceArrangement.segmentId;
      pick->segmentChoiceArrangementId = segmentChoiceArrangement.id;
      pick->instrumentAudioId = audio.value().id;
      pick->programSequencePatternEventId = event.id;
      pick->event = fabricator->getTrackName(event);
      pick->startAtSegmentMicros = startAtSegmentMicros;
      if (lengthMicros.has_value()) pick->lengthMicros = lengthMicros.value();
      pick->amplitude = event.velocity * volRatio;
      pick->tones = fabricator->getInstrumentConfig(instrument).isTonal ? note : Note::ATONAL;
      if (segmentChordVoicingId.has_value()) pick->segmentChordVoicingId = segmentChordVoicingId.value();
      fabricator->put(*pick);
    }

    /**
     Select audio from a multiphonic instrument
     <p>
     Sampler obeys isMultiphonic from Instrument config https://github.com/xjmusic/xjmusic/issues/252

     @param instrument of which to score available audios, and make a selection
     @param event      for caching reference
     @param note       to match selection
     @return matched new audio
     */
    std::optional<InstrumentAudio>
    selectMultiphonicInstrumentAudio(Instrument instrument, ProgramSequencePatternEvent event, std::string note) {
      if (fabricator->getInstrumentConfig(instrument).isAudioSelectionPersistent()) {
        if (fabricator->getPreferredAudio(event.getProgramVoiceTrackId().toString(), note).isEmpty()) {
          auto audio = selectNewMultiphonicInstrumentAudio(instrument, note);
          audio.ifPresent(
              instrumentAudio->fabricator->putPreferredAudio(event.getProgramVoiceTrackId().toString(), note,
                                                             instrumentAudio));
        }
        return fabricator->getPreferredAudio(event.getProgramVoiceTrackId().toString(), note);

      } else {
        return selectNewMultiphonicInstrumentAudio(instrument, note);
      }
    }

    /**
     Select audio from a multiphonic instrument
     <p>
     If never encountered, default to new selection and cache that.

     @param instrument of which to score available audios, and make a selection
     @param event      to match selection
     @return matched new audio
     @on failure
     */
    std::optional<InstrumentAudio>
    selectMonophonicInstrumentAudio(Instrument instrument, ProgramSequencePatternEvent event) {
      if (fabricator->getInstrumentConfig(instrument).isAudioSelectionPersistent()) {
        if (fabricator->getPreferredAudio(event.getProgramVoiceTrackId().toString(), event.getTones()).isEmpty())
          fabricator->putPreferredAudio(event.getProgramVoiceTrackId().toString(), event.getTones(),
                                        selectNewNoteEventInstrumentAudio(instrument, event).orElseThrow(()->
        new FabricationException("Unable to select note event instrument audio!")));
        return fabricator->getPreferredAudio(event.getProgramVoiceTrackId().toString(), event.getTones());

      } else {
        return selectNewNoteEventInstrumentAudio(instrument, event);
      }
    }

    /**
     Chord instrument mode
     https://github.com/xjmusic/xjmusic/issues/235
     <p>
     If never encountered, default to new selection and cache that.

     @param instrument of which to score available audios, and make a selection
     @param chord      to match selection
     @return matched new audio
     */
    std::optional<InstrumentAudio> selectChordPartInstrumentAudio(Instrument instrument, Chord chord) {
      if (fabricator->getInstrumentConfig(instrument).isAudioSelectionPersistent()) {
        if (fabricator->getPreferredAudio(instrument.id.toString(), chord.getName()).isEmpty()) {
          auto audio = selectNewChordPartInstrumentAudio(instrument, chord);
          audio.ifPresent(
              instrumentAudio->fabricator->putPreferredAudio(instrument.id.toString(), chord.getName(),
                                                             instrumentAudio));
        }
        return fabricator->getPreferredAudio(instrument.id.toString(), chord.getName());

      } else {
        return selectNewChordPartInstrumentAudio(instrument, chord);
      }
    }

    /**
     Select a new random instrument audio based on a pattern event

     @param instrument of which to score available audios, and make a selection
     @param event      to match
     @return matched new audio
     */
    std::optional<InstrumentAudio>
    selectNewNoteEventInstrumentAudio(Instrument instrument, ProgramSequencePatternEvent event) {
      std::map<UUID, int> score = new HashMap<>();

      // add all audio to chooser
      fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument).forEach(a->score.put(a.id, 0));

      // score each audio against the current voice event, with some variability
      for (InstrumentAudio audio: fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument))
        if (instrument.type == Instrument::Type::Drum)
          score.put(audio.id, Objects.equals(fabricator->getTrackName(event), audio.getEvent()) ? 300 : 0);
        else if (Note.of(audio.getTones()).sameAs(Note.of(event.getTones())))
          score.put(audio.id, 100);

      // chosen audio event
      auto pickId = ValueUtils.getKeyOfHighestValue(score);
      return pickId.isPresent() ? fabricator->getSourceMaterial()->getInstrumentAudio(pickId.get())
                                : std::nullopt;
    }


    /**
     Select a new random instrument audio based on a pattern event
     <p>
     Sampler obeys isMultiphonic from Instrument config https://github.com/xjmusic/xjmusic/issues/252

     @param instrument of which to score available audios, and make a selection
     @param note       to match
     @return matched new audio
     */
    std::optional<InstrumentAudio> selectNewMultiphonicInstrumentAudio(Instrument instrument, std::string note) {
      auto instrumentAudios = fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument);
      auto a = Note.of(note);
      auto audio = MarbleBag.quickPick(instrumentAudios.stream().filter(candidate-> {
        if (Objects.isNull(candidate) || StringUtils.isNullOrEmpty(candidate.getTones())) return false;
        auto b = Note.of(candidate.getTones());
        return a.isAtonal() || b.isAtonal() || a.sameAs(b);
      }).toList());

      if (audio.isEmpty()) {
        reportMissing(std::map.of("instrumentId", instrument.id.toString(), "searchForNote", note, "availableNotes",
                                  CsvUtils.from(
                                      instrumentAudios.stream().map(InstrumentAudio::getTones).map(Note::of).sorted(
                                          Note::compareTo).map(N->N.toString(Accidental.Sharp)).collect(
                                          Collectors.toList()))));
        return std::nullopt;
      }

      return fabricator->getSourceMaterial()->getInstrumentAudio(audio.get().id);
    }

    /**
     Test if an instrument contains audios named like N
     <p>
     Choose drum instrument to fulfill beat program event names https://github.com/xjmusic/xjmusic/issues/253

     @param instrument    to test
     @param requireEvents N
     @return true if instrument contains audios named like N or required event names list is empty
     */
    bool instrumentContainsAudioEventsLike(const Instrument *instrument, std::set<std::string> requireEvents) {
      if (requireEvents.isEmpty()) return true;
      for (auto event: requireEvents)
        if (fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument.id).stream().noneMatch(
            a->Objects.equals(event, a.getEvent())))
          return false;
      return true;
    }
  };

}