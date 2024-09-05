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

	void LoadData(USoundWave* NewWave, const bool bAllowDecompression);

	bool IsValidToUse() const;

private:

	FByteBulkData* Bulk = nullptr;

	void FinishLoad();
};

UCLASS()
class XJMUSICPLUGIN_API UXjAudioLoader : public UObject
{
	GENERATED_BODY()

public:

	void Setup();

	void Shutdown();

	void DecompressAll();

	TSharedPtr<FXjAudioWave> GetSoundById(const FString& Id, const bool bAllowDecompression = false);

	bool IsLoadingAssets() const
	{
		return bLoading.Load();
	}

private:

	FStreamableManager StreamableManager;

	TSharedPtr<FStreamableHandle> InitialAssetsStream;

	TAtomic<bool> bLoading = true;
		
	TMap<FString, FSoftObjectPath> AudiosSoftReferences;
	TMap<FString, TSharedPtr<FXjAudioWave>> CachedAudios;

	void RetrieveProjectsContent();

	void OnAssetsLoaded();
};
