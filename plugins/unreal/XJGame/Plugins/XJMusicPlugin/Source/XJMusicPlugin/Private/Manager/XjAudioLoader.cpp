// Fill out your copyright notice in the Description page of Project Settings.


#include "Manager/XjAudioLoader.h"
#include "Settings/XJMusicDefaultSettings.h"
#include "Misc/FileHelper.h"
#include "Sound/SoundWave.h"
#include "AssetRegistryModule.h"
#include "Engine/AssetManager.h"
#include "XJMusicPlugin.h"

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
	if (!NewWave || NewWave == Wave)
	{
		return;
	}

	Wave = NewWave;

	uint8* RawData = (uint8*)Wave->RawData.LockReadOnly();

	if (!RawData)
	{
		return;
	}

	int32 RawDataSize = Wave->RawData.GetBulkDataSize();

	SamplesData = (int16*)RawData;
	NumSamples = RawDataSize / sizeof(int16);
}

void FXjAudioWave::UnLoadData()
{
	if (!IsValidToUse() || !Wave->RawData.IsLocked())
	{
		return;
	}

	Wave->RawData.Unlock();
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

	FString ProjectPath = XjSettings->PathToXjProjectFile;
	FString FolderContainingProject = FPaths::GetPath(ProjectPath);
	FString PathToBuildFolder = FPaths::Combine(FolderContainingProject, TEXT("build/"));

	RetrieveProjectsContent(PathToBuildFolder);
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

void UXjAudioLoader::RetrieveProjectsContent(const FString& Directory)
{
	UXJMusicDefaultSettings* XjSettings = GetMutableDefault<UXJMusicDefaultSettings>();
	if (!XjSettings)
	{
		return;
	}

	FAssetRegistryModule& AssetRegistryModule = FModuleManager::LoadModuleChecked<FAssetRegistryModule>("AssetRegistry");
	IAssetRegistry& AssetRegistry = AssetRegistryModule.Get();

	FString ContentDirectory = "/Game/XJ/" + XjSettings->XjProjectName;

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
