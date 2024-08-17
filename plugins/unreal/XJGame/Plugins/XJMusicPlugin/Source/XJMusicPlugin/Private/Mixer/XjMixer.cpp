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
	
	UXjMusicInstanceSubsystem* XjSubsystem = GetWorld()->GetGameInstance()->GetSubsystem<UXjMusicInstanceSubsystem>();
	if (XjSubsystem)
	{
		USoundWave* Wave = XjSubsystem->GetSoundWaveById("0d1ba43d-02b4-47e2-9e51-dc3d37f5d55d.wav", 20);
		TimeRecord Start;
		TimeRecord End;

		AudioComponent = UGameplayStatics::CreateSound2D(GetWorld(), Wave);
		if (AudioComponent)
		{
			AudioComponent->Play();
		}

		Start.SetInSeconds(0);
		End.SetInSeconds(20.0f);

		TestAudio.Wave = Wave;
		TestAudio.StartSamples = Start.GetSamples(SampleRate, NumChannels);
		TestAudio.EndSamples = End.GetSamples(SampleRate, NumChannels);
	}
}

void UXjMixer::Shutdown()
{
	AudioComponent->MarkPendingKill();
	Output->MarkPendingKill();
}

void UXjMixer::AddActiveAudio(const FMixerAudio& Audio)
{
}

void UXjMixer::UpdateActiveAudio(const FMixerAudio& Audio)
{
}

void UXjMixer::RemoveActiveAudio(const FMixerAudio& Audio)
{
}

int32 UXjMixer::OnGeneratePCMAudio(TArray<uint8>& OutAudio, int32 NumSamples)
{
	OutAudio.Reset();

	OutAudio.AddZeroed(NumSamples * sizeof(int16));
	int16* OutAudioBuffer = (int16*)OutAudio.GetData();


	uint8* RawData = (uint8*)TestAudio.Wave->RawData.Lock(LOCK_READ_ONLY);
	int32 RawDataSize = TestAudio.Wave->RawData.GetBulkDataSize();

	int16* SamplesData = (int16*)RawData;
	int32 AudioSamples = RawDataSize / sizeof(int16);

	for (int32 Sample = 0; Sample < NumSamples; ++Sample)
	{
		if (!TestAudio.Wave)
		{
			break;
		}

		if (SampleCounter < AudioSamples && SampleCounter < TestAudio.EndSamples)
		{
			OutAudioBuffer[Sample] = SamplesData[SampleCounter];
		}

		SampleCounter += 1;
	}

	TestAudio.Wave->RawData.Unlock();

	return NumSamples;
}
