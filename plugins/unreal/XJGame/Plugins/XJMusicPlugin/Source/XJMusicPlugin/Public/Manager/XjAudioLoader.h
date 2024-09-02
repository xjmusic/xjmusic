// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "UObject/NoExportTypes.h"
#include "UObject/SoftObjectPath.h"
#include "Engine/StreamableManager.h"
#include "AudioDecompress.h"
#include "XjAudioLoader.generated.h"

USTRUCT()
struct FXjAudioWave
{
	GENERATED_BODY();

public:
	
	FXjAudioWave() = default;

	FXjAudioWave(const FXjAudioWave& Other);
	const FXjAudioWave& operator = (const FXjAudioWave& Other);

	UPROPERTY()
	USoundWave* Wave;

	int32 NumSamples = 0;
	int16* SamplesData = nullptr;

	void LoadData(USoundWave* NewWave);

	bool IsValidToUse() const;

	static bool IsDecompressionInProgress()
	{
		return DecompressionSemaphore.Load() != 0;
	}

private:

	TSharedPtr<FAsyncAudioDecompress> DecompressTask;

	FByteBulkData* Bulk = nullptr;

	void FinishLoad();

	inline static TAtomic<uint8> DecompressionSemaphore = 0;
};

UCLASS()
class XJMUSICPLUGIN_API UXjAudioLoader : public UObject
{
	GENERATED_BODY()

public:

	void Setup();

	void Shutdown();

	TSharedPtr<FXjAudioWave> GetSoundById(const FString& Id);

	TSharedPtr<FStreamableHandle> InitialAssetsStream;

private:

	FStreamableManager StreamableManager;
		
	TMap<FString, FSoftObjectPath> AudiosSoftReferences;
	TMap<FString, TSharedPtr<FXjAudioWave>> CachedAudios;

	void RetrieveProjectsContent();
};
