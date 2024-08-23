// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "UObject/NoExportTypes.h"
#include "UObject/SoftObjectPath.h"
#include "Engine/StreamableManager.h"
#include "XjAudioLoader.generated.h"

USTRUCT()
struct FXjAudioWave
{
	GENERATED_BODY();

public:
	
	FXjAudioWave() = default;
	~FXjAudioWave();

	FXjAudioWave(const FXjAudioWave& Other);
	const FXjAudioWave& operator = (const FXjAudioWave& Other);

	UPROPERTY()
	USoundWave* Wave;

	int32 NumSamples = 0;
	int16* SamplesData = nullptr;

	void LoadData(USoundWave* NewWave);

	void UnLoadData();

	bool IsValidToUse() const;
};

UCLASS()
class XJMUSICPLUGIN_API UXjAudioLoader : public UObject
{
	GENERATED_BODY()

public:

	void Setup();

	void Shutdown();

	FXjAudioWave GetSoundById(const FString& Id);

	bool IsLoading() const
	{
		return InitialAssetsStream.IsValid() && InitialAssetsStream->IsLoadingInProgress();
	}

private:

	FStreamableManager StreamableManager;
	
	TSharedPtr<FStreamableHandle> InitialAssetsStream;

	FCriticalSection StreamAccessCriticalSection;
	
	TMap<FString, FSoftObjectPath> AudiosSoftReferences;
	TMap<FString, FXjAudioWave> CachedAudios;

	void RetrieveProjectsContent();
};
