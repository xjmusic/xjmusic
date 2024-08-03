// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Quartz/AudioMixerClockHandle.h"
#include "Subsystems/GameInstanceSubsystem.h"
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

	void AddActiveAudio(const FAudioPlayer& Audio);

	void UpdateActiveAudio(const FAudioPlayer& Audio);

	void RemoveActiveAudio(const FAudioPlayer& Audio);

	virtual void Initialize(FSubsystemCollectionBase& Collection) override;

	virtual void Deinitialize() override;

	const TMap<FString, FAudioPlayer>& GetActiveAudios() const
	{
		return ActiveAudios;
	}

private:

	USoundWave* GetSoundWaveByName(const FString& AudioName);

	void InitQuartz();

	void OnEnabledShowDebugChain(class IConsoleVariable* Var);

	void UpdateDebugChainView();

private:

	class UXjManager* Manager = nullptr;

	UQuartzClockHandle* QuartzClockHandle;

	FAudioDeviceHandle WorldAudioDeviceHandle;

	TWeakPtr<TEngineBase> ActiveEngine;

	TSharedPtr<SDebugChainView> DebugChainViewWidget;

	TMap<FString, FString> AudioPathsByNameLookup;

	TMap<FString, TMap<float, UAudioComponent*>> SoundsMap;

	TMap<FString, FAudioPlayer> ActiveAudios;

	mutable FCriticalSection SoundsMapCriticalSection;

	const FString AudioExtension = ".wav";

	float PlanAheadMs = 64;
};