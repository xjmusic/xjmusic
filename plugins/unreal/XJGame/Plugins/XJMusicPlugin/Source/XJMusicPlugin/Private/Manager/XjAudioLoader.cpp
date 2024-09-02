// Fill out your copyright notice in the Description page of Project Settings.


#include "Manager/XjAudioLoader.h"
#include "Settings/XJMusicDefaultSettings.h"
#include "Misc/FileHelper.h"
#include "Sound/SoundWave.h"
#include "Engine/AssetManager.h"
#include "Misc/FileHelper.h"
#include "HAL/PlatformFilemanager.h"
#include "Misc/Paths.h"
#include "AudioDevice.h"
#include "Mixer/XjMixer.h"
#include "Manager/XjManager.h"
#include "AudioDecompress.h"
#include "AudioDevice.h"

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
	if (!NewWave || NewWave == Wave)
	{
		return;
	}

	Wave = NewWave;

	if (Wave->RawPCMData == nullptr)
	{
		FAudioDevice* AudioDevice = GEngine->GetMainAudioDeviceRaw();
		if (!AudioDevice)
		{
			return;
		}

		EDecompressionType DecompressionType = Wave->DecompressionType;
		Wave->DecompressionType = DTYPE_Native;

		FName Format;
		
#if ENGINE_MAJOR_VERSION >= 5
		Format = Wave->GetRuntimeFormat();
#else
		Format = AudioDevice->GetRuntimeFormat(Wave);
#endif

		Bulk = Wave->GetCompressedData(Format, Wave->GetPlatformCompressionOverridesForCurrentPlatform());

		if (!Bulk)
		{
			return;
		}

		Wave->InitAudioResource(*Bulk);

		if (Wave->DecompressionType == DTYPE_RealTime || Wave->CachedRealtimeFirstBuffer != nullptr)
		{
			return;
		}

		DecompressTask = MakeShared<FAsyncAudioDecompress>(Wave, 128, AudioDevice);

		DecompressionSemaphore.IncrementExchange();

		Async(EAsyncExecution::TaskGraphMainThread, [this]()
			{
				DecompressTask->StartSynchronousTask();

				DecompressionSemaphore.DecrementExchange();

				AsyncTask(ENamedThreads::GameThread, [this]()
					{
						Wave->DecompressionType = DTYPE_Native;

						FinishLoad();
					});
			});
	}
	else
	{
		FinishLoad();
	}
}

bool FXjAudioWave::IsValidToUse() const
{
	return Wave && SamplesData;
}

void FXjAudioWave::FinishLoad()
{
	TArrayView<uint8> Arr((uint8*)Wave->RawPCMData, Wave->RawPCMDataSize / 2);

	SamplesData = (int16*)Arr.GetData();
	NumSamples = Wave->RawPCMDataSize / sizeof(int16);
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

TSharedPtr<FXjAudioWave> UXjAudioLoader::GetSoundById(const FString& Id)
{
	if (TSharedPtr<FXjAudioWave>* Wave = CachedAudios.Find(Id))
	{
		return *Wave;
	}

	FSoftObjectPath* ObjectPath = AudiosSoftReferences.Find(Id);
	if (!ObjectPath)
	{
		return {};
	}

	USoundWave* Wave = Cast<USoundWave>(ObjectPath->ResolveObject());
	if (!Wave)
	{
		return {};
	}

	Wave->LoadingBehavior = ESoundWaveLoadingBehavior::ForceInline;

	TSharedPtr<FXjAudioWave> XjAudio = MakeShared<FXjAudioWave>();
	XjAudio->LoadData(Wave);
	
	CachedAudios.Add(Id, XjAudio);

	return MoveTemp(XjAudio);
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