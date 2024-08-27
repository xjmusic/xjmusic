// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Sound/SoundWaveProcedural.h"
#include "XjOutput.generated.h"

DECLARE_DELEGATE_RetVal_TwoParams(int32, FOnSoundGeneratePCM, TArray<uint8>&, int32);

UCLASS()
class XJMUSICPLUGIN_API UXjOutput : public USoundWaveProcedural
{
	GENERATED_BODY()

public:

	FOnSoundGeneratePCM OnGeneratePCMData;

	int32 OnGeneratePCMAudio(TArray<uint8>& OutAudio, int32 NumSamples) override;

	virtual Audio::EAudioMixerStreamDataFormat::Type GetGeneratedPCMDataFormat() const override
	{
		return Audio::EAudioMixerStreamDataFormat::Type::Float;
	}
};
