// Fill out your copyright notice in the Description page of Project Settings.


#include "Mixer/XjMixer.h"
#include "Mixer/XjOutput.h"
#include "Components/AudioComponent.h"
#include "Kismet/GameplayStatics.h"
#include "XjMusicInstanceSubsystem.h"

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
		Sample = (float(Wave.SamplesData[SamplePointer] * GetAmplitude(FrameDelta)) / INT16_MAX) 
			* FadeOutEnvelope.Out(ReleaseDelta) * FadeOutEnvelope.In(SamplePointer);

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
}

void UXjMixer::AddOrUpdateActiveAudio(const FMixerAudio& Audio)
{
	AudiosToUpdate.Enqueue(Audio);
}

void UXjMixer::RemoveActiveAudio(const FString& AudioId)
{
	AudiosToRemove.Enqueue(AudioId);
}

float UXjMixer::CalculateAmplitude(const FMixerAudio& Audio) const
{
	const int32 Difference = Audio.EndSamples - Audio.GetSamplePointer();
	const int32 FadeOutSamples = FadeOutDuration * SampleRate;

	return 1.0f - FMath::Clamp((float)FadeOutSamples / (float)Difference, 0.0f, 1.0f);
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

	OutAudio.AddZeroed(NumSamples * sizeof(int16));
	int16* OutAudioBuffer = (int16*)OutAudio.GetData();

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

		//Clipping fix. TODO make this using limits in somewhere else
		MixedData = FMath::Clamp(MixedData, -1.0f, 1.0f);

		OutAudioBuffer[Sample] = MixedData * INT16_MAX;

		if (StartMixing)
		{
			SampleCounter += 1;
		}
	}

	return NumSamples;
}
