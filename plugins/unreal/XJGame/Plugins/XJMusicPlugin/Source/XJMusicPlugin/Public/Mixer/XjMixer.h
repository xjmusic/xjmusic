// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "XjMixer.generated.h"


UCLASS()
class XJMUSICPLUGIN_API UXjMixer : public UObject
{
	GENERATED_BODY()

public:

	void Setup();

	void Shutdown();

private:

	UPROPERTY()
	class UAudioComponent* AudioComponent;

	UPROPERTY()
	class UXjOutput* Output;

	int32 SampleCounter = 0;

	int32 OnGeneratePCMAudio(TArray<uint8>& OutAudio, int32 NumSamples);
};
