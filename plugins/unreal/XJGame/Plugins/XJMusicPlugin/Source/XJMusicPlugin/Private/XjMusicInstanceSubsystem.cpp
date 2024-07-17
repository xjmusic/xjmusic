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

#include "TimerManager.h"

void UXjMusicInstanceSubsystem::RetrieveProjectsContent(const FString& Directory)
{
	WorldAudioDeviceHandle = GetWorld()->GetAudioDevice();

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

void UXjMusicInstanceSubsystem::PlayAudioByName(const FString& Name, const float OffsetPlayTime)
{
	USoundWave* SoundWave = GetSoundWaveByName(Name);
	if (!SoundWave)
	{
		return;
	}

	if (FAudioDeviceHandle Device = GetWorld()->GetAudioDevice())
	{
		TSharedPtr<FActiveSound> ActiveSound = MakeShared<FActiveSound>();
		ActiveSound->SetSound(SoundWave);
		ActiveSound->SetWorld(GetWorld());

		ActiveSound->SetVolume(1.0f);

		ActiveSound->RequestedStartTime = OffsetPlayTime;

		ActiveSound->bIsUISound = true;
		ActiveSound->bAllowSpatialization = false;

		ActiveSound->Priority = SoundWave->GetPriority();
		ActiveSound->SubtitlePriority = SoundWave->GetSubtitlePriority();

		SoundsMap.Add(Name, ActiveSound);

		Device->AddNewActiveSound(*ActiveSound.Get());
	}
}

void UXjMusicInstanceSubsystem::StopAudioByName(const FString& Name)
{
	if (!SoundsMap.Contains(Name))
	{
		return;
	}

	FActiveSound* Sound = SoundsMap[Name].Get();

	if (!Sound)
	{
		return;
	}

	FAudioThread::RunCommandOnAudioThread([this, Sound, Name]()
		{
			TArray<FActiveSound*> ActiveSounds = WorldAudioDeviceHandle->GetActiveSounds();

			for (int32 SoundIndex = ActiveSounds.Num() - 1; SoundIndex >= 0; --SoundIndex)
			{
				FActiveSound* Inst = ActiveSounds[SoundIndex];
				if (Inst && (Inst->GetSound()->GetName() == Sound->GetSound()->GetName()))
				{
					WorldAudioDeviceHandle->AddSoundToStop(Inst);
					SoundsMap.Remove(Name);

					break;
				}
			}
		});
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

void UXjMusicInstanceSubsystem::TestPlayAllSounds()
{
	GEngine->AddOnScreenDebugMessage(-1, 15.0f, FColor::Green, "Audio test started");

	TestAudioCounter = 0;
	GetWorld()->GetTimerManager().SetTimer(TestTimerHandle, this, &UXjMusicInstanceSubsystem::OnTestTimerCallback,5.0f, true);
}

void UXjMusicInstanceSubsystem::OnTestTimerCallback()
{
	if (TestAudioCounter >= AudioPathsByNameLookup.Num())
	{
		GEngine->AddOnScreenDebugMessage(-1, 15.0f, FColor::Green, "Audio test finished");
		return;
	}

	TArray<FString> Names;
	AudioPathsByNameLookup.GetKeys(Names);

	GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Green, FString::Printf(TEXT("Played audio %d/%d"), TestAudioCounter + 1, AudioPathsByNameLookup.Num()));

	if (!TestLastAudioName.IsEmpty())
	{
		StopAudioByName(TestLastAudioName);
	}

	TestLastAudioName = Names[TestAudioCounter];
	PlayAudioByName(TestLastAudioName);

	TestAudioCounter++;
}
