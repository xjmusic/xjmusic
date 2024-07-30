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
	std::vector<AudioScheduleEvent> ReceivedAudioEvents = XjEngine->RunCycle(ChainMicros);

	TArray<FAudioPlayer> Output;

	for (const AudioScheduleEvent& Event : ReceivedAudioEvents)
	{
		FString WaveKey = Event.audio.getAudio()->waveformKey.c_str();
		FString Name = Event.audio.getAudio()->name.c_str();

		TimeRecord StartTime = Event.getStartAtChainMicros();
		TimeRecord EndTime = Event.audio.getStopAtChainMicros();

		FAudioPlayer AudioPlayer;
		AudioPlayer.StartTime = StartTime;
		AudioPlayer.EndTime = EndTime;
		AudioPlayer.Name = Name;
		AudioPlayer.Id = WaveKey;
		AudioPlayer.Event = (EAudioEventType)Event.type;

		Output.Add(AudioPlayer);
	}

	return Output;
}

EngineSettings TXjMainEngine::GetSettings() const
{
	if (!XjEngine)
	{
		return {};
	}

	WorkSettings XjSettings = XjEngine->getSettings();

	EngineSettings Settings;
	Settings.CraftAheadSeconds = XjSettings.craftAheadMicros;
	Settings.DubAheadSeconds = XjSettings.dubAheadMicros;
	Settings.PersistenceWindowSeconds = XjSettings.persistenceWindowSeconds;

	return Settings;
}
