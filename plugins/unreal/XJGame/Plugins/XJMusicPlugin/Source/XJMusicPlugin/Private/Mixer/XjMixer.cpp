// Fill out your copyright notice in the Description page of Project Settings.


#include "Mixer/XjMixer.h"
#include "Mixer/XjOutput.h"
#include "Components/AudioComponent.h"
#include "Kismet/GameplayStatics.h"
#include "XjMusicInstanceSubsystem.h"

void UXjMixer::Setup()
{
	Output = NewObject<UXjOutput>();

	if (!IsValid(Output))
	{
		return;
	}

	Output->SetSampleRate(48000);
	Output->NumChannels = 2;
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

int32 UXjMixer::OnGeneratePCMAudio(TArray<uint8>& OutAudio, int32 NumSamples)
{
	OutAudio.Reset();

	OutAudio.AddZeroed(NumSamples * sizeof(int16));
	int16* OutAudioBuffer = (int16*)OutAudio.GetData();

	for (int32 Sample = 0; Sample < NumSamples; ++Sample)
	{
		OutAudioBuffer[Sample] = static_cast<int32>((((SampleCounter * (SampleCounter >> 8 | SampleCounter >> 9) & 46 & SampleCounter >> 8)) ^ (SampleCounter & SampleCounter >> 13 | SampleCounter >> 6)) & 0xFF);
	
		SampleCounter += Sample;
	}

	return NumSamples;
}
