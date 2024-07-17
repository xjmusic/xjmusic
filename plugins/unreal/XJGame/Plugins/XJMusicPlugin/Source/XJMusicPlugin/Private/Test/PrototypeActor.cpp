// Fill out your copyright notice in the Description page of Project Settings.


#include "Test/PrototypeActor.h"
#include "Components/AudioComponent.h"
#include <optional>
#include <set>
#include <XjMusicInstanceSubsystem.h>

APrototypeActor::APrototypeActor()
{
	PrimaryActorTick.bCanEverTick = true;
}

void APrototypeActor::BeginPlay()
{
	Super::BeginPlay();

	WorkSettings DefaultSettings;

	FString PathToProject = XjProjectFolder + XjProjectFile;
	std::string PathToProjectStr(TCHAR_TO_UTF8(*PathToProject));
	UE_LOG(LogTemp, Display, TEXT("Path to project: %s"), *FString(PathToProjectStr.c_str()));
	
	FString PathToBuildFolder = XjProjectFolder + "build/";
	UE_LOG(LogTemp, Display, TEXT("Path to build folder: %s"), *PathToBuildFolder);

	try
	{
		XjMusicInstanceSubsystem = GetWorld()->GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>();
		if (XjMusicInstanceSubsystem)
		{
			XjMusicInstanceSubsystem->RetrieveProjectsContent(PathToBuildFolder);
		}
		else
		{
			UE_LOG(LogTemp, Error, TEXT("Cannot find XjMusicInstanceSubsystem"));
			return;
		}

		XjEngine = MakeUnique<Engine>(PathToProjectStr,
										Fabricator::ControlMode::Taxonomy, 
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
}

void APrototypeActor::BeginDestroy()
{
	Super::BeginDestroy();
}

void APrototypeActor::RunXjOneCycleTick(const float DeltaTime)
{
	if (!XjEngine)
	{
		return;
	}

	std::set<ActiveAudio> ReceivedAudios = XjEngine->runCycle(AtChainMicros);

	bool NeedSort = false;

	for (const ActiveAudio& Audio : ReceivedAudios)
	{
		FString WavKey = Audio.getAudio()->waveformKey.c_str();
		FString Name = Audio.getAudio()->name.c_str();

		long TransientMicros = Audio.getAudio()->transientSeconds * MICROS_PER_SECOND;
		long LengthMicros = Audio.getPick()->lengthMicros;

		unsigned long long StartTime = Audio.getStartAtChainMicros() - TransientMicros - AtChainMicros;
		unsigned long long EndTime = StartTime + TransientMicros + LengthMicros;

		FAudioPlayer AudioPlayer;
		AudioPlayer.StartTime = StartTime;
		AudioPlayer.EndTime = EndTime;
		AudioPlayer.Name = Name;
		AudioPlayer.Id = WavKey;

		if (AudioLookup.Contains(StartTime) && AudioLookup[StartTime].Id == AudioPlayer.Id)
		{
			AudioLookup[StartTime] = AudioPlayer;
		}
		else
		{
			AudioLookup.Add(StartTime, AudioPlayer);
			NeedSort = true;
		}
	}

	if (NeedSort)
	{
		AudioLookup.KeySort(TLess<unsigned long long>());
	}

	GEngine->AddOnScreenDebugMessage(-1, GetWorld()->GetDeltaSeconds(), FColor::Green,
	                                 FString::Printf(TEXT("Play at %d:"), AtChainMicros), true);

	AtChainMicros += MICROS_PER_CYCLE * DeltaTime;
}

void APrototypeActor::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);

	if (!HasSegmentsDubbedPastMinimumOffset() && IsWithinTimeLimit())
	{
		RunXjOneCycleTick(DeltaTime);
	}

	if (!XjMusicInstanceSubsystem)
	{
		return;
	}

	TArray<unsigned long long> EndedAudiosList;

	for (TPair<unsigned long long, FAudioPlayer>& AudioInfo : AudioLookup)
	{
		const unsigned long long& StartTime = AudioInfo.Key;
		FAudioPlayer& AudioPlayer = AudioInfo.Value;

		//We don't want to go to the unscheduled part
		if (StartTime > AtChainMicros)
		{
			break;
		}

		if (AudioPlayer.EndTime <= AtChainMicros)
		{
			XjMusicInstanceSubsystem->StopAudioByName(AudioPlayer.Id);
			CurrentlyPlayingIds.Remove(AudioPlayer.Id);

			EndedAudiosList.Add(AudioPlayer.StartTime);

			continue;
		}

		if (AudioPlayer.bIsPlaying)
		{
			continue;
		}

		if (CurrentlyPlayingIds.Contains(AudioPlayer.Id))
		{
			EndedAudiosList.Add(StartTime);
			continue;
		}

		CurrentlyPlayingIds.Add(AudioPlayer.Id);

		float OverridedStartTime = AtChainMicros - StartTime / 1000000.0f;
		
		XjMusicInstanceSubsystem->PlayAudioByName(AudioPlayer.Id);
		AudioPlayer.bIsPlaying = true;
	}

	for (unsigned long long Id : EndedAudiosList)
	{
		AudioLookup.Remove(Id);
	}
}
