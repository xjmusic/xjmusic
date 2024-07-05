// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include <vector>

#include "xjmusic/craft/Craft.h"
#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/util/CsvUtils.h"
#include "xjmusic/util/ValueUtils.h"

#include "../_helper/ContentFixtures.h"
#include "../_helper/SegmentFixtures.h"
#include "../_helper/YamlTest.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

/**
 XJ has a serviceable voicing algorithm https://github.com/xjmusic/xjmusic/issues/221
 */
class ArrangementTest : public YamlTest {
protected:
  std::string TEST_PATH_PREFIX = "_data/arrangements/";
  int REPEAT_EACH_TEST_TIMES = 7;

  int TEMPO = 60;// 60 BPM such that 1 beat = 1 second
  std::set<Instrument::Type> INSTRUMENT_TYPES_TO_TEST = {
      Instrument::Type::Bass,
      Instrument::Type::Pad,
      Instrument::Type::Stab,
      Instrument::Type::Stripe,
      Instrument::Type::Sticky};
  // this is how we provide content for fabrication
  std::unique_ptr<FabricatorFactory> fabrication;
  std::unique_ptr<SegmentEntityStore> store;
  std::unique_ptr<ContentEntityStore> content;
  std::unique_ptr<Fabricator> fabricator;
  // list of all entities to return from Hub
  // maps with specific entities that will reference each other
  std::map<Instrument::Type, Instrument> instruments;
  std::map<Instrument::Type, Program> detailPrograms;
  std::map<Instrument::Type, ProgramVoice> detailProgramVoices;
  std::map<Instrument::Type, ProgramSequence> detailProgramSequences;
  std::map<Instrument::Type, std::vector<ProgramSequencePatternEvent>> detailProgramSequencePatternEvents;
  std::vector<StickyBun> stickyBuns;
  Chain *chain = nullptr;
  const Segment *segment = nullptr;
  std::map<Instrument::Type, const SegmentChoice *> segmentChoices;
  Program mainProgram1;


  /**
   Reset the resources before each repetition of each test
   */
  void reset() {
    store = std::make_unique<SegmentEntityStore>();
    fabrication = std::make_unique<FabricatorFactory>(store.get());

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    const auto project1 = ContentFixtures::buildProject("fish");
    const Template template1 = ContentFixtures::buildTemplate(&project1, "Test Template 1", "test1");
    const auto library1 = ContentFixtures::buildLibrary(&project1, "palm tree");
    mainProgram1 = ContentFixtures::buildProgram(&library1, Program::Type::Main, Program::State::Published, "ANTS",
                                                 "C#",
                                                 60.0f);// 60 BPM such that 1 beat = 1 second
    chain = store->put(SegmentFixtures::buildChain(&template1));

    // prepare content
    content = std::make_unique<ContentEntityStore>();
    content->put(template1);
    content->put(library1);
    content->put(mainProgram1);

    // prepare maps with specific entities that will reference each other
    instruments = {};
    detailPrograms = {};
    detailProgramVoices = {};
    detailProgramSequences = {};
    detailProgramSequencePatternEvents = {};
    stickyBuns = {};
    segmentChoices = {};
  }

  /**
   Load the instrument section of the test YAML file, for one type of Instrument@param data YAML file wrapper

   @param type of instrument to read
   */
  void loadInstrument(YAML::Node data, Instrument::Type type) {
    auto dataKey = StringUtils::toLowerCase(Instrument::toString(type)) + "Instrument";
    try {
      YAML::Node obj = data[dataKey];
      if (obj.IsNull()) return;

      auto isTonalObj = obj["isTonal"];
      auto isMultiphonicObj = obj["isMultiphonic"];
      auto notesObj = obj["notes"];
      if (isTonalObj.IsNull() || isMultiphonicObj.IsNull() || notesObj.IsNull()) return;

      const auto isTonalBool = static_cast<std::optional<bool>>(isTonalObj);
      const auto isMultiphonicBool = static_cast<std::optional<bool>>(isMultiphonicObj);
      const auto instrument = ContentFixtures::buildInstrument(
          type,
          Instrument::Mode::Event,
          isTonalBool.value(),
          isMultiphonicBool.value());
      instruments[type] = instrument;

      if (!notesObj.IsScalar()) return;
      const auto notesString = notesObj.as<std::string>();
      for (const auto &item: ContentFixtures::buildInstrumentWithAudios(
               &instrument,
               notesString)) {
        // check if item is Instrument or InstrumentAudio
        if (std::holds_alternative<Instrument>(item)) {
          content->put(std::get<Instrument>(item));
        } else if (std::holds_alternative<InstrumentAudio>(item)) {
          content->put(std::get<InstrumentAudio>(item));
        }
      }

    } catch (const YAML::Exception &e) {
      spdlog::warn("[data[{}]] Exception: {}", dataKey, e.what());
    }
  }

  /**
   Load the detail program section of the test YAML file, for one type of Instrument

   @param data YAML file wrapper
   @param type of instrument to read
   */
  void loadDetailProgram(YAML::Node data, Instrument::Type type) {
    auto dataKey = StringUtils::toLowerCase(Instrument::toString(type)) + "DetailProgram";
    try {
      YAML::Node obj = data[dataKey];
      if (obj.IsNull()) return;

      auto keyObj = obj["key"];
      auto doPatternRestartOnChordObj = obj["doPatternRestartOnChord"];
      if (keyObj.IsNull() || doPatternRestartOnChordObj.IsNull()) return;
      if (!keyObj.IsScalar() || !doPatternRestartOnChordObj.IsScalar()) return;

      auto program = ContentFixtures::buildDetailProgram(
          keyObj.as<std::string>(),
          doPatternRestartOnChordObj.as<bool>(),
          Instrument::toString(type) + " Test");
      detailPrograms[type] = program;
      content->put(program);

      auto voice = ContentFixtures::buildVoice(&program, type);
      detailProgramVoices[type] = voice;
      content->put(voice);

      auto track = ContentFixtures::buildTrack(&voice);
      content->put(track);

      YAML::Node sObj = obj["sequence"];
      auto sequence = ContentFixtures::buildSequence(&program, sObj["total"].as<int>());
      detailProgramSequences[type] = sequence;
      content->put(sequence);

      YAML::Node pObj = sObj["pattern"];
      auto pattern = ContentFixtures::buildPattern(&sequence, &voice, pObj["total"].as<int>());
      content->put(pattern);
      for (YAML::Node eObj: pObj["events"]) {
        auto event = ContentFixtures::buildEvent(&pattern, &track,
                                                 eObj["position"].as<float>(),
                                                 eObj["duration"].as<float>(),
                                                 eObj["tones"].as<std::string>());
        content->put(event);
        if (detailProgramSequencePatternEvents.find(type) == detailProgramSequencePatternEvents.end()) {
          detailProgramSequencePatternEvents[type] = std::vector<ProgramSequencePatternEvent>();
        }
        detailProgramSequencePatternEvents[type].push_back(event);
      }
    } catch (const YAML::Exception &e) {
      spdlog::warn("[data[{}]] Exception: {}", dataKey, e.what());
    }
  }

  /**
   Load the segment section of the test YAML file

   @param data YAML file wrapper
   */
  void loadSegment(YAML::Node data) {
    try {
      YAML::Node obj = data["segment"];

      segment = store->put(SegmentFixtures::buildSegment(chain,
                                                         getStr(obj, "key").value(),
                                                         getInt(obj, "total").value(),
                                                         getFloat(obj, "intensity").value(),
                                                         static_cast<float>(TEMPO)));

      /*

    if (obj.containsKey("stickyBuns")) {
      for (Map<?, ?> sbObj : (List<Map<?, ?>>) obj.get("stickyBuns")) {
        auto sbType = InstrumentType.valueOf(getStr(sbObj, "type"));
        auto sbPosition = getFloat(sbObj, "position");
        auto sbSeed = getInt(sbObj, "seed");
        auto event = detailProgramSequencePatternEvents.get(sbType).stream()
          .filter(e -> e.getPosition()== sbPosition)
          .findAny()
          .orElseThrow(() -> FabricationException(String.format("Failed to locate event type %s position %f", sbType, sbPosition)));
        stickyBuns.add(new StickyBun(event->id, List.of(Objects.requireNonNull(sbSeed))));
      }
    }
       */

      if (obj["stickyBuns"]) {
        for (YAML::Node sbObj: obj["stickyBuns"]) {
          auto sbType = Instrument::parseType(getStr(sbObj, "type").value());
          auto sbPosition = getFloat(sbObj, "position").value();
          auto sbSeed = getInt(sbObj, "seed").value();
          auto it = detailProgramSequencePatternEvents.find(sbType);
          if (it == detailProgramSequencePatternEvents.end()) {
            throw FabricationException("Failed to locate event type " + Instrument::toString(sbType));
          }

          auto eventIt = std::find_if(it->second.begin(), it->second.end(), [&](const auto &e) {
            return e.position == sbPosition;
          });

          if (eventIt == it->second.end()) {
            throw FabricationException("Failed to locate event type " + Instrument::toString(sbType) + " position " +
                                       std::to_string(sbPosition));
          }
          auto bun = StickyBun(eventIt->id, std::vector{sbSeed});
          stickyBuns.emplace_back(bun);
        }
      }

      for (YAML::Node cObj: obj["chords"]) {
        auto chord = store->put(SegmentFixtures::buildSegmentChord(segment,
                                                                   getFloat(cObj).value(),
                                                                   getStr(cObj, "name").value()));
        YAML::Node vObj = cObj["voicings"];
        for (const auto &[instrumentType, instrument]: instruments) {
          if (auto notes = getStr(vObj,
                                  StringUtils::toLowerCase(Instrument::toString(instrument.type)));
              notes.has_value())
            store->put(SegmentFixtures::buildSegmentChordVoicing(chord, instrument.type, notes.value()));
        }
      }

      for (const auto &[instrumentType, instrument]: instruments)
        if (detailPrograms.count(instrument.type) &&
            detailProgramSequences.count(instrument.type) &&
            detailProgramVoices.count(instrument.type))
          segmentChoices[instrument.type] =
              store->put(SegmentFixtures::buildSegmentChoice(segment,
                                                             &detailPrograms[instrument.type],
                                                             &detailProgramSequences[instrument.type],
                                                             &detailProgramVoices[instrument.type],
                                                             &instrument));
    } catch (const YAML::Exception &e) {
      spdlog::warn("[segment] Exception: {}", e.what());
    }
  }

  /**
   Load the assertions of picks section after a test has run
   Load the instrument section of the test YAML file, for one type of Instrument@param data YAML file wrapper
   */
  void loadAndPerformAssertions(YAML::Node data) const {
    const YAML::Node obj = data["assertPicks"];
    if (!obj) return;
    for (const auto type: INSTRUMENT_TYPES_TO_TEST) loadAndPerformAssertions(obj, type);
  }

  /**
   * Load the assertions of picks section after a test has run
   * @param data  YAML file wrapper
   * @param type  type of instrument to read
   */
  void loadAndPerformAssertions(YAML::Node data, Instrument::Type type) const {
    auto objs = data[StringUtils::toLowerCase(Instrument::toString(type))];
    if (!objs) return;

    std::vector<const SegmentChoiceArrangementPick *> actualPicks;
    for (const auto &pick: fabricator->getPicks())
      actualPicks.emplace_back(pick);
    std::sort(actualPicks.begin(), actualPicks.end(), [](const auto *a, const auto *b) {
      return a->startAtSegmentMicros < b->startAtSegmentMicros;
    });

    for (auto obj: objs) {
      auto startAtSeconds = getFloat(static_cast<YAML::Node>(obj), "start");
      auto lengthSeconds = getFloat(static_cast<YAML::Node>(obj), "length");
      std::optional<long> startAtMicros = std::nullopt;
      if (startAtSeconds.has_value())
        startAtMicros = static_cast<long>(startAtSeconds.value() * static_cast<float>(ValueUtils::MICROS_PER_SECOND));
      std::optional<long> lengthMicros = std::nullopt;
      if (lengthSeconds.has_value())
        lengthMicros = static_cast<long>(lengthSeconds.value() * static_cast<float>(ValueUtils::MICROS_PER_SECOND));
      auto count = getInt(static_cast<YAML::Node>(obj), "count");
      auto notes = getStr(static_cast<YAML::Node>(obj), "notes");

      auto assertionName = Instrument::toString(type) + "-type actualNoteStrings" +
                           " starting at " + (startAtMicros.has_value() ? std::to_string(startAtSeconds.value()) : "") +
                           "s" +
                           " with length " + (lengthMicros.has_value() ? std::to_string(lengthSeconds.value()) : "") +
                           "s";

      std::vector<std::string> actualNoteStrings;
      for (const auto &pick: actualPicks) {
        if (pick->event == Instrument::toString(type) &&
            (!startAtMicros.has_value() || startAtMicros.value() == pick->startAtSegmentMicros) &&
            (!lengthMicros.has_value() || lengthMicros.value() == pick->lengthMicros)) {
          actualNoteStrings.push_back(pick->tones);
        }
      }

      if (count.has_value())
        ASSERT_EQ(count.value(), actualNoteStrings.size())
            << "Count " + std::to_string(count.value()) + " " + assertionName;

      if (notes.has_value()) {
        std::vector<std::string> expectedNoteStrings = CsvUtils::split(notes.value());
        std::set<Note> expectedNotesSet;
        for (const auto &noteString: expectedNoteStrings) {
          expectedNotesSet.insert(Note::of(noteString));
        }
        std::set<Note> actualNotesSet;
        for (const auto &noteString: actualNoteStrings) {
          actualNotesSet.insert(Note::of(noteString));
        }
        ASSERT_EQ(expectedNotesSet, actualNotesSet) << "Notes of " + assertionName;
      }
    }
  }

  /**
   Load the specified test YAML file and run it repeatedly.

   @param filename of test YAML file
   */
  void loadAndRunTest(const std::string &filename) {
    for (int i = 0; i < REPEAT_EACH_TEST_TIMES; i++)
      try {
        reset();

        // Load YAML and parse
        const auto data = loadYaml(TEST_PATH_PREFIX, filename);

        // Read Instruments and Detail Programs from the test YAML
        for (const auto instrumentType: INSTRUMENT_TYPES_TO_TEST) {
          loadInstrument(data, instrumentType);
          loadDetailProgram(data, instrumentType);
        }

        // Read Segment and make choices of instruments and programs
        loadSegment(data);

        // Fabricate: Craft Arrangements for Choices
        fabricator = fabrication->fabricate(content, segment->id, std::nullopt);
        for (const StickyBun &bun: stickyBuns) {
          fabricator->putStickyBun(bun);
        }
        fabricator->put(SegmentFixtures::buildSegmentChoice(segment, &mainProgram1), false);
        auto subject = Craft(fabricator);
        for (const auto &[instrumentType, choice]: segmentChoices)
          subject.craftNoteEventArrangements(static_cast<float>(TEMPO), choice, false);

        // assert picks
        loadAndPerformAssertions(data);

      } catch (const FabricationException &e) {
        failures.emplace("[" + filename + "] Exception: " + e.what());
      }
  }
};


TEST_F(ArrangementTest, ArrangementBaseline) {
  auto prg = Program();
  prg.id = EntityUtils::computeUniqueId();
  prg.name = "Baseline";

  prg.name = "Modified";
  ASSERT_EQ("Modified", prg.name);
}

TEST_F(ArrangementTest, Arrangement1) {
  loadAndRunTest("arrangement_1.yaml");
}

TEST_F(ArrangementTest, Arrangement2) {
  loadAndRunTest("arrangement_2.yaml");
}

TEST_F(ArrangementTest, Arrangement3) {
  loadAndRunTest("arrangement_3.yaml");
}

TEST_F(ArrangementTest, Arrangement4) {
  loadAndRunTest("arrangement_4.yaml");
}

TEST_F(ArrangementTest, Arrangement5) {
  loadAndRunTest("arrangement_5.yaml");
}

TEST_F(ArrangementTest, Arrangement6) {
  loadAndRunTest("arrangement_6.yaml");
}

TEST_F(ArrangementTest, Arrangement7) {
  loadAndRunTest("arrangement_7.yaml");
}

TEST_F(ArrangementTest, Arrangement8) {
  loadAndRunTest("arrangement_8.yaml");
}

TEST_F(ArrangementTest, Arrangement9) {
  loadAndRunTest("arrangement_9.yaml");
}

TEST_F(ArrangementTest, Arrangement10) {
  loadAndRunTest("arrangement_10.yaml");
}

TEST_F(ArrangementTest, Arrangement_12_sticky_bun_basic) {
  loadAndRunTest("arrangement_12_sticky_bun_basic.yaml");
}

TEST_F(ArrangementTest, Arrangement0_NoChordSections) {
  loadAndRunTest("arrangement_0_no_chord_sections.yaml");
}
