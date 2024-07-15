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
	void RetriveProjectsContent(const FString& Directory);

	void PlayAudioByName(const FString& Name);

	void StopAudioByName(const FString& Name);

	void TestPlayAllSounds();

private:
	UFUNCTION()
	void OnTestTimerCallback();

	USoundWave* GetSoundWaveByName(const FString& AudioName);

private:
	const FString AudioExtension = ".wav";

	TMap<FString, FString> AudioPathsByNameLookup;

	TMap<FString, TSharedPtr<FActiveSound>> SoundsMap;

	FAudioDeviceHandle WorldAudioDeviceHandle;

	FString TestLastAudioName;

	FTimerHandle TestTimerHandle;

	int TestAudioCounter = 0;
};