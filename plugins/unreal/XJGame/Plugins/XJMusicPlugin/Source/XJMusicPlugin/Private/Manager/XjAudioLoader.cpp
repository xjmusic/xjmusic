// Fill out your copyright notice in the Description page of Project Settings.


#include "Manager/XjAudioLoader.h"
#include "Settings/XJMusicDefaultSettings.h"
#include "Misc/FileHelper.h"
#include "Sound/SoundWave.h"

FXjAudioWave::~FXjAudioWave()
{
	UnLoadData();
}

void FXjAudioWave::LoadData(USoundWave* NewWave)
{
	if (!NewWave || NewWave == Wave || NewWave->RawData.IsLocked())
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
	AudioPathsByNameLookup.Reset();

	CachedSoundWaves.Reset();
}

FXjAudioWave UXjAudioLoader::GetOrLoadSoundById(const FString& Id, const float Duration)
{
	if (!AudioPathsByNameLookup.Contains(Id))
	{
		return {};
	}

	FString FilePath = AudioPathsByNameLookup[Id];

	const uint32 PathHash = GetTypeHash(FilePath);
	const uint32 DurationHash = GetTypeHash((int)Duration);
	const uint32 SearchHash = HashCombine(PathHash, DurationHash);

	if (FXjAudioWave* Result = CachedSoundWaves.Find(SearchHash))
	{
		return *Result;
	}

	USoundWave* SoundWave = NewObject<USoundWave>(USoundWave::StaticClass());
	if (!SoundWave)
	{
		UE_LOG(LogTemp, Error, TEXT("Failed to create USoundWave object"));
		return {};
	}

	TArray<uint8> RawFile;
	if (!FFileHelper::LoadFileToArray(RawFile, *FilePath))
	{
		return {};
	}

	uint32 NeededChunkSize = RawFile.Num();

	FWaveModInfo WaveInfo;
	if (WaveInfo.ReadWaveInfo(RawFile.GetData(), RawFile.Num()))
	{
		check(WaveInfo.pChannels);
		const uint32 Channels = *WaveInfo.pChannels;

		check(WaveInfo.pSamplesPerSec);
		const uint32 SampleRate = *WaveInfo.pSamplesPerSec;

		check(WaveInfo.pBlockAlign);
		const uint32 BlockAlign = *WaveInfo.pBlockAlign;

		check(WaveInfo.pWaveDataSize);
		const uint32 WaveData = *WaveInfo.pWaveDataSize;

		const uint32 HeaderSize = RawFile.Num() - WaveData;

		const uint32 NewSize = HeaderSize + Duration * SampleRate * BlockAlign;

		NeededChunkSize = FMath::Min(NeededChunkSize, NewSize);

		SoundWave->NumChannels = Channels;
		SoundWave->SetSampleRate(SampleRate);
	}

	SoundWave->SoundGroup = ESoundGroup::SOUNDGROUP_Music;
	SoundWave->RawData.Lock(LOCK_READ_WRITE);

	void* LockedData = SoundWave->RawData.Realloc(NeededChunkSize);
	if (LockedData)
	{
		FMemory::Memcpy(LockedData, RawFile.GetData(), NeededChunkSize);
	}

	SoundWave->RawData.Unlock();

	SoundWave->RawData.SetBulkDataFlags(BULKDATA_ForceInlinePayload);
	SoundWave->InvalidateCompressedData();

	FXjAudioWave XjAudio;
	XjAudio.LoadData(SoundWave);

	CachedSoundWaves.Add(SearchHash, XjAudio);

	return XjAudio;
}

void UXjAudioLoader::RetrieveProjectsContent(const FString& Directory)
{
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
