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

		engine = std::make_unique<Engine>(PathToProjectStr,
		                    Fabricator::ControlMode::Taxonomy, DefaultSettings.craftAheadSeconds,
		                    DefaultSettings.dubAheadSeconds, DefaultSettings.persistenceWindowSeconds);

		if (!engine)
		{
			UE_LOG(LogTemp, Error, TEXT("Cannot instantiate XJ Engine"));
			return;
		}

		std::set<const Template*> TemplatesInfo = engine->getProjectContent()->getTemplates();


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

		engine->start(FirstTemplate->id);
	}
	catch (const std::invalid_argument& e)
	{
		FString str(e.what());
		UE_LOG(LogTemp, Error, TEXT("%s"), *str);
	}
}

void APrototypeActor::BeginDestroy()
{
	Super::BeginDestroy();
}

void APrototypeActor::RunXjOneCycleTick(const float DeltaTime)
{
	if (!engine) return;
	auto audios = engine->runCycle(atChainMicros);

	for (auto audio : audios)
	{
		FString Name = audio.getAudio()->name.c_str();
		FString WavKey = audio.getAudio()->waveformKey.c_str();

		auto TransientMicros = audio.getAudio()->transientSeconds * MICROS_PER_SECOND;
		auto LengthMicros = audio.getPick()->lengthMicros;

		auto StartTime = audio.getStartAtChainMicros() - TransientMicros - atChainMicros;
		auto EndTime = StartTime + TransientMicros + LengthMicros;

		AudioPlayer Player;
		Player.StartTime = StartTime;
		Player.EndTime = EndTime;
		Player.Name = Name;
		Player.Id = WavKey;

		if (StartTime > atChainMicros)
		{
			if (AudioLookup.Contains(StartTime))
			{
				AudioLookup[StartTime] = Player;
			}
			else
			{
				AudioLookup.Add(StartTime, Player);
			}
		}
		else
		{
			bool Skip = false;

			for (AudioPlayer& Info : ActiveAudios)
			{
				if (Info.Name == Name)
				{
					Info.EndTime = Player.EndTime;
					Skip = true;

					break;
				}
			}

			if (!Skip)
			{
				Player.StartTime = atChainMicros - Player.StartTime;
				ActiveAudios.Add(Player);
			}
		}
	}

	GEngine->AddOnScreenDebugMessage(-1, GetWorld()->GetDeltaSeconds(), FColor::Green,
	                                 FString::Printf(TEXT("Play at %d:"), atChainMicros));

	atChainMicros += MICROS_PER_CYCLE;
}

void APrototypeActor::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);

	if (!HasSegmentsDubbedPastMinimumOffset() && IsWithinTimeLimit())
	{
		RunXjOneCycleTick(DeltaTime);
	}

	TArray<unsigned long long> ToRemoveKeys;

	for (const TPair<unsigned long long, AudioPlayer>& Info : AudioLookup)
	{
		if (Info.Key > atChainMicros)
		{
			continue;
		}

		bool Skip = false;

		for (const AudioPlayer& Player : ActiveAudios)
		{
			if (Player.Name == Info.Value.Name)
			{
				Skip = true;
				break;
			}
		}

		if (!Skip)
		{
			ActiveAudios.Add(Info.Value);
		}

		ToRemoveKeys.Add(Info.Key);
	}

	for (auto Key : ToRemoveKeys)
	{
		AudioLookup.Remove(Key);
	}

	ActiveAudios.RemoveAll([this](const AudioPlayer& Element)
	{
		if (Element.EndTime >= atChainMicros)
		{
			if (XjMusicInstanceSubsystem)
			{
				GEngine->AddOnScreenDebugMessage(-1, 3.0f, FColor::Red,
				                                 FString::Printf(TEXT("Removed%s"), *Element.Name));
				XjMusicInstanceSubsystem->StopAudioByName(Element.Id);
			}

			return true;
		}

		return false;
	});


	for (AudioPlayer& Player : ActiveAudios)
	{
		GEngine->AddOnScreenDebugMessage(-1, 0.0f, FColor::Red,
		                                 FString::Printf(TEXT("%s(%s)"), *Player.Name, *Player.Id));

		if (!Player.bIsPlaying)
		{
			if (XjMusicInstanceSubsystem)
			{
				XjMusicInstanceSubsystem->PlayAudioByName(Player.Id, Player.StartTime / 1000000.0f);
				Player.bIsPlaying = true;
			}
		}
	}
}
