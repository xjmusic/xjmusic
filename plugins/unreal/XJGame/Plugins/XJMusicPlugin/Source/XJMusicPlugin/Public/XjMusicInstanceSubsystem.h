// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Quartz/AudioMixerClockHandle.h"
#include "Subsystems/GameInstanceSubsystem.h"
#include "Widgets/DebugChainView.h"
#include "Types/XjObjectPool.h"

#include "XjMusicInstanceSubsystem.generated.h"

UCLASS(DisplayName = "XjSubsystem")
class XJMUSICPLUGIN_API UXjMusicInstanceSubsystem : public UGameInstanceSubsystem
{
	GENERATED_BODY()
	
public:

	UFUNCTION(BlueprintCallable)
	void DoOverrideTaxonomy(const FString Taxonomy);

	void SetupXJ();

	void ShutdownXJ();

	void RetrieveProjectsContent(const FString& Directory);

	void AddActiveAudio(const FAudioPlayer& Audio);

	void UpdateActiveAudio(const FAudioPlayer& Audio);

	void RemoveActiveAudio(const FAudioPlayer& Audio);

	virtual void Initialize(FSubsystemCollectionBase& Collection) override;

	const TMap<FString, FAudioPlayer>& GetActiveAudios() const
	{
		return ActiveAudios;
	}

	USoundWave* GetSoundWaveById(const FString& Id, const float Duration = 0.0f);

private:
	void InitQuartz();

	void OnEnabledShowDebugChain(class IConsoleVariable* Var);

	void UpdateDebugChainView();

	bool PlayAudio(const FAudioPlayer& Audio);

	void OnAudioComponentFinished(UAudioComponent* AudioComponent);

private:
	UPROPERTY()
	class UXjManager* Manager = nullptr;

	UPROPERTY()
	class UXjMixer* Mixer = nullptr;

	UPROPERTY()
	UQuartzClockHandle* QuartzClockHandle = nullptr;

	UPROPERTY()
	UQuartzSubsystem* QuartzSubsystem = nullptr;

	UPROPERTY()
	TMap<uint32, USoundWave*>  CachedSoundWaves;

	UPROPERTY()
	class USoundConcurrency* SoundConcurrency = nullptr;

	const FString AudioExtension = ".wav";

	FAudioDeviceHandle WorldAudioDeviceHandle;

	TSharedPtr<SDebugChainView> DebugChainViewWidget;

	FXjObjectPool AudioComponentsPool;

	mutable FCriticalSection SoundsMapCriticalSection;

	TMap<FString, UAudioComponent*> SoundsMap;

	TMap<FString, FString> AudioPathsByNameLookup;
	TMap<FString, FAudioPlayer> ActiveAudios;
};