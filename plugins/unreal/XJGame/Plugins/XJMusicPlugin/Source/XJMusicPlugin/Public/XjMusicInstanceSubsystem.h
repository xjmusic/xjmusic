// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Subsystems/GameInstanceSubsystem.h"
#include "Quartz/QuartzSubsystem.h"
#include "XjMusicInstanceSubsystem.generated.h"

UCLASS()
class XJMUSICPLUGIN_API UXjMusicInstanceSubsystem : public UGameInstanceSubsystem
{
	GENERATED_BODY()
	
public:
	void RetrieveProjectsContent(const FString& Directory);

	//Play audio from loaded tracks. GlobalStartTime in millieseconds
	bool PlayAudioByName(const FString& Name, const float GlobalStartTime);

	void StopAudioByName(const FString& Name);

	bool IsAudioScheduled(const FString& Name, const float Time) const;

private:
	USoundWave* GetSoundWaveByName(const FString& AudioName);

	void InitQuartz();

private:
	FAudioDeviceHandle WorldAudioDeviceHandle;

	const FString AudioExtension = ".wav";

	TMap<FString, FString> AudioPathsByNameLookup;

	TMap<FString, TMap<float, UAudioComponent*>> SoundsMap;

	UQuartzClockHandle* QuartzClockHandle;
};