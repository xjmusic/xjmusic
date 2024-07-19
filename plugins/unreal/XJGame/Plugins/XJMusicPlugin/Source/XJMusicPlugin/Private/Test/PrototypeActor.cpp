// Fill out your copyright notice in the Description page of Project Settings.


#include "Test/PrototypeActor.h"
#include "Components/AudioComponent.h"
#include <optional>
#include <set>
#include <XjMusicInstanceSubsystem.h>

FXjRunnable::FXjRunnable()
{
	Thread = FRunnableThread::Create(this, TEXT("FXjRunnable"), 0, TPri_BelowNormal);
}

FXjRunnable::~FXjRunnable()
{
}

bool FXjRunnable::Init()
{
	return true;
}

uint32 FXjRunnable::Run()
{
	while (true)
	{

	}

	return uint32();
}

void FXjRunnable::Stop()
{
}

void FXjRunnable::Exit()
{
}

void FXjRunnable::EnsureCompletion()
{
}

APrototypeActor::APrototypeActor()
{
	PrimaryActorTick.bCanEverTick = true;
}


void APrototypeActor::BeginPlay()
{
	Super::BeginPlay();

	XjStartTime.SetInMicros(EntityUtils::currentTimeMillis());

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

void APrototypeActor::RunXjOneCycleTick(const float DeltaTime)
{
	if (!XjEngine)
	{
		return;
	}

	GEngine->AddOnScreenDebugMessage(-1, DeltaTime, FColor::Green, FString("Chain micros: ") + AtChainMicros.ToString(), false);

	std::set<ActiveAudio> ReceivedAudios = XjEngine->runCycle(AtChainMicros.GetMicros());

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

		if (AudiosLookup.Contains(StartTime))
		{
			TArray<FAudioPlayer>& SavedAudios = AudiosLookup[StartTime];

			bool SkipAdding = false;

			for (FAudioPlayer& SavedAudio : SavedAudios)
			{
				if (SavedAudio.Id != WaveKey)
				{
					continue;
				}

				if (SavedAudio.StartTime == AudioPlayer.StartTime)
				{
					SkipAdding = true;

					if (!(SavedAudio.EndTime == AudioPlayer.EndTime))
					{
						//Update end time then
					}
				}
			}

			if (!SkipAdding)
			{
				SavedAudios.Add(AudioPlayer);
			}
		}
		else
		{
			AudiosLookup.Add(StartTime, { AudioPlayer });
			AudiosKeys.Add(StartTime);
		}

		if (DebugViewAudioToTime.Contains(Name))
		{
			DebugViewAudioToTime[Name].Add(AudioPlayer);
		}
		else
		{
			DebugViewAudioToTime.Add(Name, { AudioPlayer });
		}

		if (DebugViewTimeToAudio.Contains(StartTime))
		{
			DebugViewTimeToAudio[StartTime].Add(Name);
		}
		else
		{
			DebugViewTimeToAudio.Add(StartTime, { Name });
		}
	}

	AudiosKeys.Sort();
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

	FString PlayingAudios = "Playing:\n";

	for (TimeRecord Time : AudiosKeys)
	{
		if (!AudiosLookup.Contains(Time))
		{
			continue;
		}

		TArray<FAudioPlayer>& Audios = AudiosLookup[Time];
		
		Audios.RemoveAll([this, &PlayingAudios](FAudioPlayer& Element)
			{
				if (Element.StartTime > AtChainMicros.GetMicros())
				{
					return false;
				}

				if (Element.EndTime <= AtChainMicros.GetMicros())
				{
					XjMusicInstanceSubsystem->StopAudioByName(Element.Id);
					return true;
				}

				PlayingAudios += FString::Printf(TEXT("%s start - %f  end - %f\n"), *Element.Name, Element.StartTime.GetSeconds(), Element.EndTime.GetSeconds());

				if (Element.bIsPlaying)
				{
					return false;
				}

				Element.bIsPlaying = true;

				XjMusicInstanceSubsystem->PlayAudioByName(Element.Id);

				return false;
			});
	}

	GEngine->AddOnScreenDebugMessage(-1, DeltaTime, FColor::Magenta, PlayingAudios);

	AtChainMicros.SetInMicros(AtChainMicros.GetMicros() + DeltaTime * MICROS_PER_SECOND);
}