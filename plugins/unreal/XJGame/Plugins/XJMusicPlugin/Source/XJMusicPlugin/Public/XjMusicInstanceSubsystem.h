// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Subsystems/GameInstanceSubsystem.h"
#include "Quartz/QuartzSubsystem.h"
#include "Widgets/DebugChainView.h"

#include "XjMusicInstanceSubsystem.generated.h"

UCLASS(DisplayName = "XjSubsystem")
class XJMUSICPLUGIN_API UXjMusicInstanceSubsystem : public UGameInstanceSubsystem
{
	GENERATED_BODY()
	
public:
	void SetupXJ();

	void RetrieveProjectsContent(const FString& Directory);

	void SetActiveEngine(const TWeakPtr<TEngineBase>& Engine);

	//Play audio from loaded tracks. GlobalStartTime and GlobalEndTime in millieseconds
	bool PlayAudioByName(const FString& Name, const float GlobalStartTime, const float Duration);

	void StopAudioByName(const FString& Name);

	bool IsAudioScheduled(const FString& Name, const float Time) const;

	virtual void Initialize(FSubsystemCollectionBase& Collection) override;

	virtual void Deinitialize() override;

private:

	USoundWave* GetSoundWaveByName(const FString& AudioName);

	void InitQuartz();

	void OnEnabledShowDebugChain(class IConsoleVariable* Var);

private:

	class UXjManager* Manager = nullptr;

	FAudioDeviceHandle WorldAudioDeviceHandle;

	const FString AudioExtension = ".wav";

	TMap<FString, FString> AudioPathsByNameLookup;

	TMap<FString, TMap<float, UAudioComponent*>> SoundsMap;

	mutable FCriticalSection SoundsMapCriticalSection;

	UQuartzClockHandle* QuartzClockHandle;

	float PlanAheadMs = 64;

	TWeakPtr<TEngineBase> ActiveEngine;

	TSharedPtr<SDebugChainView> DebugChainViewWidget;
};