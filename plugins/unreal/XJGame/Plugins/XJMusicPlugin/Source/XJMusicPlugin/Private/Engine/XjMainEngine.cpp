// Fill out your copyright notice in the Description page of Project Settings.

#include "Engine/XjMainEngine.h"
#include <XjMusicInstanceSubsystem.h>

void TXjMainEngine::Setup(const FString& PathToProject)
{
	WorkSettings DefaultSettings;
	
	std::string PathToProjectStr = TCHAR_TO_UTF8(*PathToProject);

	try
	{
		XjEngine = MakeUnique<Engine>(PathToProjectStr,
			DefaultSettings.controlMode,
			DefaultSettings.craftAheadSeconds,
			DefaultSettings.dubAheadSeconds,
			DefaultSettings.persistenceWindowSeconds);

		if (!XjEngine)
		{
			UE_LOG(LogTemp, Error, TEXT("Cannot instantiate XJ Engine"));
			return;
		}

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

		MemeTaxonomy Taxonomy = XjEngine->getMemeTaxonomy().value();
		std::set<MemeCategory> Categories = Taxonomy.getCategories();

		std::set<std::string> Memes;

		for (MemeCategory Category : Categories)
		{
			if (Category.hasMemes())
			{
				std::string Meme = *Category.getMemes().begin();
				Memes.insert(Meme);
			}
		}

		FString MemesStr = "Activated memes: \n";

		for (std::string Meme : Memes)
		{
			FString MemeStr = Meme.c_str();
			MemesStr += MemeStr + "\n";
		}

		GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Red, MemesStr);

		XjEngine->doOverrideMemes(Memes);


		const Template* FirstTemplate = *TemplatesInfo.begin();

		XjEngine->start(FirstTemplate->id);
	}
	catch (const std::invalid_argument& Exception)
	{
		FString ErrorStr(Exception.what());
		UE_LOG(LogTemp, Error, TEXT("%s"), *ErrorStr);
	}

	FPlatformProcess::Sleep(5.0f);
}

void TXjMainEngine::Shutdown()
{
}

TSet<FAudioPlayer> TXjMainEngine::RunCycle(const uint64 ChainMicros)
{
	std::set<ActiveAudio> ReceivedAudios = XjEngine->RunCycle(ChainMicros);

	TSet<FAudioPlayer> Output;

	for (const ActiveAudio& Audio : ReceivedAudios)
	{
		FString WaveKey = Audio.getAudio()->waveformKey.c_str();
		FString Name = Audio.getAudio()->name.c_str();

		long TransientMicros = Audio.getAudio()->transientSeconds * MICROS_PER_SECOND;
		long LengthMicros = Audio.getPick()->lengthMicros;

		TimeRecord StartTime = Audio.getStartAtChainMicros();
		TimeRecord EndTime = Audio.getStopAtChainMicros().value();

		FAudioPlayer AudioPlayer;
		AudioPlayer.StartTime = StartTime;
		AudioPlayer.EndTime = EndTime;
		AudioPlayer.Name = Name;
		AudioPlayer.Id = WaveKey;

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
	Settings.CraftAheadSeconds = XjSettings.craftAheadSeconds;
	Settings.DubAheadSeconds = XjSettings.dubAheadSeconds;
	Settings.PersistenceWindowSeconds = XjSettings.persistenceWindowSeconds;

	return Settings;
}
