// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Subsystems/GameInstanceSubsystem.h"
#include "XjMusicInstanceSubsystem.generated.h"

UCLASS()
class XJMUSICPLUGIN_API UXjMusicInstanceSubsystem : public UGameInstanceSubsystem
{
	GENERATED_BODY()
	
public:
	void RetriveProjectsContent();

	USoundWave* GetSoundWaveByName(const FString& AudioName);

	void TestPlayAllSounds(AActor* AudioActor);

private:
	UFUNCTION()
	void OnTestTimerCallback();

private:
	const FString AudioExtension = ".wav";

	TMap<FString, FString> AudioPathsByNameLookup;

	FTimerHandle TestTimerHandle;

	int TestAudioCounter = 0;

	class AXjTestActor* XjTestActor;
};