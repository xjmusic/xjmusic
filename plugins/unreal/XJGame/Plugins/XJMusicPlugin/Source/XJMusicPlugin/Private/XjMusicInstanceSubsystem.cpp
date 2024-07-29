// Fill out your copyright notice in the Description page of Project Settings.

#include "XjMusicInstanceSubsystem.h"
#include "Kismet/GameplayStatics.h"
#include <Settings/XJMusicDefaultSettings.h>
#include <Sound/SoundBase.h>
#include <Sound/SoundWave.h>
#include <Engine/StreamableManager.h>
#include <Engine/AssetManager.h>
#include <Engine/ObjectLibrary.h>
#include <Misc/FileHelper.h>
#include <Runtime/Engine/Public/AudioDevice.h>
#include <Async/Async.h>
#include <Manager/XjManager.h>

void UXjMusicInstanceSubsystem::Initialize(FSubsystemCollectionBase& Collection)
{
	Super::Initialize(Collection);
}

void UXjMusicInstanceSubsystem::Deinitialize()
{
	Super::Deinitialize();
}

void UXjMusicInstanceSubsystem::SetupXJ()
{
	if (IsValid(Manager))
	{
		return;
	}

	Manager = NewObject<UXjManager>(this);
	if (Manager)
	{
		Manager->Setup();
	}
}

void UXjMusicInstanceSubsystem::RetrieveProjectsContent(const FString& Directory)
{
	InitQuartz();

	TArray<FString> WavFiles;

	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (!XjSettings)
	{
		return;
	}

	FString WorkPath = Directory;

	IPlatformFile& PlatformFile = FPlatformFileManager::Get().GetPlatformFile();

	if (!PlatformFile.DirectoryExists(*WorkPath))
	{
		return;
	}

	TArray<FString> FoundAudioFilesPaths;

	PlatformFile.FindFilesRecursively(FoundAudioFilesPaths, *WorkPath, *AudioExtension);

	for (const FString& Path : FoundAudioFilesPaths)
	{
		FString Name = FPaths::GetCleanFilename(Path);
		AudioPathsByNameLookup.Add(Name, Path);
	}
}

bool UXjMusicInstanceSubsystem::PlayAudioByName(const FString& Name, const float StartTime, const float Duration)
{
	volatile float DurationSeconds = Duration  / 1000.0f;

	if (StartTime == 0.0f)
	{
		return false;
	}
	
	AsyncTask(ENamedThreads::GameThread, [this, StartTime, DurationSeconds, Name]()
		{
			if (IsAudioScheduled(Name, StartTime))
			{
				return;
			}

			USoundWave* SoundWave = GetSoundWaveByName(Name);
			if (!SoundWave)
			{
				return;
			}

			bool OverrideStartBars = false;

			FQuartzTransportTimeStamp CurrentTimestamp = QuartzClockHandle->GetCurrentTimestamp(GetWorld());
			float ActualCurrentTime = CurrentTimestamp.Seconds * CurrentTimestamp.BeatFraction;

			if (ActualCurrentTime > StartTime)
			{
				OverrideStartBars = true;
			}
			
			UAudioComponent* NewAudioComponent = UGameplayStatics::CreateSound2D(GetWorld(), SoundWave);
			if (NewAudioComponent)
			{
				if (OverrideStartBars)
				{
					float PredictedStartTime = ActualCurrentTime + (PlanAheadMs / 1000.0f);

					FQuartzQuantizationBoundary Boundary;
					Boundary.Quantization = EQuartzCommandQuantization::ThirtySecondNote;
					Boundary.CountingReferencePoint = EQuarztQuantizationReference::CurrentTimeRelative;
					Boundary.Multiplier = PredictedStartTime;
					
					NewAudioComponent->PlayQuantized(GetWorld(), QuartzClockHandle, Boundary, {}, PredictedStartTime);
				}
				else
				{
					FQuartzQuantizationBoundary Boundary;
					Boundary.Quantization = EQuartzCommandQuantization::ThirtySecondNote;
					Boundary.CountingReferencePoint = EQuarztQuantizationReference::TransportRelative;
					Boundary.Multiplier = StartTime;

					NewAudioComponent->PlayQuantized(GetWorld(), QuartzClockHandle, Boundary, {});
				}

				NewAudioComponent->StopDelayed(DurationSeconds);

				SoundsMapCriticalSection.Lock();

				if (!SoundsMap.Contains(Name))
				{
					SoundsMap.Add(Name, {});
				}

				SoundsMap[Name].Add(StartTime, NewAudioComponent);

				SoundsMapCriticalSection.Unlock();
			}
		});

	return true;
}

void UXjMusicInstanceSubsystem::StopAudioByName(const FString& Name)
{

}

bool UXjMusicInstanceSubsystem::IsAudioScheduled(const FString& Name, const float Time) const
{
	SoundsMapCriticalSection.Lock();

	if (SoundsMap.Contains(Name))
	{
		if (SoundsMap[Name].Contains(Time))
		{
			return true;
		}
	}

	SoundsMapCriticalSection.Unlock();

	return false;
}

USoundWave* UXjMusicInstanceSubsystem::GetSoundWaveByName(const FString& AudioName)
{
	if (!AudioPathsByNameLookup.Contains(AudioName))
	{
		return nullptr;
	}

	FString FilePath = AudioPathsByNameLookup[AudioName];

	USoundWave* SoundWave = NewObject<USoundWave>(USoundWave::StaticClass());
	if (!SoundWave)
	{
		UE_LOG(LogTemp, Error, TEXT("Failed to create USoundWave object"));
		return nullptr;
	}

	TArray<uint8> RawFile;
	if (!FFileHelper::LoadFileToArray(RawFile, *FilePath))
	{
		return nullptr;
	}

	SoundWave->SoundGroup = ESoundGroup::SOUNDGROUP_Default;
	SoundWave->NumChannels = 2; 

	SoundWave->RawData.Lock(LOCK_READ_WRITE);

	void* LockedData = SoundWave->RawData.Realloc(RawFile.Num());
	if (LockedData)
	{
		FMemory::Memcpy(LockedData, RawFile.GetData(), RawFile.Num());
	}

	SoundWave->RawData.Unlock();

	SoundWave->RawData.SetBulkDataFlags(BULKDATA_ForceInlinePayload);
	SoundWave->InvalidateCompressedData();
	//SoundWave->bNeedsThumbnailGeneration = true;
	
	return SoundWave;
}

void UXjMusicInstanceSubsystem::InitQuartz()
{
	UQuartzSubsystem* QuartzSubsystem = UQuartzSubsystem::Get(GetWorld());
	if (QuartzSubsystem)
	{
		FQuartzTimeSignature TimeSignatures;
		TimeSignatures.BeatType = EQuartzTimeSignatureQuantization::ThirtySecondNote;
		TimeSignatures.NumBeats = 1;

		FQuartzClockSettings Settings;
		Settings.TimeSignature = TimeSignatures;

		QuartzClockHandle = QuartzSubsystem->CreateNewClock(GetWorld(), "XJ Clock", Settings);
		if (QuartzClockHandle)
		{
			QuartzClockHandle->SetThirtySecondNotesPerMinute(GetWorld(), {}, {}, QuartzClockHandle, 60000);
		
			QuartzClockHandle->StartClock(GetWorld(), QuartzClockHandle);
		}
	}
}