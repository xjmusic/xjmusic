// Fill out your copyright notice in the Description page of Project Settings.


#include "Test/PrototypeActor.h"
#include "Components/AudioComponent.h"
#include <optional>
#include <set>
#include <XjMusicInstanceSubsystem.h>
#include <Math/UnrealMathUtility.h>

FXjRunnable::FXjRunnable(const FString& XjProjectFolder, const FString& XjProjectFile, UWorld* World)
{
	check(World);

	XjStartTime.SetInMicros(EntityUtils::currentTimeMillis());

	WorkSettings DefaultSettings;

	FString PathToProject = XjProjectFolder + XjProjectFile;
	std::string PathToProjectStr(TCHAR_TO_UTF8(*PathToProject));
	UE_LOG(LogTemp, Display, TEXT("Path to project: %s"), *FString(PathToProjectStr.c_str()));

	FString PathToBuildFolder = XjProjectFolder + "build/";
	UE_LOG(LogTemp, Display, TEXT("Path to build folder: %s"), *PathToBuildFolder);

	try
	{
		XjMusicInstanceSubsystem = World->GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>();
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

	Thread = FRunnableThread::Create(this, TEXT("XJ Thread"), 0, TPri_TimeCritical);
}

FXjRunnable::~FXjRunnable()
{
	if (Thread)
	{
		delete Thread;
		Thread = nullptr;
	}
}

bool FXjRunnable::Init()
{
	return true;
}

uint32 FXjRunnable::Run()
{
	FString DebugInfo;

	while (!bShouldStop)
	{
		float StartFrameTime = FPlatformTime::Seconds();

		if (!HasSegmentsDubbedPastMinimumOffset() && IsWithinTimeLimit())
		{
			RunXjOneCycleTick();
		}

		if (!XjMusicInstanceSubsystem)
		{
			return 0;
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
						//XjMusicInstanceSubsystem->StopAudioByName(Element.Id);
						return true;
					}

					PlayingAudios += FString::Printf(TEXT("%s start - %f  end - %f\n"), *Element.Name, Element.StartTime.GetSeconds(), Element.EndTime.GetSeconds());

					if (Element.bIsPlaying)
					{
						return false;
					}

					Element.bIsPlaying = true;

					//XjMusicInstanceSubsystem->PlayAudioByName(Element.Id);

					return false;
				});
		}

		{
			float EndFrameTime = FPlatformTime::Seconds();

			float DeltaTime = EndFrameTime - StartFrameTime;
			float SleepInterval = 1.0f / RunCycleFrequency - (EndFrameTime - StartFrameTime);
			SleepInterval = FMath::Max(SleepInterval, 0.0f);

			FPlatformProcess::Sleep(SleepInterval);

			if (SleepInterval == 0.0f)
			{
				SleepInterval = DeltaTime;
			}

			AtChainMicros.SetInSeconds(AtChainMicros.GetSeconds() + SleepInterval);


			DebugInfo = FString("Chain micros: ") + AtChainMicros.ToString();
			DebugInfo += FString::Printf(TEXT("\n %f ms"), SleepInterval);

			GEngine->AddOnScreenDebugMessage(-1, SleepInterval, FColor::Green, DebugInfo);
			GEngine->AddOnScreenDebugMessage(-1, SleepInterval, FColor::Magenta, PlayingAudios);
		}
	}

	return 0;
}

void FXjRunnable::Stop()
{
	bShouldStop = true;
	Thread->WaitForCompletion();
}

void FXjRunnable::RunXjOneCycleTick()
{
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

APrototypeActor::APrototypeActor()
{
	PrimaryActorTick.bCanEverTick = false;
}

void APrototypeActor::BeginPlay()
{
	XjThread = new FXjRunnable(XjProjectFolder, XjProjectFile, GetWorld()); 
}

void APrototypeActor::EndPlay(const EEndPlayReason::Type EndPlayReason)
{
	if (XjThread)
	{
		XjThread->Stop();
		delete XjThread;
	}
}