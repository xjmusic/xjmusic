// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Types/XjTypes.h"
#include "Manager/XjAudioLoader.h"
#include "Math/UnrealMathUtility.h"
#include "XjMixer.generated.h"

class UXjOutput;
class UAudioComponent;

class Envelope
{

public:

	float Out(const int32 Delta)
	{
		if (Delta > Exponential.Num())
		{
			return 0.0f;
		}

		return Delta > 0 ? Exponential[Exponential.Num() - Delta] : 1.0f;
	}

	float In(const int32 Delta)
	{
		if (Delta < 0.0f)
		{
			return 0.0f;
		}

		return Delta <  Exponential.Num() ? Exponential[Delta] : 1.0f;
	}

	void SetEnvelope(const int32 Samples)
	{
		Exponential.Reserve(Samples - 1);

		for (int i = 1; i < Samples; ++i) 
		{
			Exponential.Push(FMath::Sin(HALF_PI * i / Samples));
		}

		Length = Samples;
	}

	int32 GetLength() const
	{
		return Length;
	}

private:

	TArray<float> Exponential;
	int32 Length = 0;
};

USTRUCT()
struct FMixerAudio
{
	GENERATED_BODY()

public:

	void SetupEnvelops(const int32 ReleaseTimeSamples);

	float ReadSample(const int32 CurrentSample, const float FrameDelta);

	int32 GetSamplePointer() const
	{
		return SamplePointer;
	}

	int32 GetEndWithRelease() const
	{
		return EndSamples + FadeOutEnvelope->GetLength();
	}
	
	TSharedPtr<FXjAudioWave> Wave;

	FString Id;

	int32 StartSamples = 0;
	int32 EndSamples = 0;

	float FromVolume;
	float ToVolume;

	static FMixerAudio CreateMixerAudio(const FAudioPlayer& AudioPlayer, class UXjAudioLoader* AudioLoader);

private:
	
	TSharedPtr<Envelope> FadeOutEnvelope;

	int32 SamplePointer = 0;

	int32 ReleaseDelta = 0;

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

	void Setup(const bool bDefaultOutput);

	void Shutdown();

	void AddOrUpdateActiveAudio(const FMixerAudio& Audio);

	void RemoveActiveAudio(const FString& AudioId);

	int32 OnGeneratePCMAudio(TArray<uint8>& OutAudio, int32 NumSamples);

	bool IsPlaying() const
	{
		return StartMixing;
	}

	static int32 GetSampleRate()
	{
		return SampleRate;
	}

	static int32 GetNumChannels()
	{
		return NumChannels;
	}
	
	UPROPERTY()
	UAudioComponent* AudioComponent;

	UPROPERTY()
	UXjOutput* Output;

private:

	bool StartMixing = false;

	float FadeOutDuration = 0.5f;

	int32 SampleCounter = 0;

	TQueue<FMixerAudio> AudiosToUpdate;

	TQueue<FString> AudiosToRemove;

	TMap<FString, FMixerAudio> ActiveAudios;

	inline static int32 SampleRate = 0;
	inline static int32 NumChannels = 0;
};
