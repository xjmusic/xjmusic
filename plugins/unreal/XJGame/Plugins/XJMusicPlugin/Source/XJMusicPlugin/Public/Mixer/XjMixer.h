// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Manager/XjAudioLoader.h"
#include "XjMixer.generated.h"

USTRUCT()
struct FMixerAudio
{
	GENERATED_BODY()

public:

	FXjAudioWave Wave;

	FString Id;

	int32 StartSamples = 0;
	int32 EndSamples = 0;

	float FromVolume;
	float ToVolume;

	int32 GetSamplePointer() const
	{
		return SamplePointer;
	}

	int16 ReadSample(const int32 CurrentSample);

private:

	int32 SamplePointer = 0;

	float GetAmplitude(const float Delta) const
	{
		if (FromVolume == ToVolume)
		{
			return FromVolume;
		}

		return FromVolume + (ToVolume - FromVolume) * Delta;
	}
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

	float CalculateAmplitude(const FMixerAudio& Audio) const;

	int32 OnGeneratePCMAudio(TArray<uint8>& OutAudio, int32 NumSamples);

	int32 GetSampleRate() const
	{
		return SampleRate;
	}

	int32 GetNumChannels() const
	{
		return NumChannels;
	}

private:

	float FadeOutDuration = 0.5f;

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
};
