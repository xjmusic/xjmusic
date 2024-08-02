// Fill out your copyright notice in the Description page of Project Settings.

#include "Engine/XjMainEngine.h"
#include <XjMusicInstanceSubsystem.h>

void TXjMainEngine::Setup(const FString& PathToProject)
{	
	std::string PathToProjectStr = TCHAR_TO_UTF8(*PathToProject);

	Fabricator::ControlMode	   ControlMode = Fabricator::ControlMode::Auto;
	std::optional<int>		   CraftAheadSeconds;
	std::optional<int>		   DubAheadSeconds;
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

void TXjMainEngine::Shutdown()
{
}

TArray<FAudioPlayer> TXjMainEngine::RunCycle(const uint64 ChainMicros)
{
	LastChainMicros.SetInMicros(ChainMicros);

	std::vector<AudioScheduleEvent> ReceivedAudioEvents = XjEngine->RunCycle(ChainMicros);

	TArray<FAudioPlayer> Output;

	for (const AudioScheduleEvent& Event : ReceivedAudioEvents)
	{
		FString WaveKey = Event.audio.getAudio()->waveformKey.c_str();
		FString Id = Event.audio.getId().c_str();
		FString Name = Event.audio.getAudio()->name.c_str();

		TimeRecord StartTime = Event.getStartAtChainMicros();
		TimeRecord EndTime = Event.audio.getStopAtChainMicros();

		FAudioPlayer AudioPlayer;
		AudioPlayer.StartTime = StartTime;
		AudioPlayer.EndTime = EndTime;
		AudioPlayer.Name = Name;
		AudioPlayer.Id = Id;
		AudioPlayer.WaveId = WaveKey;
		AudioPlayer.Event = (EAudioEventType)Event.type;

		Output.Add(AudioPlayer);
	}

	return Output;
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

FSegmentChoice TXjMainEngine::ParseSegmentChoice(const SegmentChoice* Choice)
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
			
			const uint64 AudioTransientMicros = Audio.value()->transientSeconds * MICROS_PER_SECOND;
			const uint64 AudioLengthMicros = Audio.value()->lengthSeconds * MICROS_PER_SECOND;
			const uint64 ThresholdActiveFrom = Pick->startAtSegmentMicros - AudioTransientMicros;
			const uint64 ThresholdActiveTo = Pick->lengthMicros == 0 ? Pick->startAtSegmentMicros - AudioTransientMicros + AudioLengthMicros : Pick->startAtSegmentMicros + Pick->lengthMicros;
			const bool	 bActive = LastChainMicros.GetMicros() >= ThresholdActiveFrom && LastChainMicros.GetMicros() <= ThresholdActiveTo;
			const uint64 DubbedToMicros = LastChainMicros.GetMicros() + XjEngine->getSettings().dubAheadMicros;
			const bool	 bDubbed = DubbedToMicros >= ThresholdActiveFrom && DubbedToMicros <= ThresholdActiveTo;

			FAudioPick AudioPick;
			AudioPick.Name = Audio.value()->name.c_str();
			AudioPick.StartTime.SetInMicros(Pick->startAtSegmentMicros);
			AudioPick.bActive = bActive || bDubbed;

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
		if (!(Segment->durationMicros.has_value() && Segment->beginAtChainMicros + Segment->durationMicros.value() > LastChainMicros.GetMicros()))
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

		for (const SegmentMeme* Meme : XjEngine->getSegmentStore()->readAllSegmentMemes({ Segment->id }))
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
				Info.MacroChoices.Add(ParseSegmentChoice(Choice));
			}
			else if (!Choice->programId.empty() && Program::Type::Main == Choice->programType)
			{
				Info.MainChoices.Add(ParseSegmentChoice(Choice));
			}
			else if ((!Choice->programId.empty() && Program::Type::Beat == Choice->programType) || Instrument::Type::Drum == Choice->instrumentType)
			{
				Info.BeatChoices.Add(ParseSegmentChoice(Choice));
			}
			else
			{
				Info.DetailChoices.Add(ParseSegmentChoice(Choice));
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
