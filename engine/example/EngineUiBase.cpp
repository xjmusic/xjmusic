// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.


#include <iomanip>

#include <SDL2/SDL.h>
#include <ftxui/component/component.hpp>
#include <ftxui/component/screen_interactive.hpp>
#include <ftxui/dom/table.hpp>

#include "EngineUiBase.h"

EngineUiBase::EngineUiBase(
    const std::string &pathToProjectFile,
    Fabricator::ControlMode controlMode,
    std::optional<int> craftAheadSeconds,
    std::optional<int> dubAheadSeconds,
    std::optional<int> persistenceWindowSeconds)
    : engine(std::make_unique<Engine>(pathToProjectFile,
                                      controlMode,
                                      craftAheadSeconds,
                                      dubAheadSeconds,
                                      persistenceWindowSeconds)),
      screen(ScreenInteractive::Fullscreen()) {
  AtChainMicros = 0;
  ui_tab_selected = 0;
}

const Template *EngineUiBase::SelectTemplate() {
  using namespace ftxui;

  std::vector<const Template *> AllTemplates;
  for (const Template *Template: engine->getProjectContent()->getTemplates()) {
    AllTemplates.push_back(Template);
  }
  std::sort(AllTemplates.begin(), AllTemplates.end(), [](const Template *a, const Template *b) {
    return a->name < b->name;
  });

  // Extract template names
  std::vector<std::string> templateNames;
  templateNames.reserve(AllTemplates.size());
  for (const auto &tmpl: AllTemplates) {
    templateNames.push_back(tmpl->name);
  }

  int selected = 0;
  MenuOption option;
  option.on_enter = screen.ExitLoopClosure();

  auto header_text = Renderer([] {
    return text("Select a template and press ENTER") | bold;
  });
  auto template_menu = Menu(&templateNames, &selected, option);
  auto container = Container::Vertical({
                                           header_text,
                                           template_menu,
                                       });

  auto document = Renderer(container, [&] {
    return vbox({
                    header_text->Render(),
                    separator(),
                    template_menu->Render(),
                }) |
           border;
  });

  screen.Clear();
  screen.Loop(document);

  // Return the selected template
  if (selected >= 0 && selected < AllTemplates.size()) {
    return AllTemplates[selected];
  } else {
    throw std::invalid_argument("Invalid template index");
  }
}

std::shared_ptr<ComponentBase> EngineUiBase::BuildRunningUI() {
  memeTaxonomy = engine->getMemeTaxonomy()->toMap();
  for (const auto& category: memeTaxonomy) {
    memeTaxonomySelection[category.first] = 0;
    std::vector<Element> memeList;
    for (const auto &meme: category.second) {
      memeList.push_back(text(meme));
    }
    if (!memeTaxonomyCategories.empty()) {
      memeTaxonomyCategories.push_back(Renderer([] { return separator(); }));
    }
    memeTaxonomyCategories.push_back(Renderer([] { return separatorEmpty(); }));
    memeTaxonomyCategories.push_back(
        Container::Vertical({
                               Renderer([&category] { return hbox(separatorEmpty(), text(category.first) | bold); }),
                               Renderer([] { return separator(); }),
                               Radiobox(&memeTaxonomy[category.first], &memeTaxonomySelection[category.first]),
                           }));
    memeTaxonomyCategories.push_back(Renderer([] { return separatorEmpty(); }));
  }

  ui_tab_content_template = Container::Horizontal({
                                                      Renderer([] { return separatorEmpty(); }),
                                                      Container::Vertical({
                                                                              Container::Vertical({
                                                                                                      Renderer([] {
                                                                                                        return hbox(
                                                                                                            {filler(),
                                                                                                             text(
                                                                                                                 "Meme Taxonomy") |
                                                                                                             bold,
                                                                                                             filler()});
                                                                                                      }),
                                                                                                      Renderer(
                                                                                                          [] { return separator(); }),
                                                                                                      Container::Horizontal(
                                                                                                          memeTaxonomyCategories)
                                                                                                  }) | border,
                                                                          }),
                                                      Renderer([] { return separatorEmpty(); }),
                                                  });

  ui_tab_content_segments = Renderer([this] {
    std::vector<Element> segments;
    for (auto &segment: engine->getSegmentStore()->readAllSegments()) {
      if (segment->durationMicros.has_value() && segment->beginAtChainMicros + segment->durationMicros.value() >
                                                 AtChainMicros) {

        std::set < std::string > segMemeNames;
        for (auto &meme: engine->getSegmentStore()->readAllSegmentMemes(segment->id)) {
          segMemeNames.emplace(meme->name);
        }
        std::vector<std::string> segMemeNamesSorted;
        segMemeNamesSorted.reserve(segMemeNames.size());
        for (auto &name: segMemeNames) {
          segMemeNamesSorted.push_back(name);
        }
        std::sort(segMemeNamesSorted.begin(), segMemeNamesSorted.end());
        std::vector<Element> segMemeList;
        segMemeList.reserve(segMemeNamesSorted.size());
        for (auto &name: segMemeNamesSorted) {
          segMemeList.push_back(text(name) | bold);
        }

        auto segCol = vbox({
                               hbox({
                                        separatorEmpty(),
                                        vbox({
                                                 text("[" + std::to_string(segment->id) + "]") | color(Color::GrayDark),
                                                 text(formatTimeFromMicros(segment->beginAtChainMicros)) |
                                                 bold,
                                             }),
                                        separatorEmpty(),
                                        separatorEmpty(),
                                        vbox({
                                                 text(formatPositionBarBeats(*segment, segment->delta)) |
                                                 color(Color::GrayDark),
                                                 text(Segment::toString(segment->type)) | bold,
                                             }),
                                        separatorEmpty(),
                                    }),
                               separatorEmpty(),
                               hbox({
                                        separatorEmpty(),
                                        vbox({
                                                 text(formatTotalBars(*segment, segment->total)) |
                                                 color(Color::GrayDark),
                                                 text(formatTimeFromMicros(
                                                     segment->durationMicros.value())) |
                                                 bold,
                                             }),
                                        separatorEmpty(),
                                        separatorEmpty(),
                                        vbox({
                                                 text("Intensity") | color(Color::GrayDark),
                                                 text(formatDecimal(segment->intensity, 2)) | bold,
                                             }),
                                        separatorEmpty(),
                                        separatorEmpty(),
                                        vbox({
                                                 text("Tempo") | color(Color::GrayDark),
                                                 text(formatMinDecimal(segment->tempo)) | bold,
                                             }),
                                        separatorEmpty(),
                                        separatorEmpty(),
                                        vbox({
                                                 text("Key") | color(Color::GrayDark),
                                                 text(segment->key) | bold,
                                             }),
                                        separatorEmpty(),
                                    }),
                               separatorEmpty(),
                               hbox({
                                        separatorEmpty(),
                                        vbox({
                                                 text("Memes") | color(Color::GrayDark),
                                                 vbox(segMemeList),
                                             }),
                                        separatorEmpty(),
                                    }),
                               separatorEmpty(),
                               hbox({
                                        separatorEmpty(),
                                        computeSegmentChoicesNode(segment),
                                        separatorEmpty(),
                                    }),
                           });
        segments.push_back(segCol);
        segments.push_back(separatorLight());
      }
    }
    return hbox(segments) | flex;
  });

  ui_tab_content_audio = Renderer([this] {
    std::vector<std::vector<std::string>> tableContents;
    tableContents.emplace_back(std::vector<std::string>{"Audio Name", "Start At", "Stop At"});
    for (const auto &audio: ActiveAudios) {
      std::vector<std::string> row = {
          audio.getAudio()->name,
          formatMicrosAsFloatingPointSeconds(audio.getStartAtChainMicros()),
          audio.getStopAtChainMicros().has_value() ? formatMicrosAsFloatingPointSeconds(
              audio.getStopAtChainMicros().value())
                                                   : "-",
      };
      tableContents.emplace_back(row);
    }

    auto table = Table(tableContents);

    table.SelectAll().Border(LIGHT);

    // Add border around the first column.
    table.SelectColumn(0).Border(LIGHT);

    // Make first row bold with a double border.
    table.SelectRow(0).Decorate(bold);
    table.SelectRow(0).SeparatorVertical(LIGHT);
    table.SelectRow(0).Border(DOUBLE);

    // Align right the "Release date" column.
    table.SelectColumn(2).DecorateCells(align_right);

    return hbox({
                    separatorEmpty(),
                    table.Render(),
                    separatorEmpty(),
                });
  });

  ui_header_elapsed_time = Renderer([this] {
    return text("Time Elapsed: " + formatTimeFromMicros(AtChainMicros)) | color(Color::Green);
  });

  ui_tab_content_content = Renderer([this] {
    std::vector<Element> lines;
    for (const std::string &line: StringUtils::split(engine->getTemplateContent()->toString(), '\n')) {
      lines.push_back(text(line));
    }
    return hbox({
                    separatorEmpty(),
                    vbox(lines),
                    separatorEmpty(),
                });
  });

  ui_tab_selected = 0;
  ui_tab_toggle = Toggle(&ui_tab_values, &ui_tab_selected);
  ui_tab_container = Container::Tab(
      {
          ui_tab_content_segments,
          ui_tab_content_audio,
          ui_tab_content_template,
          ui_tab_content_content,
      },
      &ui_tab_selected);

  ui_container = Container::Vertical({
                                         ui_tab_toggle,
                                         ui_tab_container,
                                     });

  ui_document = Renderer(ui_container, [&] {
    return vbox({
                    hbox({
                             separatorEmpty(),
                             ui_header_elapsed_time->Render(),
                             separatorEmpty(),
                             separator(),
                             separatorEmpty(),
                             text(engine->getTemplateContent()->getFirstTemplate().value()->name) | bold,
                             separatorEmpty(),
                             separator(),
                             separatorEmpty(),
                             text(Fabricator::toString(engine->getSettings().controlMode) + " control mode") |
                             color(Color::GrayLight),
                             separatorEmpty(),
                             separator(),
                             separatorEmpty(),
                             text("Dub ahead " + std::to_string(engine->getSettings().dubAheadSeconds) + "s") |
                             color(Color::GrayLight),
                             separatorEmpty(),
                             separator(),
                             separatorEmpty(),
                             text("Craft ahead " + std::to_string(engine->getSettings().craftAheadSeconds) + "s") |
                             color(Color::GrayLight),
                             separatorEmpty(),
                             separator(),
                             filler(),
                             separator(),
                             ui_tab_toggle->Render(),
                         }),
                    separator(),
                    ui_tab_container->Render(),
                }) |
           border;
  });

  return ui_document;
}

std::string EngineUiBase::formatMicrosAsFloatingPointSeconds(unsigned long long int micros, int precision) {
  std::stringstream ss;
  ss << std::fixed << std::setprecision(precision) << static_cast<float>(micros) / 1000000;
  return ss.str() + "s";
}

std::string EngineUiBase::formatTimeFromMicros(unsigned long long int micros) {
  // Check for null equivalent in C++ (microseconds being 0 might be considered as "null" equivalent)
  if (micros == 0) {
    return "0s";
  }

  // Round up to the nearest second
  unsigned long long totalSeconds = (micros + 999999) / 1000000;

  // Get fractional seconds
  float fractionalSeconds = static_cast<float>(micros % 1000000) / 1000000.0f;

  // Calculate hours, minutes, and remaining seconds
  unsigned long long hours = totalSeconds / 3600;
  unsigned long long remainingSeconds = totalSeconds % 3600;
  unsigned long long minutes = remainingSeconds / 60;
  unsigned long long seconds = remainingSeconds % 60;

  // Build the readable string
  std::stringstream readableTime;
  if (hours > 0) {
    readableTime << hours << "h";
  }
  if (minutes > 0) {
    readableTime << minutes << "m";
  }
  if (seconds > 0 || (hours == 0 && minutes == 0)) {
    if (hours == 0 && minutes == 0) {
      readableTime << seconds << "." << static_cast<int>(floor(fractionalSeconds * 10)) << "s";
    } else {
      readableTime << seconds << "s";
    }
  }

  return readableTime.str();
}

std::string EngineUiBase::formatTotalBars(const Segment &segment, std::optional<int> beats) const {
  if (!beats.has_value()) return "N/A";
  auto barBeats = getBarBeats(segment);
  if (!barBeats.has_value()) return "N/A";
  return std::to_string(beats.value() / barBeats.value()) +
         formatFractionalSuffix(static_cast<double>(beats.value() % barBeats.value()) / barBeats.value()) + " " +
         (beats.value() == 1 ? "bar" : "bars");
}

std::string EngineUiBase::formatPositionBarBeats(const Segment &segment, double position) const {
  if (isnan(position)) {// Assuming position is a pointer or a nullable type in the original context
    return "N/A";
  }

  auto barBeatsOpt = getBarBeats(segment);
  if (barBeatsOpt.has_value()) {
    int barBeats = barBeatsOpt.value();
    int bars = floor(position / barBeats);
    int beats = floor(fmod(position, barBeats));
    double remaining = beats > 0 ? fmod(fmod(position, barBeats), beats) : 0;

    std::ostringstream ss;
    ss << (bars + 1) << '.' << (beats + 1) << formatDecimalSuffix(remaining);
    return ss.str();
  } else {
    return formatMinDecimal(position);// Assuming this function is implemented to handle double to string conversion
  }
}

std::string EngineUiBase::formatDecimal(const double value, const int precision) {
  std::stringstream ss;
  ss << std::fixed << std::setprecision(precision) << value;
  return ss.str();
}

std::string EngineUiBase::formatDecimalSuffix(double value) {
  if (value <= 0 || value >= 1) {
    return "";
  }
  return formatMinDecimal(value).substr(1);
}

std::string EngineUiBase::formatFractionalSuffix(const double value) {
  if (value <= 0 || value >= 1) {
    return "";
  }

  switch (static_cast<int>(value * 100)) {
    case 10:
      return "⅒";
    case 11:
      return "⅑";
    case 12:
      return "⅛";
    case 14:
      return "⅐";
    case 16:
      return "⅙";
    case 20:
      return "⅕";
    case 25:
      return "¼";
    case 33:
      return "⅓";
    case 37:
      return "⅜";
    case 40:
      return "⅖";
    case 50:
      return "½";
    case 60:
      return "⅗";
    case 62:
      return "⅝";
    case 66:
      return "⅔";
    case 75:
      return "¾";
    case 80:
      return "⅘";
    case 83:
      return "⅚";
    case 87:
      return "⅞";
    default:
      return formatMinDecimal(value).substr(1);
  }
}

std::string EngineUiBase::formatMinDecimal(const double number) {
  if (isnan(number)) {
    return "N/A";
  }
  if (floor(number) == number) {
    std::stringstream ss;
    ss << std::fixed << std::setprecision(0) << number;
    return ss.str();
  } else {
    std::stringstream ss;
    ss << std::fixed << std::setprecision(3) << number;
    std::string str = ss.str();

    // Remove trailing zeros
    str.erase(str.find_last_not_of('0') + 1, std::string::npos);

    // Remove trailing decimal point if any
    if (str.back() == '.') {
      str.pop_back();
    }

    return str;
  }
}

/**
 Get the bar beats for the given segment.

 @param segment for which to get the bar beats
 @return bar beats, else empty
 */
std::optional<int> EngineUiBase::getBarBeats(const Segment &segment) const {
  try {
    auto choice = engine->getSegmentStore()->readChoice(segment.id, Program::Main);
    if (!choice.has_value()) {
      // Failed to retrieve main program choice to determine beats for Segment
      return std::nullopt;
    }

    auto program = engine->getProjectContent()->getProgram(choice.value()->programId);
    if (!program.has_value()) {
      // "Failed to retrieve main program to determine beats for Segment
      return std::nullopt;
    }

    return program.value()->config.barBeats;

  } catch (...) {
    // Failed to format beats duration for Segment
    return std::nullopt;
  }
}

std::shared_ptr<Node> EngineUiBase::computeSegmentChoicesNode(const Segment *&pSegment) const {
  std::vector<Element> col;
  col.push_back(text("Choices") | color(Color::GrayDark));
  std::vector<const SegmentChoice *> macroChoices;
  std::vector<const SegmentChoice *> mainChoices;
  std::vector<const SegmentChoice *> beatChoices;
  std::vector<const SegmentChoice *> detailChoices;
  for (const SegmentChoice *choice: engine->getSegmentStore()->readAllSegmentChoices(pSegment->id)) {
    if (!choice->programId.empty() && Program::Type::Macro == choice->programType) {
      macroChoices.push_back(choice);
    } else if (!choice->programId.empty() && Program::Type::Main == choice->programType) {
      mainChoices.push_back(choice);
    } else if ((!choice->programId.empty() && Program::Type::Beat == choice->programType) ||
               Instrument::Type::Drum == choice->instrumentType) {
      beatChoices.push_back(choice);
    } else {
      detailChoices.push_back(choice);
    }
  }
  std::sort(macroChoices.begin(), macroChoices.end());
  col.push_back(computeSegmentChoicesNode(macroChoices));
  std::sort(mainChoices.begin(), mainChoices.end());
  col.push_back(computeSegmentChoicesNode(mainChoices));
  std::sort(beatChoices.begin(), beatChoices.end());
  col.push_back(computeSegmentChoicesNode(beatChoices));
  std::sort(detailChoices.begin(), detailChoices.end());
  col.push_back(computeSegmentChoicesNode(detailChoices));
  return vbox(col);
}

std::shared_ptr<Node>
EngineUiBase::computeSegmentChoicesNode(const std::vector<const SegmentChoice *> &segmentChoices) const {
  std::vector<Element> col;
  for (const SegmentChoice *choice: segmentChoices) {
    std::optional<const Instrument *> instrument = engine->getProjectContent()->getInstrument(choice->instrumentId);
    std::optional<const Program *> program = engine->getProjectContent()->getProgram(choice->programId);
    std::vector<Element> row;
    if (instrument.has_value()) {
      col.push_back(hbox({text(
          "[" + Instrument::toString(instrument.value()->type) +
          Instrument::toString(instrument.value()->mode) + "]"),
                          separatorEmpty(),
                          text(instrument.value()->name)}));
      col.push_back(computeSegmentPicksNode(choice));
    } else if (program.has_value()) {
      col.push_back(hbox({text(
          "[" + Program::toString(program.value()->type) + "]"),
                          separatorEmpty(),
                          text(program.value()->name)}));
    }
  }
  return vbox(col);
}

std::shared_ptr<Node> EngineUiBase::computeSegmentPicksNode(const SegmentChoice *&pSegmentChoice) const {
  std::vector<Element> col;
  for (const SegmentChoiceArrangementPick *pick: engine->getSegmentStore()->readAllSegmentChoiceArrangementPicks(
      pSegmentChoice)) {
    std::optional<const InstrumentAudio *> audio = engine->getProjectContent()->getInstrumentAudio(
        pick->instrumentAudioId);
    if (!audio.has_value()) continue;
    std::vector<Element> row;
    col.push_back(hbox({separatorEmpty(),
                        separatorEmpty(),
                        text("[" + formatTimeFromMicros(pick->startAtSegmentMicros) + "]") | color(Color::GrayDark),
                        separatorEmpty(),
                        text(audio.value()->name) | color(Color::GrayLight)}));
  }
  return vbox(col);
}
