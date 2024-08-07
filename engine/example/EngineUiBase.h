// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_ENGINE_UI_BASE_H
#define XJMUSIC_ENGINE_UI_BASE_H

#include <memory>

#include <SDL2/SDL.h>
#include <ftxui/component/screen_interactive.hpp>
#include <ftxui/dom/elements.hpp>

#include "xjmusic/Engine.h"

#include "EngineUiBase.h"
#include "EngineScheduledAudio.h"

using namespace XJ;
using namespace ftxui;

class EngineUiBase
{
protected:
	const Uint32					   CYCLE_MILLISECONDS = 382;
	std::unique_ptr<Engine>			   engine;
	ScreenInteractive				   screen;
	std::map<std::string, EngineScheduledAudio> Schedule;
	Component						   ui_container;
	Component						   ui_document;
	Component						   ui_header_elapsed_time;
	Component						   ui_tab_container;
	Component						   ui_tab_content_audio;
	Component						   ui_tab_content_content;
	Component						   ui_tab_content_engine;
	int								   ui_tab_content_engine_right_size = 50;
	Component						   ui_tab_content_template;
	Component						   ui_tab_content_timeline;
	Component						   ui_tab_toggle;
	std::vector<std::string>		   ui_tab_values{
		  "Engine",
		  "Template",
		  "Content",
	};
	int												ui_tab_selected;
	std::vector<Component>							memeTaxonomyCategories;
	std::map<std::string, std::vector<std::string>> memeTaxonomy;
	std::map<std::string, int>						memeTaxonomySelection;

	/**
	 * The running state of the XJPlayer.
	 */
	bool running = true;

	/**
	 * The elapsed time in milliseconds since starting playback.
	 */
	Uint32 AtChainMicros{};

	/**
	 * Selects a template from the Engine.
	 * @return  The selected template.
	 */
	const Template* SelectTemplate();

	/**
	 * Shows the running UI.
	 * This should be run on a separate thread.
	 */
	std::shared_ptr<ComponentBase> BuildRunningUI();

	/**
	 * Formats the given micros into a human-readable string of thousands of a second, i.e. 24.573s
	 * @param micros
	 * @return
	 */
	static std::string formatMicrosAsFloatingPointSeconds(unsigned long long int micros, int precision = 3);

	/**
	 * Formats the given micros into a human-readable string of minutes and seconds like 23m12s
	 * @param micros
	 * @return
	 */
	static std::string formatTimeFromMicros(unsigned long long int micros);

	/**
	 * Format the total bars of a segment
	 * @param segment  The segment to format
	 * @param beats  The number of beats in the segment
	 * @return
	 */
	std::string formatTotalBars(const Segment& segment, std::optional<int> beats) const;

	/**
	 * Format the position of a segment in bars and beats
	 * @param segment  The segment to format
	 * @param position  The position in the segment to format
	 * @return  The formatted position
	 */
	std::string formatPositionBarBeats(const Segment& segment, double position) const;

	/**
	 * Format the position of a segment in bars and beats
	 * @param value  The value to format
	 * @param precision  The number of decimal places to include
	 * @return  The formatted value
	 */
	static std::string formatDecimal(double value, int precision);

	/**
	 * Format the decimal suffix portion of a human-readable string
	 *
	 * @param value number to format
	 * @return human-readable string like ".5", ".25", or ".333".
	 */
	static std::string formatDecimalSuffix(double value);

	/**
	 * Format the suffix of a human-readable string with a number and fraction
	 *
	 * @param value number to format
	 * @return human-readable string like "½", "¼", or "⅔".
	 **/
	static std::string formatFractionalSuffix(double value);

	/**
	 * Format a number to a human-readable string
	 *
	 * @param value number to format
	 * @return human-readable string like "5", "4.5", or "1.27".
	 */
	static std::string formatMinDecimal(double value);

	/**
	 * Get the bar and beat of a segment
	 * @param segment  The segment to get the bar and beat of
	 * @return  The bar and beat of the segment
	 */
	std::optional<int> getBarBeats(const Segment& segment) const;

public:
	/**
	 * Construct a new App
	 * @param pathToProjectFile     path to the .xj project file from which to load content
	 * @param controlMode      the fabrication control mode
	 * @param craftAheadSeconds (optional) how many seconds ahead to craft
	 * @param dubAheadSeconds  (optional) how many seconds ahead to dub
	 * @param deadlineSeconds  (optional) audio scheduling deadline in seconds
	 * @param persistenceWindowSeconds (optional) how long to keep segments in memory
	 */
	explicit EngineUiBase(
		const std::string&		pathToProjectFile,
		Fabricator::ControlMode controlMode,
		std::optional<int>		craftAheadSeconds,
		std::optional<int>		dubAheadSeconds,
		std::optional<int>		deadlineSeconds,
		std::optional<int>		persistenceWindowSeconds);

	/**
	 * Compute the node to show all choices for a segment
	 * @param pSegment  for which to show choices
	 * @return  choices display node
	 */
	std::shared_ptr<Node> computeSegmentChoicesNode(const Segment*& pSegment) const;

	/**
	 * Compute the node to show all picks for a segment having the given program type
	 * @param segmentChoices  from which to filter and display
	 * @return  choices display node
	 */
	std::shared_ptr<Node> computeSegmentChoicesNode(const std::vector<const SegmentChoice*>& segmentChoices,
		long long																			 AtSegmentMicros) const;

	/**
	 * Compute the node to show all picks for the given segment choice
	 * @param pSegmentChoice for which to show picks
	 * @return  picks display node
	 */
	std::shared_ptr<Node> computeSegmentPicksNode(const SegmentChoice*& pSegmentChoice, long long AtSegmentMicros) const;

	/**
	 * Compute the node to display a segment
	 * @param pSegment  to display
	 * @return  segment display node
	 */
	Element computeSegmentNode(const Segment*& pSegment) const;

	/**
	 * Compute the node to display the audio mixer
	 * @return the  audio mixer display node
	 */
	Element computeAudioMixerNode();

	/**
	 * Engage the engine's meme override with the currently selected memes
	 */
	void engageMemeOverride();
};

#endif // XJMUSIC_ENGINE_UI_BASE_H
