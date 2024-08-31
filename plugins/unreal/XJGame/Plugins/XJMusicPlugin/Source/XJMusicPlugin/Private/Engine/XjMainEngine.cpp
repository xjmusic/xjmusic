// Fill out your copyright notice in the Description page of Project Settings.

#include "Engine/XjMainEngine.h"
#include <XjMusicInstanceSubsystem.h>

void TXjMainEngine::Setup(const FString& PathToProject)
{	
	std::string PathToProjectStr = TCHAR_TO_UTF8(*PathToProject);
	
	Fabricator::ControlMode	   ControlMode = Fabricator::ControlMode::Taxonomy;
	std::optional<int>		   CraftAheadSeconds = 60;
	std::optional<int>		   DubAheadSeconds = 30;
	std::optional<int>		   DeadlineSeconds;
	std::optional<int>		   PersistenceWindowSeconds;

	try
	{
		XjEngine = MakeUnique<Engine>(PathToProjectStr,
			ControlMode,
			CraftAheadSeconds,
			DubAheadSeconds,
			DeadlineSeconds,
			PersistenceWindowSeconds);

		if (!XjEngine)
		{
			UE_LOG(LogTemp, Error, TEXT("Cannot instantiate XJ Engine"));
			return;
		}

		volatile WorkSettings craft = XjEngine->getSettings();

		std::set<const Template*> TemplatesInfo = XjEngine->getProjectContent()->getTemplates();


		for (const Template* Info : TemplatesInfo)
		{
			FString Name(Info->name.c_str());

			UE_LOG(LogTemp, Warning, TEXT("Imported template: %s"), *Name);
		}

		if (TemplatesInfo.size() < 1)
		{
			return;
		}

		const Template* FirstTemplate = *TemplatesInfo.begin();

		CurrentTemplateName = FirstTemplate->name.c_str();

		XjEngine->start(FirstTemplate->id);
	}
	catch (const std::invalid_argument& Exception)
	{
		FString ErrorStr(Exception.what());
		UE_LOG(LogTemp, Error, TEXT("%s"), *ErrorStr);
	}
}

TArray<FAudioPlayer> TXjMainEngine::RunCycle(const uint64 ChainMicros)
{
	LastChainMicros.SetInMicros(ChainMicros);

	std::vector<AudioScheduleEvent> ReceivedAudioEvents = XjEngine->RunCycle(ChainMicros);

	TArray<FAudioPlayer> Output;

	for (const AudioScheduleEvent& Event : ReceivedAudioEvents)
	{
		FString WaveKey = Event.schedule.getAudio()->waveformKey.c_str();
		FString Id = Event.schedule.getId().c_str();
		FString Name = Event.schedule.getAudio()->name.c_str();

		TimeRecord StartTime;
		StartTime.SetInMicros(Event.getStartAtChainMicros());

		TimeRecord EndTime;
		EndTime.SetInMicros(Event.schedule.getStopAtChainMicros());

		TimeRecord ReleaseTime;
		ReleaseTime.SetInMillie(Event.schedule.getReleaseMillis());

		FAudioPlayer AudioPlayer;
		AudioPlayer.StartTime = StartTime;
		AudioPlayer.EndTime = EndTime;
		AudioPlayer.ReleaseTime = ReleaseTime;
		AudioPlayer.Name = Name;
		AudioPlayer.Id = Id;
		AudioPlayer.WaveId = WaveKey;
		AudioPlayer.Event = (EAudioEventType)Event.type;
		AudioPlayer.FromVolume = Event.schedule.getFromVolume();
		AudioPlayer.ToVolume = Event.schedule.getToVolume();

		Output.Add(AudioPlayer);
	}

	return Output;
}

void TXjMainEngine::DoOverrideTaxonomy(const FString& Taxonomy)
{
	std::string Cstr = TCHAR_TO_UTF8(*Taxonomy);

	std::istringstream sstream(Cstr);
	
	std::set<std::string> Memes;

	while (sstream)
	{
		std::string str;
		sstream >> str;

		Memes.insert(str);
	}

	if (XjEngine)
	{
		XjEngine->doOverrideMemes(Memes);
	}
}

void TXjMainEngine::DoOverrideMacro(const FString& Macro)
{
	std::string Cstr = TCHAR_TO_UTF8(*Macro);

	std::istringstream sstream(Cstr);

	std::set<std::string> Memes;

	while (sstream)
	{
		std::string str;
		sstream >> str;

		Memes.insert(str);
	}

	const Program* SelectedProgram = nullptr;

	for (const Program*& Program : XjEngine->getAllMacroPrograms())
	{
		if (Program->name.c_str() == Macro)
		{
			SelectedProgram = Program;
		}
	}

	if (XjEngine && SelectedProgram)
	{
		XjEngine->doOverrideMacro(SelectedProgram);
	}
}

void TXjMainEngine::DoOverrideIntensity(const float Intensity)
{
	if (XjEngine)
	{
		XjEngine->setIntensityOverride(Intensity);
	}
}

TArray<FString> TXjMainEngine::GetAllTaxonomyMemes() const
{
	if (!XjEngine)
	{
		return {};
	}
	
	TArray<FString> Taxonomies;

	for (const MemeCategory& Category : XjEngine->getTemplateContent()->getMemeTaxonomy().getCategories())
	{
		if (!Category.hasMemes())
		{
			continue;
		}

		for (const std::string& Meme : Category.getMemes())
		{
			Taxonomies.Add(Meme.c_str());
		}
	}

	return Taxonomies;
}

TArray<FString> TXjMainEngine::GetAllMacros() const
{
	if (!XjEngine)
	{
		return {};
	}

	TArray<FString> OutMacros;

	for (const Program*& Macro : XjEngine->getAllMacroPrograms())
	{
		OutMacros.Add(Macro->name.c_str());
	}

	return OutMacros;
}

FEngineSettings TXjMainEngine::GetSettings() const
{
	if (!XjEngine)
	{
		return {};
	}

	WorkSettings XjSettings = XjEngine->getSettings();

	FEngineSettings Settings;
	Settings.CraftAheadSeconds = XjSettings.craftAheadMicros / MICROS_PER_SECOND;
	Settings.DubAheadSeconds = XjSettings.dubAheadMicros / MICROS_PER_SECOND;
	Settings.DeadlineSeconds = XjSettings.deadlineMicros / MICROS_PER_SECOND;
	Settings.PersistenceWindowSeconds = XjSettings.persistenceWindowSeconds;

	return Settings;
}

FString TXjMainEngine::GetActiveTemplateName() const
{
	return CurrentTemplateName;
}

int TXjMainEngine::GetSegmentBarsBeats(const int Id) const
{
	const auto choice = XjEngine->getSegmentStore()->readChoice(Id, Program::Main);
	if (!choice.has_value())
	{
		return 0;
	}

	const auto program = XjEngine->getProjectContent()->getProgram(choice.value()->programId);
	if (!program.has_value())
	{
		return 0;
	}

	return program.value()->config.barBeats;
}

FSegmentChoice TXjMainEngine::ParseSegmentChoice(const SegmentChoice* Choice, const int64 SegmentMicros)
{
	FSegmentChoice Out;

	std::optional<const Instrument*> Instr = XjEngine->getProjectContent()->getInstrument(Choice->instrumentId);
	std::optional<const Program*>	 Prgm = XjEngine->getProjectContent()->getProgram(Choice->programId);

	if (Instr.has_value())
	{
		Out.Type = Instrument::toString(Instr.value()->type).c_str();
		Out.Mode = Instrument::toString(Instr.value()->mode).c_str();
		Out.Name = Instr.value()->name.c_str();

		for (const SegmentChoiceArrangementPick* Pick : XjEngine->getSegmentStore()->readAllSegmentChoiceArrangementPicks(Choice))
		{
			std::optional<const InstrumentAudio*> Audio = XjEngine->getProjectContent()->getInstrumentAudio(Pick->instrumentAudioId);
			if (!Audio.has_value())
			{
				continue;
			}
			
			const int64 AudioTransientMicros = Audio.value()->transientSeconds * MICROS_PER_SECOND;
			const int64 AudioLengthMicros = Audio.value()->lengthSeconds * MICROS_PER_SECOND;
			const int64 ThresholdActiveFrom = Pick->startAtSegmentMicros - AudioTransientMicros;
			const int64 ThresholdActiveTo = Pick->lengthMicros == 0 ? Pick->startAtSegmentMicros - AudioTransientMicros + AudioLengthMicros : Pick->startAtSegmentMicros + Pick->lengthMicros;
			const bool	bActive = SegmentMicros >= ThresholdActiveFrom && SegmentMicros <= ThresholdActiveTo;

			FAudioPick AudioPick;
			AudioPick.Name = Audio.value()->name.c_str();
			AudioPick.StartTime.SetInMicros(Pick->startAtSegmentMicros);
			AudioPick.bActive = bActive;

			Out.Picks.Add(AudioPick);
		}
	}
	else if (Prgm.has_value())
	{
		Out.Type = Program::toString(Prgm.value()->type).c_str();
		Out.Name = Prgm.value()->name.c_str();
	}

	return Out;
}

TArray<FSegmentInfo> TXjMainEngine::GetSegments()
{
	if (!XjEngine)
	{
		return {};
	}

	TArray<FSegmentInfo> Segments;

	for (const Segment* Segment : XjEngine->getSegmentStore()->readAllSegments())
	{
		if (!Segment->durationMicros.has_value() || Segment->beginAtChainMicros + Segment->durationMicros.value() < LastChainMicros.GetMicros())
		{
			continue;
		}

		FSegmentInfo Info;

		Info.Id = Segment->id;
		Info.Delta = Segment->delta;
		Info.StartTime.SetInMicros(Segment->beginAtChainMicros);
		Info.TypeStr = Segment::toString(Segment->type).c_str();

		Info.TotalBars = Segment->total / GetSegmentBarsBeats(Segment->id);
		Info.TotalTime.SetInMicros(Segment->durationMicros.value());

		Info.Intensity = Segment->intensity;

		Info.Tempo = Segment->tempo;

		Info.Key = Segment->key.c_str();

		const int64 AtSegmentMicros = LastChainMicros.GetMicros() - Segment->beginAtChainMicros;

		for (const SegmentMeme* Meme : XjEngine->getSegmentStore()->readAllSegmentMemes(Segment->id))
		{
			if (!Meme)
			{
				continue;
			}

			Info.Memes.Add(Meme->name.c_str());
		}
		
		for (const SegmentChoice* Choice : XjEngine->getSegmentStore()->readAllSegmentChoices(Segment->id))
		{
			if (!Choice->programId.empty() && Program::Type::Macro == Choice->programType)
			{
				Info.MacroChoices.Add(ParseSegmentChoice(Choice, AtSegmentMicros));
			}
			else if (!Choice->programId.empty() && Program::Type::Main == Choice->programType)
			{
				Info.MainChoices.Add(ParseSegmentChoice(Choice, AtSegmentMicros));
			}
			else if ((!Choice->programId.empty() && Program::Type::Beat == Choice->programType) || Instrument::Type::Drum == Choice->instrumentType)
			{
				Info.BeatChoices.Add(ParseSegmentChoice(Choice, AtSegmentMicros));
			}
			else
			{
				Info.DetailChoices.Add(ParseSegmentChoice(Choice, AtSegmentMicros));
			}
		}

		Info.Memes.Sort();

		Info.MacroChoices.Sort();
		Info.MainChoices.Sort();
		Info.BeatChoices.Sort();
		Info.DetailChoices.Sort();

		Segments.Add(Info);
	}

	return Segments;
}
