// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "UObject/NoExportTypes.h"
#include "XjAudioLoader.generated.h"

USTRUCT()
struct FXjAudioWave
{
	GENERATED_BODY();

public:
	FXjAudioWave() = default;
	~FXjAudioWave();

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

	FXjAudioWave GetOrLoadSoundById(const FString& Id, const float Duration);

	void Setup();

	void Shutdown();

private:
	
	const FString AudioExtension = ".wav";

	TMap<FString, FString> AudioPathsByNameLookup;

	UPROPERTY()
	TMap<uint32, FXjAudioWave> CachedSoundWaves;

	void RetrieveProjectsContent(const FString& Directory);
};
