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

	int16 ReadSample();

private:
	int32 SamplePointer = 0;

	int32 ReadNumSamples = 0;
	int16* ReadSamplesData = nullptr;

	void BeginReadSample();

	void EndReadSample();
};

UCLASS()
class XJMUSICPLUGIN_API UXjMixer : public UObject
{
	GENERATED_BODY()

public:

	void Setup();

	void Shutdown();

	void AddOrUpdateActiveAudio(const FMixerAudio& Audio);

	void RemoveActiveAudio(const FString& AudioId);

private:

	int32 SampleRate = 0;
	int32 NumChannels = 0;

	UPROPERTY()
	class UAudioComponent* AudioComponent;

	UPROPERTY()
	class UXjOutput* Output;

	int32 SampleCounter = 0;

	TQueue<FMixerAudio> AudiosToUpdate;

	TQueue<FString> AudiosToRemove;

	TMap<FString, FMixerAudio> ActiveAudios;

	int32 OnGeneratePCMAudio(TArray<uint8>& OutAudio, int32 NumSamples);
};
