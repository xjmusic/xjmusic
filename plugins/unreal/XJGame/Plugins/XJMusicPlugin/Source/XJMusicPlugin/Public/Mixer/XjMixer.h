// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "XjMixer.generated.h"

USTRUCT()
struct FMixerAudio
{
	GENERATED_BODY()

public:
	UPROPERTY()
	USoundWave* Wave;

	FString Id;

	int32 StartSamples = 0;
	int32 EndSamples = 0;
	int32 SamplePointer = 0;
};

UCLASS()
class XJMUSICPLUGIN_API UXjMixer : public UObject
{
	GENERATED_BODY()

public:

	void Setup();

	void Shutdown();

	void AddActiveAudio(const FMixerAudio& Audio);

	void UpdateActiveAudio(const FMixerAudio& Audio);

	void RemoveActiveAudio(const FMixerAudio& Audio);

private:

	uint32 SampleRate = 0;
	uint32 NumChannels = 0;

	UPROPERTY()
	class UAudioComponent* AudioComponent;

	UPROPERTY()
	class UXjOutput* Output;

	int32 SampleCounter = 0;

	FMixerAudio TestAudio;

	TMap<FString, FMixerAudio> ActiveAudios;

	int32 OnGeneratePCMAudio(TArray<uint8>& OutAudio, int32 NumSamples);
};
