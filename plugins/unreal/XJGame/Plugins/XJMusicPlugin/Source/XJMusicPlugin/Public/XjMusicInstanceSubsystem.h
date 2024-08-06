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

	USoundWave* GetSoundWaveById(const FString& Id, const float Duration = 0.0f);

	void InitQuartz();

	void OnEnabledShowDebugChain(class IConsoleVariable* Var);

	void UpdateDebugChainView();

	bool PlayAudio(const FAudioPlayer& Audio);

	UFUNCTION()
	void CheckActiveAudios();

private:
	FTimerHandle CheckTimerHandle;

	UPROPERTY()
	class UXjManager* Manager = nullptr;

	UQuartzClockHandle* QuartzClockHandle;

	FAudioDeviceHandle WorldAudioDeviceHandle;

	TWeakPtr<TEngineBase> ActiveEngine;

	TSharedPtr<SDebugChainView> DebugChainViewWidget;

	TMap<FString, FString> AudioPathsByNameLookup;

	UPROPERTY()
	TMap<FString, UAudioComponent*> SoundsMap;

	TMap<FString, FAudioPlayer> ActiveAudios;

	mutable FCriticalSection SoundsMapCriticalSection;

	const FString AudioExtension = ".wav";

	UPROPERTY()
	class USoundConcurrency* SoundConcurrency = nullptr;
};