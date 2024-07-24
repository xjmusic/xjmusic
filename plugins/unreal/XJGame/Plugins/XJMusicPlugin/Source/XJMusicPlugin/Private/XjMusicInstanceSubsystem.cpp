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

bool UXjMusicInstanceSubsystem::PlayAudioByName(const FString& Name, const float StartTime)
{
	if (IsAudioScheduled(Name, StartTime))
	{
		return false;
	}

	USoundWave* SoundWave = GetSoundWaveByName(Name);
	if (!SoundWave)
	{
		return false;
	}
	
	AsyncTask(ENamedThreads::GameThread, [this, SoundWave, StartTime, Name]()
		{
			UAudioComponent* NewAudioComponent = UGameplayStatics::CreateSound2D(GetWorld(), SoundWave);
			if (NewAudioComponent)
			{
				FQuartzQuantizationBoundary Boundary;
				Boundary.Quantization = EQuartzCommandQuantization::Bar;
				Boundary.Multiplier = StartTime;
				Boundary.CountingReferencePoint = EQuarztQuantizationReference::TransportRelative;

				NewAudioComponent->PlayQuantized(GetWorld(), QuartzClockHandle, Boundary, {});

				if (!SoundsMap.Contains(Name))
				{
					SoundsMap.Add(Name, {});
				}

				SoundsMap[Name].Add(StartTime, NewAudioComponent);
			}
		});

	return true;
}

void UXjMusicInstanceSubsystem::StopAudioByName(const FString& Name)
{

}

bool UXjMusicInstanceSubsystem::IsAudioScheduled(const FString& Name, const float Time) const
{
	if (SoundsMap.Contains(Name))
	{
		if (SoundsMap[Name].Contains(Time))
		{
			return true;
		}
	}

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
			QuartzClockHandle->SetTicksPerSecond(GetWorld(), {}, {}, QuartzClockHandle, 1000.0f);
		
			QuartzClockHandle->StartClock(GetWorld(), QuartzClockHandle);
		}
	}
}
