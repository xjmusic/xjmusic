// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <algorithm>

#include "xjmusic/craft/BackgroundCraft.h"
#include "xjmusic/craft/BeatCraft.h"
#include "xjmusic/craft/DetailCraft.h"
#include "xjmusic/craft/MacroMainCraft.h"
#include "xjmusic/craft/TransitionCraft.h"
#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/util/CsvUtils.h"
#include "xjmusic/util/ValueUtils.h"
#include "xjmusic/work/CraftWork.h"

using namespace XJ;

CraftWork::CraftWork(
	SegmentEntityStore* store,
	ContentEntityStore* content,
	const int			persistenceWindowSeconds,
	const int			craftAheadSeconds,
	const int			deadlineSeconds)
{
	this->store = store;

	craftAheadMicros = craftAheadSeconds * ValueUtils::MICROS_PER_SECOND;
	deadlineMicros = deadlineSeconds * ValueUtils::MICROS_PER_SECOND;
	persistenceWindowMicros = persistenceWindowSeconds * ValueUtils::MICROS_PER_SECOND;
	this->content = content;
}

void CraftWork::start()
{
	// Create chain from template
	if (content->getTemplates().empty())
	{
		throw std::runtime_error("Cannot initialize CraftWork without Templates");
	}
	const auto tmpl = *content->getTemplates().begin();
	store->put(ChainUtils::fromTemplate(tmpl));
	running = true;
}

void CraftWork::finish()
{
	if (!running)
		return;
	running = false;
	std::cout << "CraftWork Finished" << std::endl;
}

TemplateConfig CraftWork::getTemplateConfig() const
{
	return store->readChain().value()->config;
}

std::vector<const Segment*>
CraftWork::getSegmentsIfReady(const unsigned long long fromChainMicros, const unsigned long long toChainMicros) const
{
	const auto currentSegments = store->readAllSegmentsSpanning(fromChainMicros, toChainMicros);
	if (currentSegments.empty())
	{
		return {};
	}
	auto						previousSegment = store->readSegment(currentSegments.at(0)->id - 1);
	auto						nextSegment = store->readSegment(currentSegments.at(currentSegments.size() - 1)->id + 1);
	std::vector<const Segment*> segments;
	if (previousSegment.has_value() && Segment::State::Crafted == previousSegment.value()->state)
		segments.emplace_back(previousSegment.value());
	for (auto segment : currentSegments)
		if (Segment::State::Crafted == segment->state)
			segments.emplace_back(segment);
	if (nextSegment.has_value() && Segment::State::Crafted == nextSegment.value()->state)
		segments.emplace_back(nextSegment.value());
	return segments;
}

std::optional<const Segment*> CraftWork::getSegmentAtChainMicros(const unsigned long long chainMicros) const
{
	// require current segment in crafted state
	const auto currentSegment = store->readSegmentAtChainMicros(chainMicros);
	if (!currentSegment.has_value() || currentSegment.value()->state != Segment::State::Crafted)
	{
		return std::nullopt;
	}
	return currentSegment;
}

std::optional<const Segment*> CraftWork::getSegmentAtOffset(const int offset) const
{
	// require current segment in crafted state
	const auto currentSegment = store->readSegment(offset);
	if (!currentSegment.has_value() || currentSegment.value()->state != Segment::State::Crafted)
	{
		return std::nullopt;
	}
	return currentSegment;
}

std::set<const SegmentChoiceArrangementPick*> CraftWork::getPicks(const std::vector<const Segment*>& segments) const
{
	return store->readAllSegmentChoiceArrangementPicks(segments);
}

const Instrument* CraftWork::getInstrument(const InstrumentAudio* audio) const
{
	const auto instrument = content->getInstrument(audio->instrumentId);
	if (!instrument.has_value())
	{
		throw std::runtime_error("Failed to get Instrument[" + audio->instrumentId + "]");
	}
	return instrument.value();
}

const InstrumentAudio* CraftWork::getInstrumentAudio(const SegmentChoiceArrangementPick* pick) const
{
	const auto audio = content->getInstrumentAudio(pick->instrumentAudioId);
	if (!audio.has_value())
	{
		throw std::runtime_error("Failed to get InstrumentAudio[" + pick->instrumentAudioId + "]");
	}
	return audio.value();
}

bool CraftWork::isFinished() const
{
	return !running;
}

std::optional<const Program*> CraftWork::getMainProgram(const Segment* segment) const
{
	const auto
		choices = store->readAllSegmentChoices(segment->id);
	for (const auto choice : choices)
	{
		if (Program::Type::Main == choice->programType)
		{
			return content->getProgram(choice->programId);
		}
	}
	return std::nullopt;
}

std::optional<const Program*> CraftWork::getMacroProgram(const Segment& segment) const
{
	const auto
		choices = store->readAllSegmentChoices(segment.id);
	for (const auto choice : choices)
	{
		if (Program::Type::Macro == choice->programType)
		{
			return content->getProgram(choice->programId);
		}
	}
	return std::nullopt;
}

ContentEntityStore* CraftWork::getSourceMaterial() const
{
	return content;
}

void CraftWork::runCycle(const unsigned long long int atChainMicros)
{
	if (!running)
		return;

	try
	{
		doFabrication(atChainMicros);
		doSegmentCleanup(atChainMicros);
	}
	catch (std::exception& e)
	{
		didFailWhile("running craft work", e);
	}
}

bool CraftWork::isReady() const
{
	return !nextCycleRewrite;
}

void CraftWork::doOverrideMacro(const Program* macroProgram)
{
	std::cout << "Next craft cycle, will override macro with " << macroProgram->name << std::endl;
	nextCycleOverrideMacroProgram = { macroProgram };
	doNextCycleRewriteUnlessInitialSegment();
}

void CraftWork::doOverrideMemes(std::set<std::string> memes)
{
	std::cout << "Next craft cycle, will override memes with "
			  << CsvUtils::toProperCsvAnd(std::vector(memes.begin(), memes.end())) << std::endl;
	nextCycleOverrideMemes = memes;
	doNextCycleRewriteUnlessInitialSegment();
}

bool CraftWork::getAndResetDidOverride()
{
	const auto previous = didOverride;
	didOverride = false;
	return previous;
}

void CraftWork::doNextCycleRewriteUnlessInitialSegment()
{
	if (0 < store->getSegmentCount())
		nextCycleRewrite = true;
}

void CraftWork::doFabrication(const unsigned long long int atChainMicros)
{
	if (nextCycleRewrite)
	{
		doFabricationRewrite(atChainMicros + deadlineMicros, nextCycleOverrideMacroProgram, nextCycleOverrideMemes);
		nextCycleRewrite = false;
	}
	else
	{
		doFabricationDefault(atChainMicros + craftAheadMicros, nextCycleOverrideMacroProgram, nextCycleOverrideMemes);
	}
}

void CraftWork::doFabricationDefault(
	const unsigned long long			toChainMicros,
	const std::optional<const Program*> overrideMacroProgram,
	const std::set<std::string>&		overrideMemes)
{
	try
	{
		// currently fabricated AT (vs target fabricated TO)
		const long	atChainMicros = ChainUtils::computeFabricatedToChainMicros(store->readAllSegments());
		const float aheadSeconds = atChainMicros > toChainMicros
			? (static_cast<float>(atChainMicros - toChainMicros) / ValueUtils::MICROS_PER_SECOND_FLOAT)
			: 0;
		if (aheadSeconds > 0)
			return;

		// Build next segment in chain
		// Get the last segment in the chain
		// If the chain had no last segment, it must be empty; return a template for its first segment
		Segment	   segment;
		const auto existing = store->readSegmentLast();
		if (!existing.has_value())
		{
			segment = buildSegmentInitial();
		}
		else if (!existing.value()->durationMicros.has_value())
		{
			// Last segment in chain has no duration, cannot fabricate next segment
			return;
		}
		else
		{
			segment = buildSegmentFollowing(existing.value());
		}
		doFabricationWork(store->put(segment), std::nullopt, overrideMacroProgram, overrideMemes);
	}
	catch (std::exception& e)
	{
		didFailWhile("fabricating", e);
	}
}

void CraftWork::doFabricationRewrite(
	const unsigned long long			toChainMicros,
	const std::optional<const Program*> overrideMacroProgram,
	std::set<std::string>				overrideMemes)
{
	try
	{
		// Determine the segment we are currently in the middle of dubbing
		const auto lastSegment = getSegmentAtChainMicros(toChainMicros);
		if (!lastSegment.has_value())
		{
			std::cerr << "Will not delete any segments because fabrication is already at the end of the known chain->"
					  << std::endl;
			return;
		}

		// Determine whether the current segment can be cut short
		const auto currentMainProgram = getMainProgram(lastSegment.value());
		if (!currentMainProgram.has_value())
		{
			std::cerr << "Will not delete any segments because current segment has no main program." << std::endl;
			return;
		}
		ProgramConfig mainProgramConfig;
		try
		{
			mainProgramConfig = ProgramConfig(currentMainProgram.value()->config);
		}
		catch (...)
		{
			throw FabricationException("Failed to get main program config");
		}
		const float subBeats = static_cast<float>(mainProgramConfig.barBeats * mainProgramConfig.cutoffMinimumBars);
		const auto	dubbedToSegmentMicros = toChainMicros - lastSegment.value()->beginAtChainMicros;
		const auto	microsPerBeat = static_cast<long>(ValueUtils::MICROS_PER_MINUTE_FLOAT / currentMainProgram.value()->tempo);
		const auto	dubbedToSegmentBeats = dubbedToSegmentMicros / microsPerBeat;
		const float cutoffAfterBeats = subBeats * ceil(static_cast<float>(dubbedToSegmentBeats) / subBeats);
		if (cutoffAfterBeats < static_cast<float>(lastSegment.value()->total))
		{
			doCutoffLastSegment(lastSegment.value(), cutoffAfterBeats);
		}

		// Delete all segments after the current segment and fabricate the next segment
		std::cout << "Will delete segments after #" << lastSegment.value()->id << " and re-fabricate." << std::endl;
		if (overrideMacroProgram.has_value())
			std::cout << "Has macro program override " << overrideMacroProgram.value()->name << std::endl;
		else if (!overrideMemes.empty())
			std::cout << "Has meme override "
					  << CsvUtils::toProperCsvAnd(std::vector(overrideMemes.begin(), overrideMemes.end()))
					  << std::endl;
		else
			std::cerr << "Neither override memes nor macros are present: unsure what rewrite action to take" << std::endl;

		store->deleteSegmentsAfter(lastSegment.value()->id);
		Segment followingSegment = buildSegmentFollowing(lastSegment.value());
		followingSegment.type = Segment::Type::NextMacro;
		doFabricationWork(store->put(followingSegment), Segment::Type::NextMacro, overrideMacroProgram, overrideMemes);
		didOverride = true;
	}
	catch (
		std::exception& e)
	{
		didFailWhile("fabricating", e);
	}
}

void CraftWork::doCutoffLastSegment(const Segment* inputSegment, float cutoffAfterBeats) const
{
	try
	{
		const long durationMicros = static_cast<long>(cutoffAfterBeats * ValueUtils::MICROS_PER_MINUTE_FLOAT / inputSegment->tempo);
		std::cout << "[seg-" << std::to_string(inputSegment->id) << "] Will cut current segment short after "
				  << cutoffAfterBeats << " beats." << std::endl;
		Segment updateSegment = *inputSegment;
		updateSegment.total = static_cast<int>(cutoffAfterBeats);
		updateSegment.durationMicros = static_cast<long>(durationMicros);
		store->updateSegment(updateSegment);
		for (const auto pick : store->readAllSegmentChoiceArrangementPicks(inputSegment->id))
		{
			try
			{
				if (pick->startAtSegmentMicros >= durationMicros)
					store->deleteSegmentChoiceArrangementPick(inputSegment->id, pick->id);
				else if (0 < pick->lengthMicros && pick->startAtSegmentMicros + pick->lengthMicros > durationMicros)
				{
					SegmentChoiceArrangementPick updatePick = *pick;
					updatePick.lengthMicros = durationMicros - pick->startAtSegmentMicros;
					store->put(updatePick);
				}
			}
			catch (std::exception& e)
			{
				std::cerr << "[seg-" << std::to_string(inputSegment->id) << "] "
						  << "Failed to cut SegmentChoiceArrangementPick[" << pick->id << "] short to " << cutoffAfterBeats
						  << " beats: " << e.what() << std::endl;
			}
		}
	}
	catch (std::exception& e)
	{
		throw FabricationException(
			"Failed to cut Segment[" + std::to_string(inputSegment->id) + "] short to " + std::to_string(cutoffAfterBeats) + " beats: " + e.what());
	}
}

void CraftWork::doFabricationWork(
	const Segment*						inputSegment,
	const std::optional<Segment::Type>	overrideSegmentType,
	const std::optional<const Program*> overrideMacroProgram,
	const std::set<std::string>&		overrideMemes) const
{
	// will prepare fabricator
	const auto retrospective = SegmentRetrospective(store, inputSegment->id);
	auto	   fabricator = Fabricator(content, store, &retrospective, inputSegment->id, overrideSegmentType);

	// will do craft work and fabricate the segment
	const Segment* updatedSegment = updateSegmentState(&fabricator, inputSegment, Segment::State::Planned,
		Segment::State::Crafting);
	MacroMainCraft(&fabricator, overrideMacroProgram, overrideMemes).doWork();
	BeatCraft(&fabricator).doWork();
	DetailCraft(&fabricator).doWork();
	TransitionCraft(&fabricator).doWork();
	BackgroundCraft(&fabricator).doWork();

	// Update segment state
	updateSegmentState(&fabricator, updatedSegment, Segment::State::Crafting, Segment::State::Crafted);
}

void CraftWork::doSegmentCleanup(const unsigned long long int shippedToChainMicros) const
{
	const auto segment = getSegmentAtChainMicros(shippedToChainMicros - persistenceWindowMicros);
	if (segment.has_value())
		store->deleteSegmentsBefore(segment.value()->id);
}

Segment CraftWork::buildSegmentInitial() const
{
	auto segment = Segment();
	segment.id = 0;
	segment.chainId = store->readChain().value()->id;
	segment.beginAtChainMicros = 0L;
	segment.delta = 0;
	segment.type = Segment::Type::Pending;
	segment.state = Segment::State::Planned;
	return segment;
}

Segment CraftWork::buildSegmentFollowing(const Segment* last) const
{
	if (!last->durationMicros.has_value())
	{
		throw std::runtime_error("Last segment has no duration, cannot fabricate next segment");
	}
	auto segment = Segment();
	segment.id = last->id + 1;
	segment.chainId = store->readChain().value()->id;
	segment.beginAtChainMicros = last->beginAtChainMicros + last->durationMicros.value();
	segment.delta = last->delta;
	segment.type = Segment::Type::Pending;
	segment.state = Segment::State::Planned;
	return segment;
}

void CraftWork::didFailWhile(const std::string& msgWhile, const std::exception& e)
{
	std::cerr << "Failed while " << msgWhile << " because " << e.what() << std::endl;
	running = false;
	finish();
}

const Segment*
CraftWork::updateSegmentState(Fabricator* fabricator, const Segment* inputSegment, const Segment::State fromState,
	const Segment::State toState)
{
	if (fromState != inputSegment->state)
		throw std::runtime_error("Segment[" + std::to_string(inputSegment->id) + "] " + Segment::toString(toState) + " requires Segment must be in " + Segment::toString(fromState) + " state.");
	Segment updateSegment = *inputSegment;
	updateSegment.state = toState;
	const auto updatedSegment = fabricator->updateSegment(updateSegment);

	// Segment transitioned to state OK
	return updatedSegment;
}

std::set<const SegmentChoice*> CraftWork::getChoices(const Segment* segment) const
{
	return store->readAllSegmentChoices(segment->id);
}

std::set<const SegmentChoiceArrangement*> CraftWork::getArrangements(const SegmentChoice* choice) const
{
	std::set<const SegmentChoiceArrangement*> results;
	for (auto arrangement : store->readAllSegmentChoiceArrangements(choice->segmentId))
		if (arrangement->segmentChoiceId == choice->id)
			results.emplace(arrangement);
	return results;
}

std::set<const SegmentChoiceArrangementPick*> CraftWork::getPicks(const SegmentChoiceArrangement* arrangement) const
{
	std::set<const SegmentChoiceArrangementPick*> results;
	for (auto pick : store->readAllSegmentChoiceArrangementPicks(arrangement->segmentId))
		if (pick->segmentChoiceArrangementId == arrangement->id)
			results.emplace(pick);
	return results;
}
