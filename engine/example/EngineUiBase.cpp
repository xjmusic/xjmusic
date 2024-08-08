// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <iomanip>

#include <SDL2/SDL.h>
#include <ftxui/component/component.hpp>
#include <ftxui/component/screen_interactive.hpp>
#include <ftxui/dom/table.hpp>

#include "EngineUiBase.h"
#include "xjmusic/util/ValueUtils.h"

// This is a helper function to create a button with a custom style.
// The style is defined by a lambda function that takes an EntryState and
// returns an Element.
// We are using `center` to center the text inside the button, then `border` to
// add a border around the button, and finally `flex` to make the button fill
// the available space.
ButtonOption ButtonStyle()
{
	auto option = ButtonOption::Animated();
	option.transform = [](const EntryState& s) {
		auto element = text(s.label);
		if (s.focused)
		{
			element |= bold;
		}
		return element | center | borderEmpty | flex;
	};
	return option;
}

EngineUiBase::EngineUiBase(
	const std::string&		pathToProjectFile,
	Fabricator::ControlMode controlMode,
	std::optional<int>		craftAheadSeconds,
	std::optional<int>		dubAheadSeconds,
	std::optional<int>		deadlineSeconds,
	std::optional<int>		persistenceWindowSeconds)
	: engine(std::make_unique<Engine>(pathToProjectFile,
		  controlMode,
		  craftAheadSeconds,
		  dubAheadSeconds,
		  deadlineSeconds,
		  persistenceWindowSeconds))
	, screen(ScreenInteractive::Fullscreen())
{
	AtChainMicros = 0;
	ui_tab_selected = 0;
}

const Template* EngineUiBase::SelectTemplate()
{
	using namespace ftxui;

	std::vector<const Template*> AllTemplates;
	for (const Template* Template : engine->getProjectContent()->getTemplates())
	{
		AllTemplates.push_back(Template);
	}
	std::sort(AllTemplates.begin(), AllTemplates.end(), [](const Template* a, const Template* b) {
		return a->name < b->name;
	});

	// Extract template names
	std::vector<std::string> templateNames;
	templateNames.reserve(AllTemplates.size());
	for (const auto& tmpl : AllTemplates)
	{
		templateNames.push_back(tmpl->name);
	}

	int		   selected = 0;
	MenuOption option;
	option.on_enter = screen.ExitLoopClosure();

	auto	   header_text = Renderer([] {
		  return text("Select a template and press ENTER") | bold;
	  });
	auto	   template_menu = Menu(&templateNames, &selected, option);
	const auto container = Container::Vertical({
		header_text,
		template_menu,
	});

	const auto document = Renderer(container, [&] {
		return vbox({
				   header_text->Render(),
				   separator(),
				   template_menu->Render(),
			   })
			| border;
	});

	screen.Clear();
	screen.Loop(document);

	// Return the selected template
	if (selected >= 0 && selected < AllTemplates.size())
	{
		return AllTemplates[selected];
	}
	else
	{
		throw std::invalid_argument("Invalid template index");
	}
}

std::shared_ptr<ComponentBase> EngineUiBase::BuildRunningUI()
{
	memeTaxonomy = engine->getMemeTaxonomy()->toMap();
	for (const auto& category : memeTaxonomy)
	{
		memeTaxonomySelection[category.first] = 0;
		std::vector<Element> memeList;
		memeList.reserve(category.second.size());
		for (const auto& meme : category.second)
		{
			memeList.push_back(text(meme));
		}
		if (!memeTaxonomyCategories.empty())
		{
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
			Container::Vertical({ Renderer([] {
									 return hbox(
										 { filler(),
											 text(
												 "Meme Taxonomy")
												 | bold,
											 filler() });
								 }),
				Renderer(
					[] { return separator(); }),
				Container::Horizontal(
					memeTaxonomyCategories),
				Renderer(
					[] { return separator(); }),
				Button("Engage", [this] { engageMemeOverride(); }, ButtonStyle())

			}) | border,
		}),
		Renderer([] { return separatorEmpty(); }),
	});

	ui_tab_content_timeline = Renderer([this] {
		std::vector<Element> segments;
		for (auto& segment : engine->getSegmentStore()->readAllSegments())
		{
			if (segment->durationMicros.has_value() && segment->beginAtChainMicros + segment->durationMicros.value() > AtChainMicros)
			{
				segments.push_back(computeSegmentNode(segment));
				segments.push_back(separator() | color(Color::GrayDark));
			}
		}
		return hbox(segments) | flex | yframe;
	});

	ui_tab_content_audio = Renderer([this] {
		return computeAudioMixerNode();
	});

	ResizableSplitOption resizableSplitOption;
	resizableSplitOption.main = ui_tab_content_audio;
	resizableSplitOption.back = ui_tab_content_timeline;
	resizableSplitOption.direction = Direction::Right;
	resizableSplitOption.main_size = &ui_tab_content_engine_right_size;
	resizableSplitOption.separator_func = [] { return separatorDouble(); };
	ui_tab_content_engine = ResizableSplit(resizableSplitOption) | flex;

	ui_header_elapsed_time = Renderer([this] {
		return text("Time Elapsed: " + formatTimeFromMicros(AtChainMicros)) | color(Color::Green);
	});

	ui_tab_content_content = Renderer([this] {
		std::vector<Element> lines;
		for (const std::string& line : StringUtils::split(engine->getTemplateContent()->toString(), '\n'))
		{
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
			ui_tab_content_engine,
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
					   text(Fabricator::toString(engine->getSettings().controlMode) + " control mode") | color(Color::GrayLight),
					   separatorEmpty(),
					   separator(),
					   separatorEmpty(),
					   text("Deadline " + formatTimeFromMicros(engine->getSettings().deadlineMicros)) | color(Color::GrayLight),
					   separatorEmpty(),
					   separator(),
					   separatorEmpty(),
					   text("Dub ahead " + formatTimeFromMicros(engine->getSettings().dubAheadMicros)) | color(Color::GrayLight),
					   separatorEmpty(),
					   separator(),
					   separatorEmpty(),
					   text("Craft ahead " + formatTimeFromMicros(engine->getSettings().craftAheadMicros)) | color(Color::GrayLight),
					   separatorEmpty(),
					   separator(),
					   filler(),
					   separator(),
					   ui_tab_toggle->Render(),
				   }),
				   separator(),
				   ui_tab_container->Render(),
			   })
			| border;
	});

	return ui_document;
}

std::string EngineUiBase::formatMicrosAsFloatingPointSeconds(unsigned long long int micros, int precision)
{
	std::stringstream ss;
	ss << std::fixed << std::setprecision(precision) << static_cast<float>(micros) / ValueUtils::MICROS_PER_SECOND_FLOAT;
	return ss.str() + "s";
}

std::string EngineUiBase::formatTimeFromMicros(unsigned long long int micros)
{
	// Check for null equivalent in C++ (microseconds being 0 might be considered as "null" equivalent)
	if (micros == 0)
	{
		return "0s";
	}

	// Round down to the nearest second
	const unsigned long long totalSeconds = micros / ValueUtils::MICROS_PER_SECOND;

	// Get fractional seconds
	const float fractionalSeconds = static_cast<float>(micros % 1000000) / 1000000.0f;

	// Calculate hours, minutes, and remaining seconds
	const unsigned long long hours = totalSeconds / 3600;
	const unsigned long long remainingSeconds = totalSeconds % 3600;
	const unsigned long long minutes = remainingSeconds / 60;
	const unsigned long long seconds = remainingSeconds % 60;

	// Build the readable string
	std::stringstream readableTime;
	if (hours > 0)
	{
		readableTime << hours << "h";
	}
	if (minutes > 0)
	{
		readableTime << minutes << "m";
	}
	if (seconds > 0 || (hours == 0 && minutes == 0))
	{
		if (hours == 0 && minutes == 0)
		{
			readableTime << seconds << "." << static_cast<int>(floor(fractionalSeconds * 10)) << "s";
		}
		else
		{
			readableTime << seconds << "s";
		}
	}

	return readableTime.str();
}

std::string EngineUiBase::formatTotalBars(const Segment& segment, std::optional<int> beats) const
{
	if (!beats.has_value())
		return "N/A";
	const auto barBeats = getBarBeats(segment);
	if (!barBeats.has_value())
		return "N/A";
	return std::to_string(beats.value() / barBeats.value()) + formatFractionalSuffix(static_cast<double>(beats.value() % barBeats.value()) / barBeats.value()) + " " + (beats.value() == 1 ? "bar" : "bars");
}

std::string EngineUiBase::formatPositionBarBeats(const Segment& segment, double position) const
{
	if (isnan(position))
	{ // Assuming position is a pointer or a nullable type in the original context
		return "N/A";
	}

	const auto barBeatsOpt = getBarBeats(segment);
	if (barBeatsOpt.has_value())
	{
		const int	 barBeats = barBeatsOpt.value();
		const int	 bars = floor(position / barBeats);
		const int	 beats = floor(fmod(position, barBeats));
		const double remaining = beats > 0 ? fmod(fmod(position, barBeats), beats) : 0;

		std::ostringstream ss;
		ss << (bars + 1) << '.' << (beats + 1) << formatDecimalSuffix(remaining);
		return ss.str();
	}
	else
	{
		return formatMinDecimal(position); // Assuming this function is implemented to handle double to string conversion
	}
}

std::string EngineUiBase::formatDecimal(const double value, const int precision)
{
	std::stringstream ss;
	ss << std::fixed << std::setprecision(precision) << value;
	return ss.str();
}

std::string EngineUiBase::formatDecimalSuffix(double value)
{
	if (value <= 0 || value >= 1)
	{
		return "";
	}
	return formatMinDecimal(value).substr(1);
}

std::string EngineUiBase::formatFractionalSuffix(const double value)
{
	if (value <= 0 || value >= 1)
	{
		return "";
	}

	switch (static_cast<int>(value * 100))
	{
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

std::string EngineUiBase::formatMinDecimal(const double number)
{
	if (isnan(number))
	{
		return "N/A";
	}
	if (floor(number) == number)
	{
		std::stringstream ss;
		ss << std::fixed << std::setprecision(0) << number;
		return ss.str();
	}
	else
	{
		std::stringstream ss;
		ss << std::fixed << std::setprecision(3) << number;
		std::string str = ss.str();

		// Remove trailing zeros
		str.erase(str.find_last_not_of('0') + 1, std::string::npos);

		// Remove trailing decimal point if any
		if (str.back() == '.')
		{
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
std::optional<int> EngineUiBase::getBarBeats(const Segment& segment) const
{
	try
	{
		const auto choice = engine->getSegmentStore()->readChoice(segment.id, Program::Main);
		if (!choice.has_value())
		{
			// Failed to retrieve main program choice to determine beats for Segment
			return std::nullopt;
		}

		const auto program = engine->getProjectContent()->getProgram(choice.value()->programId);
		if (!program.has_value())
		{
			// "Failed to retrieve main program to determine beats for Segment
			return std::nullopt;
		}

		return program.value()->config.barBeats;
	}
	catch (...)
	{
		// Failed to format beats duration for Segment
		return std::nullopt;
	}
}

Element EngineUiBase::computeSegmentNode(const Segment*& pSegment) const
{
	std::set<std::string> segMemeNames;
	for (auto& meme : engine->getSegmentStore()->readAllSegmentMemes(pSegment->id))
	{
		segMemeNames.emplace(meme->name);
	}
	std::vector<std::string> segMemeNamesSorted;
	segMemeNamesSorted.reserve(segMemeNames.size());
	for (auto& name : segMemeNames)
	{
		segMemeNamesSorted.push_back(name);
	}
	std::sort(segMemeNamesSorted.begin(), segMemeNamesSorted.end());
	std::vector<Element> segMemeList
		;
	segMemeList.reserve(segMemeNamesSorted.size());
	for (const auto& name : segMemeNamesSorted)
	{
		segMemeList.push_back(text(name) | bold);
	}

	auto segCol = vbox({
		hbox({
			separatorEmpty(),
			vbox({
				text("[" + std::to_string(pSegment->id) + "]") | color(Color::GrayDark),
				text(formatTimeFromMicros(pSegment->beginAtChainMicros)) | bold,
			}),
			separatorEmpty(),
			separatorEmpty(),
			vbox({
				text(formatPositionBarBeats(*pSegment, pSegment->delta)) | color(Color::GrayDark),
				text(Segment::toString(pSegment->type)) | bold,
			}),
			separatorEmpty(),
		}),
		separatorEmpty(),
		hbox({
			separatorEmpty(),
			vbox({
				text(formatTotalBars(*pSegment, pSegment->total)) | color(Color::GrayDark),
				text(formatTimeFromMicros(
					pSegment->durationMicros.value()))
					| bold,
			}),
			separatorEmpty(),
			separatorEmpty(),
			vbox({
				text("Intensity") | color(Color::GrayDark),
				text(formatDecimal(pSegment->intensity, 2)) | bold,
			}),
			separatorEmpty(),
			separatorEmpty(),
			vbox({
				text("Tempo") | color(Color::GrayDark),
				text(formatMinDecimal(pSegment->tempo)) | bold,
			}),
			separatorEmpty(),
			separatorEmpty(),
			vbox({
				text("Key") | color(Color::GrayDark),
				text(pSegment->key) | bold,
			}),
			separatorEmpty(),
		}),
		separatorEmpty(),
		hbox({
			separatorEmpty(),
			vbox({
				text("Memes") | color(Color::GrayDark),
				vbox(segMemeList) | size(HEIGHT, EQUAL, static_cast<int>(segMemeList.size())),
			}),
			separatorEmpty(),
		}),
		separatorEmpty(),
		hbox({
			separatorEmpty(),
			computeSegmentChoicesNode(pSegment),
			separatorEmpty(),
		}),
	});
	return segCol;
}

Element EngineUiBase::computeAudioMixerNode()
{
	std::vector<std::vector<Element>> tableContents;
	tableContents.emplace_back(std::vector{ text("Start"), text("Stop"), text("Audio") });
	for (const auto& [audioId, audio] : ActiveAudios)
	{
		const unsigned long long dubbedToMicros = AtChainMicros + engine->getSettings().dubAheadMicros;
		const bool				 bDubbed = dubbedToMicros >= audio.getStartAtChainMicros() && dubbedToMicros <= audio.getStopAtChainMicros();
		const bool				 bActive = AtChainMicros >= audio.getStartAtChainMicros() && AtChainMicros <= audio.getStopAtChainMicros();
		const Color				 audioColor = bActive ? Color::GreenLight : (bDubbed ? Color::Green : Color::GrayDark);
		std::vector				 row = {
			 text(formatTimeFromMicros(audio.getStartAtChainMicros())),
			 text(formatTimeFromMicros(audio.getStopAtChainMicros())),
			 text(audio.getAudio()->name) | color(audioColor),
		};
		tableContents.emplace_back(row);
	}

	auto table = Table(tableContents);
	table.SelectRow(0).Decorate(color(Color::GrayDark));
	table.SelectAll().SeparatorVertical(EMPTY);

	// Make first row bold with a double border.
	return hbox({
			   separatorEmpty(),
			   table.Render(),
			   separatorEmpty(),
		   })
		| flex | xframe | yframe;
}

void EngineUiBase::engageMemeOverride()
{
	std::set<std::string> memes;
	for (const auto& [tCategory, tMemes] : memeTaxonomy)
	{
		if (memeTaxonomySelection[tCategory] >= 0 && memeTaxonomySelection[tCategory] < tMemes.size())
		{
			memes.insert(tMemes[memeTaxonomySelection[tCategory]]);
		}
	}
	engine->doOverrideMemes(memes);
	// switch to the Engine tab
	ui_tab_selected = 0;
}

std::shared_ptr<Node> EngineUiBase::computeSegmentChoicesNode(const Segment*& pSegment) const
{
	std::vector<Element> col;
	col.push_back(text("Choices") | color(Color::GrayDark));
	std::vector<const SegmentChoice*> macroChoices;
	std::vector<const SegmentChoice*> mainChoices;
	std::vector<const SegmentChoice*> beatChoices;
	std::vector<const SegmentChoice*> detailChoices;
	const long long					  AtSegmentMicros = AtChainMicros - pSegment->beginAtChainMicros;
	for (const SegmentChoice* choice : engine->getSegmentStore()->readAllSegmentChoices(pSegment->id))
	{
		if (!choice->programId.empty() && Program::Type::Macro == choice->programType)
		{
			macroChoices.push_back(choice);
		}
		else if (!choice->programId.empty() && Program::Type::Main == choice->programType)
		{
			mainChoices.push_back(choice);
		}
		else if ((!choice->programId.empty() && Program::Type::Beat == choice->programType) || Instrument::Type::Drum == choice->instrumentType)
		{
			beatChoices.push_back(choice);
		}
		else
		{
			detailChoices.push_back(choice);
		}
	}
	std::sort(macroChoices.begin(), macroChoices.end());
	col.push_back(computeSegmentChoicesNode(macroChoices, AtSegmentMicros));
	std::sort(mainChoices.begin(), mainChoices.end());
	col.push_back(computeSegmentChoicesNode(mainChoices, AtSegmentMicros));
	std::sort(beatChoices.begin(), beatChoices.end());
	col.push_back(computeSegmentChoicesNode(beatChoices, AtSegmentMicros));
	std::sort(detailChoices.begin(), detailChoices.end());
	col.push_back(computeSegmentChoicesNode(detailChoices, AtSegmentMicros));
	return vbox(col);
}

std::shared_ptr<Node>
EngineUiBase::computeSegmentChoicesNode(const std::vector<const SegmentChoice*>& segmentChoices,
	long long																	 AtSegmentMicros) const
{
	std::vector<Element> col;
	for (const SegmentChoice* choice : segmentChoices)
	{
		std::optional<const Instrument*> instrument = engine->getProjectContent()->getInstrument(choice->instrumentId);
		std::optional<const Program*>	 program = engine->getProjectContent()->getProgram(choice->programId);
		std::vector<Element>			 row;
		if (instrument.has_value())
		{
			col.push_back(hbox({ text(
									 "[" + Instrument::toString(instrument.value()->type) + Instrument::toString(instrument.value()->mode) + "]"),
				separatorEmpty(),
				text(instrument.value()->name) }));
			col.push_back(computeSegmentPicksNode(choice, AtSegmentMicros));
		}
		else if (program.has_value())
		{
			col.push_back(hbox({ text(
									 "[" + Program::toString(program.value()->type) + "]"),
				separatorEmpty(),
				text(program.value()->name) }));
		}
	}
	return vbox(col);
}

std::shared_ptr<Node>
EngineUiBase::computeSegmentPicksNode(const SegmentChoice*& pSegmentChoice, long long AtSegmentMicros) const
{
	std::vector<Element> col;
	for (const SegmentChoiceArrangementPick* pick : engine->getSegmentStore()->readAllSegmentChoiceArrangementPicks(
			 pSegmentChoice))
	{
		std::optional<const InstrumentAudio*> audio = engine->getProjectContent()->getInstrumentAudio(pick->instrumentAudioId);
		if (!audio.has_value())
			continue;
		const unsigned long long audioTransientMicros = audio.value()->transientSeconds * MICROS_PER_SECOND;
		const unsigned long long audioLengthMicros = audio.value()->lengthSeconds * MICROS_PER_SECOND;
		const unsigned long long thresholdActiveFrom = pick->startAtSegmentMicros - audioTransientMicros;
		const unsigned long long thresholdActiveTo = pick->lengthMicros == 0 ? pick->startAtSegmentMicros - audioTransientMicros + audioLengthMicros : pick->startAtSegmentMicros + pick->lengthMicros;
		const unsigned long long dubbedToMicros = AtSegmentMicros + engine->getSettings().dubAheadMicros;
		const bool				 bDubbed = dubbedToMicros >= thresholdActiveFrom && dubbedToMicros <= thresholdActiveTo;
		const bool				 bActive = AtSegmentMicros >= thresholdActiveFrom && AtSegmentMicros <= thresholdActiveTo;
		std::vector<Element>	 row;
		const Color				 audioColor = pSegmentChoice->mute ? Color::Yellow : (bActive ? Color::GreenLight : (bDubbed ? Color::Green : Color::GrayDark));
		col.push_back(hbox({ separatorEmpty(),
			separatorEmpty(),
			text("[" + formatTimeFromMicros(pick->startAtSegmentMicros) + "]") | color(audioColor),
			separatorEmpty(),
			text(audio.value()->name) | color(audioColor)}));
  }
  return vbox(col);
}
