// Fill out your copyright notice in the Description page of Project Settings.


#include "Mixer/XjMixer.h"
#include "Mixer/XjOutput.h"
#include "Components/AudioComponent.h"
#include "Kismet/GameplayStatics.h"
#include "XjMusicInstanceSubsystem.h"

class EnvelopsCache
{
	
public:
	
	static TSharedPtr<Envelope> GetInstanceByLength(const int32 SamplesLength)
	{
		if(CacheMap.Contains(SamplesLength))
		{
			return CacheMap[SamplesLength];
		}

		TSharedPtr<Envelope> NewInstance = MakeShared<Envelope>();
		NewInstance->SetEnvelope(SamplesLength);

		CacheMap.Add(SamplesLength, NewInstance);

		return NewInstance;
	}

	static void Reset()
	{
		CacheMap.Reset();
	}
	
private:
	
	inline static TMap<int32, TSharedPtr<Envelope>> CacheMap;
};

void FMixerAudio::SetupEnvelops(const int32 ReleaseTimeSamples)
{
	FadeOutEnvelope = EnvelopsCache::GetInstanceByLength(ReleaseTimeSamples);
	FadeInEnvelope = EnvelopsCache::GetInstanceByLength(0.1f * UXjMixer::GetSampleRate());
}

static float ApplyLogarithmicCompression(const float Sample)
{
	if(Sample < -1.0f)
	{
		return -FMath::Loge(-Sample - 0.85f) / 14.0f - 0.75f;
	}
	else if(Sample > 1.0f)
	{
		return FMath::Loge(Sample - 0.85f) / 14.0f + 0.75f;
	}

	return (Sample / 1.61803398875f);
}

float FMixerAudio::ReadSample(const int32 CurrentSample, const float FrameDelta)
{
	SamplePointer = CurrentSample - StartSamples;

	float Sample = 0;

	if (!Wave.SamplesData || SamplePointer < 0)
	{
		return Sample;
	}

	if(SamplePointer <= FMath::Min((GetEndWithRelease() - StartSamples), Wave.NumSamples))
	{
		Sample = ((float)Wave.SamplesData[SamplePointer] / INT16_MAX)
				* GetAmplitude(FrameDelta) * FadeOutEnvelope->Out(ReleaseDelta) * FadeInEnvelope->In(SamplePointer);

		if (CurrentSample > EndSamples)
		{
			ReleaseDelta++;
		}
	}

	return Sample;
}

void UXjMixer::Setup(const bool bDefaultOutput)
{
	Output = NewObject<UXjOutput>();

	if (!IsValid(Output))
	{
		return;
	}

	SampleRate = 48000;
	NumChannels = 2;

	Output->SetSampleRate(SampleRate);
	Output->NumChannels = NumChannels;
	Output->Duration = INDEFINITELY_LOOPING_DURATION;
	Output->SoundGroup = ESoundGroup::SOUNDGROUP_Music;
	Output->VirtualizationMode = EVirtualizationMode::PlayWhenSilent;
	Output->OnGeneratePCMData.BindUObject(this, &UXjMixer::OnGeneratePCMAudio);

	if(!bDefaultOutput)
	{
		return;
	}
	
	AudioComponent = UGameplayStatics::CreateSound2D(GetWorld(), Output);
	if (AudioComponent)
	{
		AudioComponent->Play();
	}
}

void UXjMixer::Shutdown()
{
	AudioComponent->MarkPendingKill();
	Output->MarkPendingKill();

	EnvelopsCache::Reset();
}

void UXjMixer::AddOrUpdateActiveAudio(const FMixerAudio& Audio)
{
	AudiosToUpdate.Enqueue(Audio);
}

void UXjMixer::RemoveActiveAudio(const FString& AudioId)
{
	AudiosToRemove.Enqueue(AudioId);
}

int32 UXjMixer::OnGeneratePCMAudio(TArray<uint8>& OutAudio, int32 NumSamples)
{
	OutAudio.Reset();

	FMixerAudio UpdatedAudio;
	while (AudiosToUpdate.Dequeue(UpdatedAudio))
	{
		if (ActiveAudios.Contains(UpdatedAudio.Id))
		{
			ActiveAudios[UpdatedAudio.Id] = UpdatedAudio;
		}	
		else
		{
			ActiveAudios.Add(UpdatedAudio.Id, UpdatedAudio);

			StartMixing = true;
		}
	}

	FString AudioId;
	while (AudiosToRemove.Dequeue(AudioId))
	{
		ActiveAudios.Remove(AudioId);
	}

	OutAudio.AddZeroed(NumSamples * sizeof(float));
	float* OutAudioBuffer = (float*)OutAudio.GetData();

	for (int32 Sample = 0; Sample < NumSamples; ++Sample)
	{
		float MixedData = 0;

		for (TPair<FString, FMixerAudio>& Audio : ActiveAudios)
		{
			float FrameDelta = 1.0f / FMath::Min(NumSamples, Audio.Value.GetEndWithRelease() - SampleCounter);

			if (SampleCounter >= Audio.Value.StartSamples)
			{
				MixedData += Audio.Value.ReadSample(SampleCounter, FrameDelta);
			}
		}

		OutAudioBuffer[Sample] = ApplyLogarithmicCompression(MixedData);

		if (StartMixing)
		{
			SampleCounter += 1;
		}
	}

	return NumSamples;
}