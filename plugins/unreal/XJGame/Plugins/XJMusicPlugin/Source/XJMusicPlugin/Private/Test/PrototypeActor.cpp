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

	WorkSettings DeaultSettings;

	try
	{
		std::string Path = "D:/Dev/vgm/vgm.xj";
		std::string BuildPath =  "D:/Dev/vgm/build";

		UXjMusicInstanceSubsystem* XjMusicInstanceSubsystem = GetWorld()->GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>();
		if (XjMusicInstanceSubsystem)
		{
			XjMusicInstanceSubsystem->RetriveProjectsContent(FString(BuildPath.c_str()));
		}

		engine = new Engine(Path, Fabricator::ControlMode::Taxonomy, DeaultSettings.craftAheadSeconds, DeaultSettings.dubAheadSeconds, DeaultSettings.persistenceWindowSeconds);
	
		if (!engine)
		{
			return;
		}

		std::set<const Template*> TemplatesInfo =  engine->getProjectContent()->getTemplates();


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

		engine->doOverrideMemes({ "EXPLORATION", "CALLTOACTION", "95BPM" });
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

void APrototypeActor::RunXjOneCycleTick()
{
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
			AudioLookup.Add(StartTime, Player);
		}
		else
		{
			bool Skip = false;

			for (const AudioPlayer& Info : ActiveAudios)
			{
				if (Info.Name == Name)
				{
					Skip = true;
					break;
				}
			}

			if (!Skip)
			{
				ActiveAudios.Add(Player);
			}
		}
	}

	GEngine->AddOnScreenDebugMessage(-1, GetWorld()->GetDeltaSeconds(), FColor::Green, FString::Printf(TEXT("Play at %d:"), atChainMicros));
	
	atChainMicros += MICROS_PER_CYCLE;
}

void APrototypeActor::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);

	if (!hasSegmentsDubbedPastMinimumOffset() && isWithinTimeLimit())
	{
		RunXjOneCycleTick();
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

	ActiveAudios.RemoveAll([](const AudioPlayer& Element)
		{
			return Element.StartTime >= Element.EndTime;
		});
	
	
	for (AudioPlayer& Player : ActiveAudios)
	{
		GEngine->AddOnScreenDebugMessage(-1, 0.0f, FColor::Red, FString::Printf(TEXT("%s(%s)"), *Player.Name, *Player.Id));

		if (!Player.bIsPlaying)
		{
			UXjMusicInstanceSubsystem* XjMusicInstanceSubsystem = GetWorld()->GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>();
			if (XjMusicInstanceSubsystem)
			{
				XjMusicInstanceSubsystem->PlayAudioByName(Player.Id);
				Player.bIsPlaying = true;
			}
		}
	}
}

