// Fill out your copyright notice in the Description page of Project Settings.


#include "Mixer/XjMixer.h"
#include "Mixer/XjOutput.h"
#include "Components/AudioComponent.h"
#include "Kismet/GameplayStatics.h"
#include "XjMusicInstanceSubsystem.h"

void FMixerAudio::BeginReadSample()
{
	if (!Wave || Wave->RawData.IsLocked())
	{
		return;
	}

	uint8* RawData = (uint8*)Wave->RawData.LockReadOnly();

	if (!RawData)
	{
		return;
	}

	int32 RawDataSize = Wave->RawData.GetBulkDataSize();

	ReadSamplesData = (int16*)RawData;
	ReadNumSamples = RawDataSize / sizeof(int16);
}

int16 FMixerAudio::ReadSample()
{
	uint16 Sample = 0;

	BeginReadSample();

	if (ReadSamplesData && SamplePointer < ReadNumSamples && SamplePointer < (EndSamples - StartSamples))
	{
		Sample = ReadSamplesData[SamplePointer];
	}

	EndReadSample();

	SamplePointer++;

	return Sample;
}

void FMixerAudio::EndReadSample()
{
	if (!Wave)
	{
		return;
	}

	Wave->RawData.Unlock();
}

void UXjMixer::Setup()
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
	Output->GenerateCallback 
		= [this](TArray<uint8>& OutAudio, int32 NumSamples) -> int32
		{
			return OnGeneratePCMAudio(OutAudio, NumSamples);
		};

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
		int32 MixedData = 0;

		for (TPair<FString, FMixerAudio>& Audio : ActiveAudios)
		{
			if (SampleCounter > Audio.Value.StartSamples)
			{
				MixedData += Audio.Value.ReadSample();
			}
		}

		//Clipping fix. TODO make this using limits in somewhere else
		if (MixedData > 32767)
		{
			MixedData = 32767;
		}
		else if (MixedData < -32768)
		{
			MixedData = -32768;
		}

		OutAudioBuffer[Sample] = MixedData;

		SampleCounter += 1;
	}

	return NumSamples;
}
