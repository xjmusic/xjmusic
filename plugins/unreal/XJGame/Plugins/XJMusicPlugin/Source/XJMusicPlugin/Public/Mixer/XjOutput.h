// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Sound/SoundWaveProcedural.h"
#include "XjOutput.generated.h"

UCLASS()
class XJMUSICPLUGIN_API UXjOutput : public USoundWaveProcedural
{
	GENERATED_BODY()
	
public:
	TFunction<int32(TArray<uint8>&, int32)> GenerateCallback;

	int32 OnGeneratePCMAudio(TArray<uint8>& OutAudio, int32 NumSamples) override;
};
