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
#include <Components/AudioComponent.h>

#include "TimerManager.h"

void UXjMusicInstanceSubsystem::RetriveProjectsContent()
{
	TArray<FString> WavFiles;

	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (!XjSettings)
	{
		return;
	}

	FString WorkPath = XjSettings->GetFullWorkPath();

	IPlatformFile& PlatformFile = FPlatformFileManager::Get().GetPlatformFile();

	if (!PlatformFile.DirectoryExists(*WorkPath))
	{
		return;
	}

	TArray<FString> FoundAudioFilesPaths;

	PlatformFile.FindFilesRecursively(FoundAudioFilesPaths, *WorkPath, *AudioExtension);

	for (const FString& Path : FoundAudioFilesPaths)
	{
		FString Name = FPaths::GetBaseFilename(Path);
		AudioPathsByNameLookup.Add(Name, Path);
	}
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

	AudioComponent = 
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

	if (USoundWave* Sound = GetSoundWaveByName(Names[TestAudioCounter]))
	{
		GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Green, FString::Printf(TEXT("Played audio %d/%d"), TestAudioCounter, AudioPathsByNameLookup.Num()));

		//Play sound	
	}

	TestAudioCounter++;
}
