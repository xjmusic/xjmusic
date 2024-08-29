// Fill out your copyright notice in the Description page of Project Settings.


#include "Manager/XjAudioLoader.h"
#include "Settings/XJMusicDefaultSettings.h"
#include "Misc/FileHelper.h"
#include "Sound/SoundWave.h"
#include "Engine/AssetManager.h"
#include "Misc/FileHelper.h"
#include "HAL/PlatformFilemanager.h"
#include "Misc/Paths.h"
#include <AudioDecompress.h>
#include <AudioDevice.h>

FXjAudioWave::~FXjAudioWave()
{
	UnLoadData();
}

FXjAudioWave::FXjAudioWave(const FXjAudioWave& Other)
{
	Wave = Other.Wave;
	SamplesData = Other.SamplesData;
	NumSamples = Other.NumSamples;
}

const FXjAudioWave& FXjAudioWave::operator=(const FXjAudioWave& Other)
{
	Wave = Other.Wave;
	SamplesData = Other.SamplesData;
	NumSamples = Other.NumSamples;

	return *this;
}

void FXjAudioWave::LoadData(USoundWave* NewWave)
{
	FAudioDevice* AudioDevice = GEngine->GetMainAudioDeviceRaw();
	if (AudioDevice)
	{
		EDecompressionType DecompressionType = NewWave->DecompressionType;
		NewWave->DecompressionType = DTYPE_Native;

		FByteBulkData* Bulk = NewWave->GetCompressedData(NewWave->GetRuntimeFormat(), NewWave->GetPlatformCompressionOverridesForCurrentPlatform());
		if (Bulk)
		{
			NewWave->InitAudioResource(*Bulk);

			if (NewWave->DecompressionType != DTYPE_RealTime || NewWave->CachedRealtimeFirstBuffer == nullptr)
			{
				FAsyncAudioDecompress DecompressTask(NewWave, 128, AudioDevice);
				DecompressTask.StartSynchronousTask();
			}

			NewWave->DecompressionType = DecompressionType;
		}
	}

	TArrayView<SHORT> Arr((SHORT*)NewWave->RawPCMData, NewWave->RawPCMDataSize / 2);

	SamplesData = (int16*)Arr.GetData();
	NumSamples = NewWave->RawPCMDataSize / sizeof(int16);
}

void FXjAudioWave::UnLoadData()
{

}

bool FXjAudioWave::IsValidToUse() const
{
	return Wave && SamplesData;
}

void UXjAudioLoader::Setup()
{
	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (!XjSettings)
	{
		return;
	}

	RetrieveProjectsContent();
}

void UXjAudioLoader::Shutdown()
{
	AudiosSoftReferences.Reset();

	CachedAudios.Reset();
}

FXjAudioWave UXjAudioLoader::GetSoundById(const FString& Id)
{
	if (FXjAudioWave* Wave = CachedAudios.Find(Id))
	{
		return *Wave;
	}

	FSoftObjectPath* ObjectPath = AudiosSoftReferences.Find(Id);
	if (!ObjectPath)
	{
		return {};
	}

	USoundWave* Wave = Cast<USoundWave>(ObjectPath->ResolveObject());
	check(Wave);

	FXjAudioWave XjAudio;
	XjAudio.LoadData(Wave);

	CachedAudios.Add(Id, XjAudio);

	return XjAudio;
}

void UXjAudioLoader::RetrieveProjectsContent()
{
    UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
    if (!XjSettings)
    {
    	return;
    }
   
    UXjProject* Project = Cast<UXjProject>(XjSettings->LaunchProject.ResolveObject());
    check(Project);
   
    FAssetRegistryModule& AssetRegistryModule = FModuleManager::LoadModuleChecked<FAssetRegistryModule>("AssetRegistry");
    IAssetRegistry& AssetRegistry = AssetRegistryModule.Get();
   
    FString ContentDirectory = "/Game/XJ/" + Project->ProjectName;
   
    TArray<FAssetData> AudioData;
   
    AssetRegistry.ScanPathsSynchronous({ ContentDirectory }, true);
    AssetRegistry.GetAssetsByPath(*ContentDirectory, AudioData, true);
   
    for (const FAssetData& Data : AudioData)
    {
    	AudiosSoftReferences.Add(Data.AssetName.ToString() + ".wav", Data.ToSoftObjectPath());
    }
   
    TArray<FSoftObjectPath> Paths;
    AudiosSoftReferences.GenerateValueArray(Paths);
    InitialAssetsStream = StreamableManager.RequestAsyncLoad(Paths);
}